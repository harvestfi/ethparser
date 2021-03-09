package pro.belbix.ethparser.web3;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.web3j.protocol.core.DefaultBlockParameterName.EARLIEST;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.BatchResponse;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Response.Error;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.SubscriptionsProperties;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@SuppressWarnings("rawtypes")
@Service
@Log4j2
public class Web3Service {

  public final static int RETRY_COUNT = 5;
  public static final int LOG_LAST_PARSED_COUNT = 1_000;
  public static final long MAX_DELAY_BETWEEN_TX = 60 * 10;
  public static final DefaultBlockParameter BLOCK_NUMBER_30_AUGUST_2020 = DefaultBlockParameter
      .valueOf(new BigInteger("10765094"));
  private final Set<Disposable> subscriptions = new HashSet<>();
  private final AppProperties appProperties;
  private final SubscriptionsProperties subscriptionsProperties;
  private final UniswapDbService uniswapDbService;
  private final HarvestDBService harvestDBService;
  private final EthBlockRepository ethBlockRepository;
  private final List<BlockingQueue<Transaction>> transactionConsumers = new ArrayList<>();
  private final List<BlockingQueue<Log>> logConsumers = new ArrayList<>();
  private final List<BlockingQueue<EthBlock>> blockConsumers = new ArrayList<>();
  private final AtomicReference<Instant> lastTxTime = new AtomicReference<>(Instant.now());
  private Web3j web3;
  private boolean init = false;
  private Web3Checker web3Checker;
  private LogFlowable logFlowable;

  public Web3Service(AppProperties appProperties,
      SubscriptionsProperties subscriptionsProperties,
      UniswapDbService uniswapDbService,
      HarvestDBService harvestDBService,
      EthBlockRepository ethBlockRepository) {
    this.appProperties = appProperties;
    this.subscriptionsProperties = subscriptionsProperties;
    this.uniswapDbService = uniswapDbService;
    this.harvestDBService = harvestDBService;
    this.ethBlockRepository = ethBlockRepository;
  }

  public TransactionReceipt fetchTransactionReceipt(String hash) {
    checkInit();

    EthGetTransactionReceipt result =
        callWithRetry(() -> {
          EthGetTransactionReceipt ethGetTransactionReceipt
              = web3.ethGetTransactionReceipt(hash).send();
          if (ethGetTransactionReceipt == null) {
            log.error("Null receipt for hash: " + hash);
            return null;
          }
          Error error = ethGetTransactionReceipt.getError();
          if (error != null) {
            log.error("Got " + error.getCode() + " " + error.getMessage()
                + " " + error.getData());
            return null;
          }

          //todo alchemy.io can't return it immediately and return empty response
          if (ethGetTransactionReceipt.getTransactionReceipt().isEmpty()) {
            log.warn("Receipt is empty, retry with sleep");
            Thread.sleep(5000);
            return null;
          }
          return ethGetTransactionReceipt;
        });
    if (result == null) {
      return null;
    }
    return result.getTransactionReceipt()
        .orElseThrow(() -> new IllegalStateException("Receipt is null for " + hash));
  }

  public Stream<Optional<TransactionReceipt>> fetchTransactionReceiptBatch(
      Collection<String> hashes) {
    checkInit();
    BatchResponse batchResponse = callWithRetry(() -> {
      BatchRequest batchRequest = web3.newBatch();
      hashes.forEach(h ->
          batchRequest.add(web3.ethGetTransactionReceipt(h))
      );
      return batchRequest.send();
    });

    return batchResponse.getResponses().stream()
        .map(r -> ((EthGetTransactionReceipt) r).getTransactionReceipt());
  }

