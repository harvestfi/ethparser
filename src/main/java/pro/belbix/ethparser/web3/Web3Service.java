package pro.belbix.ethparser.web3;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.web3j.protocol.core.DefaultBlockParameterName.EARLIEST;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParser.ZERO_ADDRESS;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Response.Error;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
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
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.SubscriptionsProperties;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@SuppressWarnings("rawtypes")
@Service
public class Web3Service {

    public final static int RETRY_COUNT = 2;
    public static final int LOG_LAST_PARSED_COUNT = 1_000;
    public static final long MAX_DELAY_BETWEEN_TX = 60 * 10;
    public static final DefaultBlockParameter BLOCK_NUMBER_30_AUGUST_2020 = DefaultBlockParameter
        .valueOf(new BigInteger("10765094"));
    private static final Logger log = LoggerFactory.getLogger(Web3Service.class);
    private Web3j web3;
    private final Set<Disposable> subscriptions = new HashSet<>();
    private final AppProperties appProperties;
    private final SubscriptionsProperties subscriptionsProperties;
    private final UniswapDbService uniswapDbService;
    private final HarvestDBService harvestDBService;
    private boolean init = false;
    private final List<BlockingQueue<Transaction>> transactionConsumers = new ArrayList<>();
    private final List<BlockingQueue<Log>> logConsumers = new ArrayList<>();
    private final AtomicReference<Instant> lastTxTime = new AtomicReference<>(Instant.now());
    private Web3Checker web3Checker;
    private LogFlowable logFlowable;

    public Web3Service(AppProperties appProperties,
                       SubscriptionsProperties subscriptionsProperties,
                       UniswapDbService uniswapDbService,
                       HarvestDBService harvestDBService) {
        this.appProperties = appProperties;
        this.subscriptionsProperties = subscriptionsProperties;
        this.uniswapDbService = uniswapDbService;
        this.harvestDBService = harvestDBService;
    }

