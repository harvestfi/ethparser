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
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.SubscriptionsProperties;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@Service
@Log4j2
public class Web3Subscriber {

  private final Web3Functions web3Functions;
  private final AppProperties appProperties;
  private final SubscriptionsProperties subscriptionsProperties;
  private final UniswapDbService uniswapDbService;
  private final HarvestDBService harvestDBService;
  private final EthBlockRepository ethBlockRepository;

  private final List<BlockingQueue<Transaction>> transactionConsumers = new ArrayList<>();
  private final List<BlockingQueue<Log>> logConsumers = new ArrayList<>();
  private final List<BlockingQueue<EthBlock>> blockConsumers = new ArrayList<>();
  private final Map<String, Disposable> subscriptions = new HashMap<>();

  public Web3Subscriber(Web3Functions web3Functions,
      AppProperties appProperties,
      SubscriptionsProperties subscriptionsProperties,
      UniswapDbService uniswapDbService,
      HarvestDBService harvestDBService,
      EthBlockRepository ethBlockRepository) {
    this.web3Functions = web3Functions;
    this.appProperties = appProperties;
    this.subscriptionsProperties = subscriptionsProperties;
    this.uniswapDbService = uniswapDbService;
    this.harvestDBService = harvestDBService;
    this.ethBlockRepository = ethBlockRepository;
  }

  public void subscribeLogFlowable() {
    if (!appProperties.isParseLog()) {
      return;
    }
    web3Functions.waitInit();
    DefaultBlockParameter from;
    if (Strings.isBlank(appProperties.getStartLogBlock())) {
      from = new DefaultBlockParameterNumber(findEarliestLastBlock().subtract(BigInteger.TEN));
    } else {
      from = DefaultBlockParameter.valueOf(new BigInteger(appProperties.getStartLogBlock()));
    }
    EthFilter filter = new EthFilter(from, LATEST, subscriptionsProperties.getLogSubscriptions());
    startLogFlowableThread(filter);
    //NPE https://github.com/web3j/web3j/issues/1264
    /*
        Disposable subscription = web3.ethLogFlowable(filter)
            .subscribe(log -> logConsumers.forEach(queue ->
                    writeInQueue(queue, log)),
                e -> log.error("Log flowable error", e));
        subscriptions.add(subscription);
     */
    log.info("Subscribe to Log Flowable from {}", from);
  }

  public void subscribeTransactionFlowable() {
    if (!appProperties.isParseTransactions()) {
      return;
    }
    String name = "subscribeTransactionFlowable";
    subscriptions.computeIfPresent(name, (s, d) -> {
      d.dispose();
      return null;
    });
    subscriptions.put(name,
        web3Functions.transactionFlowable(appProperties.getStartTransactionBlock())
            .subscribe(tx -> transactionConsumers.forEach(queue ->
                    writeInQueue(queue, tx)),
                e -> {
                  log.error("Transaction flowable error", e);
                  if (appProperties.isReconnectOnWeb3Errors()) {
                    Thread.sleep(10000);
                    subscribeTransactionFlowable();
                  } else {
                    if (appProperties.isStopOnParseError()) {
                      System.exit(-1);
                    }
                  }
                })
    );
    log.info("Subscribe to Transaction Flowable");
  }

  public void subscribeOnBlocks() {
    if (!appProperties.isParseBlocks()) {
      return;
    }
    String name = "subscribeOnBlocks";
    subscriptions.computeIfPresent(name, (s, d) -> {
      d.dispose();
      return null;
    });
    subscriptions.put(name,
        web3Functions.blockFlowable(appProperties.getParseBlocksFrom(), () ->
            Optional.ofNullable(ethBlockRepository.findFirstByOrderByNumberDesc())
                .map(EthBlockEntity::getNumber))
            .subscribe(tx -> blockConsumers.forEach(queue ->
                    writeInQueue(queue, tx)),
                e -> {
                  log.error("Block flowable error", e);
                  if (appProperties.isReconnectOnWeb3Errors()) {
                    Thread.sleep(10000);
                    subscribeOnBlocks();
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
      BlockingQueue<Transaction> transactionQueue,
      DefaultBlockParameter start,
      DefaultBlockParameter end) {
    log.info("Start flow for block range " + start.getValue() + " - " + end.getValue());
    Disposable subscription =
        web3Functions.transactionsFlowable(start, end).subscribe(
            tx -> writeInQueue(transactionQueue, tx),
            e -> log.error("Transaction flowable error", e)
        );
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

  public void startLogFlowableThread(EthFilter filter) {
    Web3LogFlowable logFlowable = new Web3LogFlowable(filter, web3Functions, logConsumers);
    new Thread(logFlowable).start();
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

  private <T> void writeInQueue(BlockingQueue<T> queue, T o) {
    try {
      while (!queue.offer(o, 60, SECONDS)) {
        log.warn("The queue is full for {}", o.getClass().getSimpleName());
      }

    } catch (Exception e) {
      log.error("Error write in queue", e);
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