  private void checkInit() {
    while (!init) {
      log.info("Wait initialization...");
      try {
        //noinspection BusyWait
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
    }
  }

  public <T> T callWithRetry(Callable<T> callable) {
    int count = 0;
    while (true) {
      T result = null;
      Exception lastError = null;
      try {
        result = callable.call();
      } catch (Exception e) { //by default all errors, but can be filtered by type
        log.warn("Retryable error: " + e.getMessage());
        lastError = e;
      }

      if (result != null) {
        return result;
      }
      count++;
      if (count > RETRY_COUNT) {
        if (lastError != null) {
          lastError.printStackTrace();
        }
        return null;
      }
      log.warn("Fail call web3, retry " + count);
      try {
        //noinspection BusyWait
        Thread.sleep(1000);
      } catch (InterruptedException ignore) {
      }
    }
  }

  public Transaction findTransaction(String hash) {
    checkInit();
    return callWithRetry(
        () -> web3.ethGetTransactionByHash(hash).send().getTransaction().orElse(null));
  }

  public EthBlock findBlockByHash(String blockHash, boolean returnFullTransactionObjects) {
    checkInit();
    EthBlock result = callWithRetry(() -> {
      EthBlock ethBlock = web3.ethGetBlockByHash(blockHash, returnFullTransactionObjects).send();
      if (ethBlock == null) {
        log.error("Error fetching block with hash " + blockHash);
        return null;
      }
      if (ethBlock.getError() != null) {
        log.error("Error fetching block " + ethBlock.getError().getMessage());
        return null;
      }
      return ethBlock;
    });
    return result;
  }

  public EthBlock findBlockByNumber(long number, boolean returnFullTransactionObjects) {
    checkInit();
    return callWithRetry(() -> {
      EthBlock ethBlock = web3.ethGetBlockByNumber(
          DefaultBlockParameter.valueOf(BigInteger.valueOf(number)),
          returnFullTransactionObjects).send();
      if (ethBlock == null) {
        log.error("Error fetching block with number " + number);
        return null;
      }
      if (ethBlock.getError() != null) {
        log.error("Error fetching block " + ethBlock.getError().getMessage());
        return null;
      }
      return ethBlock;
    });
  }

  public double fetchAverageGasPrice() {
    checkInit();
    EthGasPrice result = callWithRetry(() -> {
      EthGasPrice gasPrice = web3.ethGasPrice().send();
      if (gasPrice == null) {
        log.error("Null gas fetching result");
        return null;
      }
      if (gasPrice.getError() != null) {
        log.error("Error gas fetching " + gasPrice.getError().getMessage());
        return null;
      }
      return gasPrice;
    });
    if (result == null) {
      return 0.0;
    }
    return result.getGasPrice().doubleValue() / 1000_000_000;
  }

  public List<LogResult> fetchContractLogs(List<String> addresses, Integer start,
      Integer end) {
    checkInit();
    DefaultBlockParameter fromBlock;
    DefaultBlockParameter toBlock;
    if (start == null) {
      fromBlock = EARLIEST;
    } else {
      fromBlock = new DefaultBlockParameterNumber(new BigInteger(start + ""));
    }
    if (end == null) {
      toBlock = LATEST;
    } else {
      toBlock = new DefaultBlockParameterNumber(new BigInteger(end + ""));
    }
    EthFilter filter = new EthFilter(fromBlock,
        toBlock, addresses);
    EthLog result = callWithRetry(() -> {
      EthLog ethLog = web3.ethGetLogs(filter).send();
      if (ethLog == null) {
        log.error("get logs null result");
        return null;
      }
      if (ethLog.getError() != null) {
        log.error("Can't get eth log. " + ethLog.getError().getMessage());
        return null;
      }
      return ethLog;
    });
    if (result == null) {
      return List.of();
    }
    return result.getLogs();
  }

  public double fetchBalance(String hash) {
    checkInit();
    EthGetBalance result = callWithRetry(() -> {
      EthGetBalance ethGetBalance = web3.ethGetBalance(hash, LATEST).send();
      if (ethGetBalance == null) {
        log.error("Get balance response is null");
        return null;
      }
      if (ethGetBalance.getError() != null) {
        log.error("Get balance error callback " + ethGetBalance.getError().getMessage());
        return null;
      }
      return ethGetBalance;
    });
    if (result == null) {
      return 0.0;
    }
    return result.getBalance().doubleValue();
  }

  public BigInteger fetchCurrentBlock() {
    EthBlockNumber result = callWithRetry(() -> {
      EthBlockNumber ethBlockNumber = web3.ethBlockNumber().send();
      if (ethBlockNumber == null) {
        log.error("Null callback last block");
        return null;
      }
      if (ethBlockNumber.getError() != null) {
        log.error("Error from last block: " + ethBlockNumber.getError());
        return null;
      }
      return ethBlockNumber;
    });
    if (result == null) {
      return BigInteger.ZERO;
    }
    return result.getBlockNumber();
  }

  public List<Type> callFunction(Function function, String contractAddress,
      DefaultBlockParameter block) {
    org.web3j.protocol.core.methods.request.Transaction transaction =
        org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
            ZERO_ADDRESS, contractAddress, FunctionEncoder.encode(function));

    EthCall result = callWithRetry(() -> {
      EthCall ethCall = web3.ethCall(transaction, block).send();
      if (ethCall == null) {
        log.warn("Eth call is null " + function.getName());
        return null;
      }
      if (ethCall.getError() != null) {
        log.warn(function.getName() + " Eth call callback is error "
            + ethCall.getError().getMessage());
        return null;
      }
      return ethCall;
    });
    if (result == null) {
      return null;
    }

    return FunctionReturnDecoder.decode(result.getValue(), function.getOutputParameters());
  }