    @PostConstruct
    private void init() {
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
            clientBuilder.authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials
                        .basic(appProperties.getWeb3User(), appProperties.getWeb3Password());
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
            });

            HttpService service = new HttpService(appProperties.getWeb3Url(), clientBuilder.build(), false);
            web3 = Web3j.build(service);
        }
        log.info("Successfully connected to Ethereum");
        init = true;
    }

    private void initChecker() {
        if (web3Checker == null) {
            web3Checker = new Web3Checker(lastTxTime, this);
            new Thread(web3Checker).start();
        }
    }

    public void subscribeTransactionFlowable() {
        if (!appProperties.isParseTransactions()) {
            return;
        }
        checkInit();
        Flowable<Transaction> flowable;
        if (Strings.isBlank(appProperties.getStartBlock())) {
            flowable = web3.transactionFlowable();
        } else {
            log.info("Start flow from block " + appProperties.getStartBlock());
            flowable = web3.replayPastAndFutureTransactionsFlowable(
                DefaultBlockParameter.valueOf(new BigInteger(appProperties.getStartBlock())));
        }
        Disposable subscription = flowable
            .subscribe(tx -> transactionConsumers.forEach(queue ->
                    writeInQueue(queue, tx)),
                e -> log.error("Transaction flowable error", e));
        subscriptions.add(subscription);
        initChecker();
        log.info("Subscribe to Transaction Flowable");
    }

    public void subscribeLogFlowable() {
        if (!appProperties.isParseUniswapLog() && !appProperties.isParseHarvestLog()) {
            return;
        }
        checkInit();
        DefaultBlockParameter from;
        if (Strings.isBlank(appProperties.getStartLogBlock())) {
            from = new DefaultBlockParameterNumber(findEarliestLastBlock());
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
        log.info("Subscribe to Log Flowable from {}", from.getValue());
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

    private <T> void writeInQueue(BlockingQueue<T> queue, T o) {
        try {
            queue.put(o);
            lastTxTime.set(Instant.now());
        } catch (Exception e) {
            log.error("Error write in queue", e);
        }
    }

    public TransactionReceipt fetchTransactionReceipt(String hash) {
        checkInit();
        try {
            EthGetTransactionReceipt ethGetTransactionReceipt = web3.ethGetTransactionReceipt(hash).send();
            Error error = ethGetTransactionReceipt.getError();
            if (error != null) {
                log.error("Got " + error.getCode() + " " + error.getMessage() + " " + error.getData());
            } else {
                return ethGetTransactionReceipt.getTransactionReceipt()
                    .orElseThrow(() -> new IllegalStateException("Receipt is null for " + hash));
            }
        } catch (IOException e) {
            log.error("", e);
        }

        return null;
    }

    public Transaction findTransaction(String hash) throws IOException {
        checkInit();
        return web3.ethGetTransactionByHash(hash).send().getTransaction().orElse(null);
    }

    public Block findBlock(String blockHash) {
        checkInit();
        EthBlock ethBlock;
        try {
            ethBlock = web3.ethGetBlockByHash(blockHash, false).send();
        } catch (IOException e) {
            log.error("Error get block " + blockHash, e);
            return null;
        }
        if (ethBlock != null && ethBlock.getError() != null) {
            log.error("Error fetching block " + ethBlock.getError().getMessage());
            return null;
        }

        if (ethBlock != null) {
            return ethBlock.getBlock();
        }
        return null;
    }

    public double fetchAverageGasPrice() {
        checkInit();
        EthGasPrice gasPrice;
        try {
            gasPrice = web3.ethGasPrice().send();
        } catch (IOException e) {
            log.error("Error fetch gas", e);
            return 0.0;
        }
        if (gasPrice != null && gasPrice.getError() != null) {
            log.error("Error gas fetching " + gasPrice.getError().getMessage());
            return 0.0;
        }
        if (gasPrice != null) {
            return gasPrice.getGasPrice().doubleValue() / 1000_000_000;
        }
        return 0.0;
    }

    public List<LogResult> fetchContractLogs(List<String> adresses, Integer start,
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
            toBlock, adresses);
        EthLog ethLog;
        try {
            ethLog = web3.ethGetLogs(filter).send();
        } catch (IOException e) {
            log.error("Error ethLog", e);
            return new ArrayList<>();
        }
        if (ethLog == null) {
            return new ArrayList<>();
        }
        if (ethLog.getError() != null) {
            log.error("Can't get et log. " + ethLog.getError().getMessage());
            return new ArrayList<>();
        }
        return ethLog.getLogs();
    }

    public double fetchBalance(String hash) {
        checkInit();
        EthGetBalance ethGetBalance;
        try {
            ethGetBalance = web3.ethGetBalance(hash, LATEST).send();
        } catch (IOException e) {
            log.error("Get balance error", e);
            return 0.0;
        }
        if (ethGetBalance == null) {
            return 0.0;
        }
        if (ethGetBalance.getError() != null) {
            log.error("Get balance error callback " + ethGetBalance.getError().getMessage());
            return 0.0;
        }
        return ethGetBalance.getBalance().doubleValue();
    }

    public BigInteger fetchCurrentBlock() {
        EthBlockNumber ethBlockNumber = null;
        try {
            ethBlockNumber = web3.ethBlockNumber().send();
        } catch (IOException e) {
            log.error("Error last block", e);
        }
        if (ethBlockNumber == null) {
            log.error("Null callback last block");
            return BigInteger.ZERO;
        }
        if (ethBlockNumber.getError() != null) {
            log.error("Error from last block: " + ethBlockNumber.getError());
            return BigInteger.ZERO;
        }
        return ethBlockNumber.getBlockNumber();
    }

    public List<Type> callMethod(Function function, String contractAddress, DefaultBlockParameter block) {
        org.web3j.protocol.core.methods.request.Transaction transaction =
            org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                ZERO_ADDRESS, contractAddress, FunctionEncoder.encode(function));
        EthCall ethCall = null;
        try {
            ethCall = web3.ethCall(transaction, block).send();
        } catch (IOException e) {
            log.error("Error call " + function.getName(), e);
        }
        if (ethCall == null) {
            log.error("Eth call is null " + function.getName());
            return null;
        }
        if (ethCall.getError() != null) {
            log.error(function.getName() + "Eth call callback is error " + ethCall.getError().getMessage());
            if (ethCall.getError().getMessage().contains("Disabled in this strategy")) {
                throw new IllegalStateException(ethCall.getError().getMessage());
            }
            return null;
        }
        return FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
    }

    public List<Type> callMethodWithRetry(Function function, String contractAddress, DefaultBlockParameter block) {
        int count = 0;
        do {
            List<Type> result;
            try {
                result = callMethod(function, contractAddress, block);
            } catch (IllegalStateException e) {
                log.warn("Got not retryable error for " + function.getName() + " " + contractAddress);
                return null;
            }

            if (result != null) {
                return result;
            }
            count++;
            log.warn("Fail call eth function, retry " + count);
        } while (count < RETRY_COUNT);
        return null;
    }

    public void subscribeOnTransactions(BlockingQueue<Transaction> queue) {
        transactionConsumers.add(queue);
    }

    public void subscribeOnLogs(BlockingQueue<Log> queue) {
        logConsumers.add(queue);
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

    private void logFlowable(EthFilter filter) {
        logFlowable = new LogFlowable(filter, this);
        new Thread(logFlowable).start();
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
                if (Duration.between(lastActionTime.get(), Instant.now()).getSeconds() > MAX_DELAY_BETWEEN_TX) {
                    log.error("Subscription doesn't receive any messages more than " + MAX_DELAY_BETWEEN_TX);
                    lastActionTime.set(Instant.now());
                    //TODO alert
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

}
