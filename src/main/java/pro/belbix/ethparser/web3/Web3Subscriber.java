package pro.belbix.ethparser.web3;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.deployer.db.DeployerDbService;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@Service
@Log4j2
public class Web3Subscriber {

  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final Web3Functions web3Functions;
  private final AppProperties appProperties;
  private final UniswapDbService uniswapDbService;
  private final VaultActionsDBService vaultActionsDBService;
  private final EthBlockRepository ethBlockRepository;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;
  private final DeployerDbService deployerDbService;

  private final List<BlockingQueue<Web3Model<Transaction>>> transactionConsumers = new ArrayList<>();
  private final Map<String, BlockingQueue<Web3Model<Log>>> logConsumers = new HashMap<>();
  private final List<BlockingQueue<Web3Model<EthBlock>>> blockConsumers = new ArrayList<>();
  private final Map<String, Disposable> subscriptions = new HashMap<>();

  private Map<String, Web3LogFlowable> web3LogFlowable = new HashMap<>();
  private Map<String, Web3TransactionFlowable> web3TransactionFlowable = new HashMap<>();

  public Web3Subscriber(Web3Functions web3Functions,
      AppProperties appProperties,
      UniswapDbService uniswapDbService,
      VaultActionsDBService vaultActionsDBService,
      EthBlockRepository ethBlockRepository,
      NetworkProperties networkProperties,
      ContractDbService contractDbService,
      DeployerDbService deployerDbService) {
    this.web3Functions = web3Functions;
    this.appProperties = appProperties;
    this.uniswapDbService = uniswapDbService;
    this.vaultActionsDBService = vaultActionsDBService;
    this.ethBlockRepository = ethBlockRepository;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
    this.deployerDbService = deployerDbService;
  }

  public void subscribeLogFlowable(String network) {
    if (!networkProperties.get(network).isParseLog()) {
      return;
    }
    int from;
    if (Strings.isBlank(networkProperties.get(network).getStartLogBlock())) {
      from = findEarliestLastBlock(network).subtract(BigInteger.TEN).intValue();
      if (from < 1_000_000) {
        from = ContractUtils.getStartBlock(network);
      }
    } else {
      from = new BigInteger(networkProperties.get(network).getStartLogBlock()).intValue();
    }
    startLogFlowableThread(contractDbService::getSubscriptions, from, network);
    log.info("Subscribe to Log Flowable from {}", from);
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
    startTransactionFlowableThread(getLastTransactionBlock(network), network);
  }

  private int getLastTransactionBlock(String network) {
    if (networkProperties.get(network).getStartTransactionBlock().isBlank()) {
      return deployerDbService.getLastBlock(network);
    } else {
      return Integer.parseInt(networkProperties.get(network).getStartTransactionBlock());
    }
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
            Optional.ofNullable(ethBlockRepository.findFirstByNetworkOrderByNumberDesc(
                EthBlockEntity.defineNetwork(network)
            )).map(EthBlockEntity::getNumber), network)
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

  public void startLogFlowableThread(
      Supplier<List<String>> addressesSupplier,
      Integer from,
      String network) {
    if (web3LogFlowable.containsKey(network)) {
      throw new IllegalStateException("Double call log flowable for " + network);
    }
    Web3LogFlowable logFlowable = new Web3LogFlowable(addressesSupplier, from, web3Functions,
        logConsumers, network, () -> logBlockLimitation(network));
    new Thread(logFlowable).start();
    web3LogFlowable.put(network, logFlowable);
  }

  private Long logBlockLimitation(String network) {
    if (appProperties.isLogBlockLimitations()
        && networkProperties.get(network).isParseTransactions()) {
      return (long) web3TransactionFlowable.get(network).getLastParsedBlock();
    }
    return Long.MAX_VALUE;
  }

  public void startTransactionFlowableThread(Integer from, String network) {
    if (this.web3TransactionFlowable.containsKey(network)) {
      throw new IllegalStateException("Double call transaction flowable");
    }
    Web3TransactionFlowable web3TransactionFlowable =
        new Web3TransactionFlowable(from, web3Functions, transactionConsumers, network);
    new Thread(web3TransactionFlowable)
        .start();
    this.web3TransactionFlowable.put(network, web3TransactionFlowable);
  }

  public void subscribeOnTransactions(BlockingQueue<Web3Model<Transaction>> queue) {
    transactionConsumers.add(queue);
  }

  public void subscribeOnLogs(BlockingQueue<Web3Model<Log>> queue, String name) {
    logConsumers.put(name, queue);
  }

  public void subscribeOnBlocks(BlockingQueue<Web3Model<EthBlock>> queue) {
    blockConsumers.add(queue);
  }

  private <T> void writeInQueue(BlockingQueue<Web3Model<T>> queue, T o, String network) {
    int count = 0;
    Web3Model<T> model = new Web3Model<>(o, network);
    while (run.get()) {
      try {
        boolean result = queue.offer(model, 5, SECONDS);
        if (result) {
          return;
        }
        count++;
        log.info("The queue is full for {}, retry {}",
            o.getClass().getSimpleName(), count);
      } catch (Exception e) {
        log.error("Error write in queue", e);
      }
    }
  }

  @PreDestroy
  private void close() {
    log.info("Close web3 subscriber");
    run.set(false);
    web3TransactionFlowable.values().forEach(Web3TransactionFlowable::stop);
    web3LogFlowable.values().forEach(Web3LogFlowable::stop);
    subscriptions.forEach((s, disposable) -> {
      if (disposable != null && !disposable.isDisposed()) {
        disposable.dispose();
      }
    });
  }
}