  public void subscribeOnTransactions(BlockingQueue<Transaction> queue) {
    transactionConsumers.add(queue);
  }

  public void subscribeOnLogs(BlockingQueue<Log> queue) {
    logConsumers.add(queue);
  }

  public void subscribeOnBlocks(BlockingQueue<EthBlock> queue) {
    blockConsumers.add(queue);
  }

  @PreDestroy
  private void close() {
    log.info("Close web3");
    subscriptions.forEach(Disposable::dispose);
    if (web3 != null) {
      web3.shutdown();
    }
    if (web3Checker != null) {
      web3Checker.stop();
    }
  }

  private void resubscribe() {
    log.warn("Resubscribe");
    subscriptions.forEach(Disposable::dispose);
    subscriptions.clear();
    logFlowable.stop();
    web3.shutdown();
    web3Checker.stop();

    //start all again
    init = false;
    init();
    subscribeLogFlowable();
    subscribeTransactionFlowable();
  }

  @PostConstruct
  private void init() {
    if (appProperties.isOnlyApi()) {
      return;
    }
    log.info("Connecting to Ethereum ...");
    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    clientBuilder.callTimeout(600, SECONDS)
        .writeTimeout(600, SECONDS)
        .connectTimeout(600, SECONDS);
    if (Strings.isBlank(appProperties.getWeb3User())) {
      String url;
      if (Strings.isBlank(appProperties.getWeb3Url())) {
        url = System.getProperty("ethjava.web3.url");
      } else {
        url = appProperties.getWeb3Url();
      }
      if (url == null) {
        throw new IllegalStateException("Web3 url not defined");
      }
      HttpService httpService = new HttpService(url, clientBuilder.build(), false);
      web3 = Web3j.build(httpService);
    } else {
      clientBuilder.authenticator((route, response) -> response.request().newBuilder()
          .header("Authorization",
              Credentials.basic(appProperties.getWeb3User(), appProperties.getWeb3Password()))
          .build());

      HttpService service =
          new HttpService(appProperties.getWeb3Url(), clientBuilder.build(), false);
      web3 = Web3j.build(service);
    }
    log.info("Successfully connected to Ethereum");
    init = true;
  }

