package pro.belbix.ethparser.web3;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@Service
@Log4j2
public class Web3Subscriber {

  private final Web3Functions web3Functions;
  private final AppProperties appProperties;
  private final UniswapDbService uniswapDbService;
  private final VaultActionsDBService vaultActionsDBService;
  private final EthBlockRepository ethBlockRepository;
  private final NetworkProperties networkProperties;

  private final List<BlockingQueue<Web3Model<Transaction>>> transactionConsumers = new ArrayList<>();
  private final List<BlockingQueue<Web3Model<Log>>> logConsumers = new ArrayList<>();
  private final List<BlockingQueue<Web3Model<EthBlock>>> blockConsumers = new ArrayList<>();
  private final Map<String, Disposable> subscriptions = new HashMap<>();

  public Web3Subscriber(Web3Functions web3Functions,
      AppProperties appProperties,
      UniswapDbService uniswapDbService,
      VaultActionsDBService vaultActionsDBService,
      EthBlockRepository ethBlockRepository,
      NetworkProperties networkProperties) {
    this.web3Functions = web3Functions;
    this.appProperties = appProperties;
    this.uniswapDbService = uniswapDbService;
    this.vaultActionsDBService = vaultActionsDBService;
    this.ethBlockRepository = ethBlockRepository;
    this.networkProperties = networkProperties;
  }

  public void subscribeLogFlowable(String network) {
    if (!networkProperties.get(network).isParseLog()) {
      return;
    }
    DefaultBlockParameter from;
    if (Strings.isBlank(networkProperties.get(network).getStartLogBlock())) {
      from = new DefaultBlockParameterNumber(
          findEarliestLastBlock(network).subtract(BigInteger.TEN));
    } else {
      from = DefaultBlockParameter.valueOf(
          new BigInteger(networkProperties.get(network).getStartLogBlock()));
    }
    EthFilter filter = new EthFilter(
        from, LATEST, ContractUtils.getInstance(network).getSubscriptions());
    startLogFlowableThread(filter, network);
    //NPE https://github.com/web3j/web3j/issues/1264
    /*
        Disposable subscription = web3.ethLogFlowable(filter)
            .subscribe(log -> logConsumers.forEach(queue ->
                    writeInQueue(queue, log)),
                e -> log.error("Log flowable error", e));
        subscriptions.add(subscription);
     */
    log.info("Subscribe to Log Flowable from {}", from.getValue());
  }

  public void subscribeTransactionFlowable(String network) {
    if (!networkProperties.get(network).isParseTransactions()) {
      return;
    }
    String name = "subscribeTransactionFlowable";
    subscriptions.computeIfPresent(name, (s, d) -> {
      d.dispose();
      return null;
    });
    subscriptions.put(name,
        web3Functions.transactionFlowable(
            networkProperties.get(network).getStartTransactionBlock(), network)
            .subscribe(tx -> transactionConsumers.forEach(queue ->
                    writeInQueue(queue, tx, network)),
                e -> {
                  log.error("Transaction flowable error", e);
                  if (appProperties.isReconnectSubscriptions()) {
                    Thread.sleep(10000);
                    subscribeTransactionFlowable(network);
                  } else {
                    if (appProperties.isStopOnParseError()) {
                      System.exit(-1);
                    }
                  }
                })
    );
    log.info("Subscribe to Transaction Flowable");
  }

  public void subscribeOnBlocks(String network) {
    if (!networkProperties.get(network).isParseBlocks()) {
      return;
    }
    String name = "subscribeOnBlocks";
    subscriptions.computeIfPresent(name, (s, d) -> {
      d.dispose();
      return null;
    });
    subscriptions.put(name,
        web3Functions.blockFlowable(networkProperties.get(network).getParseBlocksFrom(), () ->
            Optional.ofNullable(ethBlockRepository.findFirstByOrderByNumberDesc())
                .map(EthBlockEntity::getNumber), network)
            .subscribe(tx -> blockConsumers.forEach(queue ->
                    writeInQueue(queue, tx, network)),
                e -> {
                  log.error("Block flowable error", e);
                  if (appProperties.isReconnectSubscriptions()) {
                    Thread.sleep(10000);
                    subscribeOnBlocks(network);
                  } else {
                    if (appProperties.isStopOnParseError()) {
                      System.exit(-1);
                    }
                  }
                })
    );
    log.info("Subscribe to Block Flowable");
  }

  public Disposable getTransactionFlowableRangeSubscription(
      BlockingQueue<Web3Model<Transaction>> transactionQueue,
      DefaultBlockParameter start,
      DefaultBlockParameter end,
      String network) {
    log.info("Start flow for block range " + start.getValue() + " - " + end.getValue());
    Disposable subscription =
        web3Functions.transactionsFlowable(start, end, network).subscribe(
            tx -> writeInQueue(transactionQueue, tx, network),
            e -> log.error("Transaction flowable error", e)
        );
    log.info("Subscribed to Transaction Flowable Range");
    return subscription;
  }

  private BigInteger findEarliestLastBlock(String network) {
    BigInteger lastBlocUniswap = uniswapDbService.lastBlock();
    BigInteger lastBlocHarvest = vaultActionsDBService.lastBlock(network);
    //if only one enabled
    if (networkProperties.get(network).isParseHarvestLog()
        && !networkProperties.get(network).isParseUniswapLog()) {
      return lastBlocHarvest;
    }
    if (!networkProperties.get(network).isParseHarvestLog()
        && networkProperties.get(network).isParseUniswapLog()) {
      return lastBlocUniswap;
    }
    //multiple enabled
    if (lastBlocHarvest.intValue() < lastBlocUniswap.intValue()) {
      return lastBlocHarvest;
    } else {
      return lastBlocUniswap;
    }
  }

  public void startLogFlowableThread(EthFilter filter, String network) {
    Web3LogFlowable logFlowable = new Web3LogFlowable(filter, web3Functions, logConsumers, network);
    new Thread(logFlowable).start();
  }

  public void subscribeOnTransactions(BlockingQueue<Web3Model<Transaction>> queue) {
    transactionConsumers.add(queue);
  }

  public void subscribeOnLogs(BlockingQueue<Web3Model<Log>> queue) {
    logConsumers.add(queue);
  }

  public void subscribeOnBlocks(BlockingQueue<Web3Model<EthBlock>> queue) {
    blockConsumers.add(queue);
  }

  private <T> void writeInQueue(BlockingQueue<Web3Model<T>> queue, T o, String network) {
    int count = 0;
    Web3Model<T> model = new Web3Model<>(o, network);
    while (true) {
      try {
        boolean result = queue.offer(model, 60, SECONDS);
        if (result) {
          return;
        }
        count++;
        log.warn("The queue is full for {}, retry {}",
            o.getClass().getSimpleName(), count);
      } catch (Exception e) {
        log.error("Error write in queue", e);
      }
    }
  }

  @PreDestroy
  private void close() {
    log.info("Close web3 subscriber");
    subscriptions.forEach((s, disposable) -> {
      if (disposable != null && !disposable.isDisposed()) {
        disposable.dispose();
      }
    });
  }
}