  public void subscribeLogFlowable() {
    if (!appProperties.isParseLog()) {
      return;
    }
    checkInit();
    DefaultBlockParameter from;
    if (Strings.isBlank(appProperties.getStartLogBlock())) {
      from = new DefaultBlockParameterNumber(findEarliestLastBlock().subtract(BigInteger.TEN));
    } else {
      from = DefaultBlockParameter.valueOf(new BigInteger(appProperties.getStartLogBlock()));
    }
    EthFilter filter = new EthFilter(from, LATEST, subscriptionsProperties.getLogSubscriptions());
    logFlowable(filter);
    //NPE https://github.com/web3j/web3j/issues/1264
//        Disposable subscription = web3.ethLogFlowable(filter)
//            .subscribe(log -> logConsumers.forEach(queue ->
//                    writeInQueue(queue, log)),
//                e -> log.error("Log flowable error", e));
//        subscriptions.add(subscription);
    initChecker();
    log.info("Subscribe to Log Flowable from {}", from);
  }

  public void subscribeTransactionFlowable() {
    if (!appProperties.isParseTransactions()) {
      return;
    }
    checkInit();
    Flowable<Transaction> flowable;
    if (Strings.isBlank(appProperties.getStartTransactionBlock())) {
      flowable = callWithRetry(() -> web3.transactionFlowable());
    } else {
      log.info("Start flow from block " + appProperties.getStartTransactionBlock());
      flowable = callWithRetry(() -> web3.replayPastAndFutureTransactionsFlowable(
          DefaultBlockParameter.valueOf(new BigInteger(appProperties.getStartTransactionBlock()))));
    }
    Disposable subscription = flowable
        .subscribe(tx -> transactionConsumers.forEach(queue ->
                writeInQueue(queue, tx)),
            e -> log.error("Transaction flowable error", e));
    subscriptions.add(subscription);
    initChecker();
    log.info("Subscribe to Transaction Flowable");
  }

  public void subscribeOnBlocks() {
    if (!appProperties.isParseBlocks()) {
      return;
    }
    checkInit();
    Flowable<EthBlock> flowable;
    if (Strings.isBlank(appProperties.getStartBlocksBlock())) {
      Optional<Long> lastBlock =
          Optional.ofNullable(ethBlockRepository.findFirstByOrderByNumberDesc())
              .map(EthBlockEntity::getNumber);
      if (lastBlock.isPresent()) {
        flowable = web3.replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter.valueOf(
                BigInteger.valueOf(lastBlock.get())),
            true
        );
      } else {
        flowable = web3.blockFlowable(true);
      }
    } else {
      flowable = web3.replayPastAndFutureBlocksFlowable(
          DefaultBlockParameter.valueOf(
              new BigInteger(appProperties.getStartBlocksBlock())),
          true
      );
    }
    Disposable subscription = flowable
        .subscribe(tx -> blockConsumers.forEach(queue ->
                writeInQueue(queue, tx)),
            e -> log.error("Block flowable error", e));
    subscriptions.add(subscription);
    initChecker();
    log.info("Subscribe to Block Flowable");
  }

  public Disposable getTransactionFlowableRangeSubscription(
      BlockingQueue<Transaction> transactionQueue,
      DefaultBlockParameter start,
      DefaultBlockParameter end) {
    checkInit();
    String logStart =
        start.getValue().startsWith("0x") ? Long.decode(start.getValue()).toString()
            : start.getValue();
    String logEnd =
        end.getValue().startsWith("0x") ? Long.decode(end.getValue()).toString() : end.getValue();
    log.info("Start flow for block range " + logStart + " - " + logEnd);
    Flowable<Transaction> flowable =
        callWithRetry(() -> web3.replayPastTransactionsFlowable(start, end));
    Disposable subscription =
        flowable.subscribe(
            tx -> writeInQueue(transactionQueue, tx),
            e -> log.error("Transaction flowable error", e));
    initChecker();
    log.info("Subscribed to Transaction Flowable Range");
    return subscription;
  }

  private BigInteger findEarliestLastBlock() {
    BigInteger lastBlocUniswap = uniswapDbService.lastBlock();
    BigInteger lastBlocHarvest = harvestDBService.lastBlock();
    //if only one enabled
    if (appProperties.isParseHarvestLog() && !appProperties.isParseUniswapLog()) {
      return lastBlocHarvest;
    }
    if (!appProperties.isParseHarvestLog() && appProperties.isParseUniswapLog()) {
      return lastBlocUniswap;
    }
    //multiple enabled
    if (lastBlocHarvest.intValue() < lastBlocUniswap.intValue()) {
      return lastBlocHarvest;
    } else {
      return lastBlocUniswap;
    }
  }

  private void logFlowable(EthFilter filter) {
    logFlowable = new LogFlowable(filter, this);
    new Thread(logFlowable).start();
  }

  private void initChecker() {
    if (web3Checker == null) {
      web3Checker = new Web3Checker(lastTxTime, this);
      new Thread(web3Checker).start();
    }
  }

  private <T> void writeInQueue(BlockingQueue<T> queue, T o) {
    try {
      while (!queue.offer(o, 10, SECONDS)) {
        log.warn("The queue is full for " + o);
      }

      lastTxTime.set(Instant.now());
    } catch (Exception e) {
      log.error("Error write in queue", e);
    }
  }

  public static class LogFlowable implements Runnable {

    public static final int DEFAULT_BLOCK_TIME = 5 * 1000;
    private final AtomicBoolean run = new AtomicBoolean(true);
    private final Web3Service web3Service;
    private final List<String> addresses;
    private Integer from;
    private BigInteger lastBlock;

    public LogFlowable(EthFilter filter, Web3Service web3Service) {
      this.web3Service = web3Service;
      this.addresses = filter.getAddress();
      this.from = ((DefaultBlockParameterNumber) filter.getFromBlock()).getBlockNumber().intValue();
    }

    public void stop() {
      run.set(false);
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
      log.info("Start LogFlowable");
      BigInteger currentBlock;
      while (run.get()) {
        try {
          currentBlock = web3Service.fetchCurrentBlock();
          if (lastBlock != null && lastBlock.intValue() >= currentBlock.intValue()) {
            Thread.sleep(DEFAULT_BLOCK_TIME);
            continue;
          }
          lastBlock = currentBlock;
          int to = currentBlock.intValue();
          if (from == null) {
            from = to;
          } else {
            int diff = to - from;
            if (diff > 1000) {
              to = from + 1000;
            }
          }
          List<EthLog.LogResult> logResults = web3Service.fetchContractLogs(addresses, from, to);
          log.info("Parse log from {} to {} on block: {} - {}", from, to,
              currentBlock, logResults.size());
          for (LogResult logResult : logResults) {
            Log ethLog = (Log) logResult.get();
            if (ethLog == null) {
              continue;
            }
            for (BlockingQueue<Log> queue : web3Service.logConsumers) {
              web3Service.writeInQueue(queue, ethLog);
            }
          }
          from = to + 1;
        } catch (Exception e) {
          log.error("Error in log flow", e);
        }
      }
    }
  }

  public static class Web3Checker implements Runnable {

    private final AtomicBoolean run = new AtomicBoolean(true);
    private final AtomicReference<Instant> lastActionTime;
    private final Web3Service web3Service;

    public Web3Checker(AtomicReference<Instant> lastActionTime, Web3Service web3Service) {
      this.lastActionTime = lastActionTime;
      this.web3Service = web3Service;
    }

    public void stop() {
      run.set(false);
    }

    @Override
    public void run() {
      while (run.get()) {
        if (Duration.between(lastActionTime.get(), Instant.now()).getSeconds()
            > MAX_DELAY_BETWEEN_TX) {
          log.warn("Subscription doesn't receive any messages more than " + MAX_DELAY_BETWEEN_TX);
          lastActionTime.set(Instant.now());
          web3Service.resubscribe();
        }
        try {
          //noinspection BusyWait
          Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  public Web3j getWeb3() {
    return web3;
  }
}
