package pro.belbix.ethparser.web3.layers.blocks.parser;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;

@Service
@Log4j2
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EthBlockParser {

  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final BlockingQueue<EthBlock> input = new ArrayBlockingQueue<>(10);
  private final BlockingQueue<EthBlockEntity> output = new ArrayBlockingQueue<>(10);
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final AppProperties appProperties;
  private final EthBlockDbService ethBlockDbService;
  private Instant lastTx = Instant.now();
  private long count = 0;

  public EthBlockParser(Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, AppProperties appProperties,
      EthBlockDbService ethBlockDbService) {
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.appProperties = appProperties;
    this.ethBlockDbService = ethBlockDbService;
  }

  public void startParse() {
    log.info("Start parse Blocks");
    web3Subscriber.subscribeOnBlocks(input);
    new Thread(() -> {
      while (run.get()) {
        EthBlock ethBlock = null;
        try {
          ethBlock = input.poll(1, TimeUnit.SECONDS);
          count++;
          if (count % 100 == 0) {
            log.info(this.getClass().getSimpleName() + " handled " + count);
          }
          EthBlockEntity entity = parse(ethBlock, appProperties.getNetwork());
          if (entity != null) {
            lastTx = Instant.now();
            var persistedBlock = ethBlockDbService.save(entity);
            log.info("Persisted block {} by {}",
                entity.getNumber(), Duration.between(lastTx, Instant.now()).toMillis());
            if (persistedBlock != null) {
              output.put(persistedBlock);
            }
          }
        } catch (Exception e) {
                    log.error("Error block parser loop " + ethBlock, e);
                    if (appProperties.isStopOnParseError()) {
                        System.exit(-1);
                    }
                }
            }
        }).start();
    }

  public EthBlockEntity parse(EthBlock ethBlock, String network) {
    if (ethBlock == null) {
      return null;
    }
    Instant timer = Instant.now();
    Block block = ethBlock.getBlock();
    EthBlockEntity ethBlockEntity = blockToEntity(block);

    Set<EthTxEntity> ethTxEntities = new LinkedHashSet<>();
    Map<String, EthTxEntity> txMap = new LinkedHashMap<>();
    //noinspection unchecked
    for (TransactionResult<Transaction> transactionResult : block.getTransactions()) {
      Transaction transaction = transactionResult.get();
      EthTxEntity ethTxEntity = transactionToEntity(transaction, ethBlockEntity);
      ethTxEntities.add(ethTxEntity);
      txMap.put(ethTxEntity.getHash().getHash(), ethTxEntity);
    }
    ethBlockEntity.setTransactions(ethTxEntities);

    transactionReceipts(txMap, 0, network);

    log.info("Block {} parsed by {}ms", ethBlockEntity.getNumber(),
        Duration.between(timer, Instant.now()).toMillis());
    return ethBlockEntity;
  }

  private EthTxEntity transactionToEntity(Transaction transaction, EthBlockEntity ethBlockEntity) {
    EthTxEntity ethTxEntity = new EthTxEntity();

    ethTxEntity.setHash(new EthHashEntity(transaction.getHash().toLowerCase()));
    ethTxEntity.setNonce(transaction.getNonce().toString());
    ethTxEntity.setTransactionIndex(transaction.getTransactionIndex().longValue());
    ethTxEntity.setBlockNumber(ethBlockEntity);
    ethTxEntity.setFromAddress(new EthAddressEntity(transaction.getFrom().toLowerCase()));
    if (transaction.getTo() != null) {
      ethTxEntity.setToAddress(new EthAddressEntity(transaction.getTo().toLowerCase()));
    }
    ethTxEntity.setValue(transaction.getValue().toString());
    ethTxEntity.setGasPrice(transaction.getGasPrice().longValue());
    ethTxEntity.setGas(transaction.getGas().longValue());
    ethTxEntity.setInput(transaction.getInput());
    ethTxEntity.setCreates(transaction.getCreates());
    ethTxEntity.setPublicKey(transaction.getPublicKey());
    ethTxEntity.setRaw(transaction.getRaw());

    return ethTxEntity;
  }

  private EthBlockEntity blockToEntity(Block block) {
    EthBlockEntity ethBlockEntity = new EthBlockEntity();
    ethBlockEntity.setNumber(block.getNumber().longValue());
    ethBlockEntity.setHash(new EthHashEntity(block.getHash().toLowerCase()));
    ethBlockEntity.setParentHash(new EthHashEntity(block.getParentHash().toLowerCase()));
    ethBlockEntity.setNonce(block.getNonce().toString());
    ethBlockEntity.setAuthor(block.getAuthor());
    ethBlockEntity.setMiner(new EthAddressEntity(block.getMiner().toLowerCase()));
    ethBlockEntity.setDifficulty(block.getDifficulty().toString());
    ethBlockEntity.setTotalDifficulty(block.getTotalDifficulty().toString());
    ethBlockEntity.setExtraData(block.getExtraData());
    ethBlockEntity.setSize(block.getSize().longValue());
    ethBlockEntity.setGasLimit(block.getGasLimit().longValue());
    ethBlockEntity.setGasUsed(block.getGasUsed().longValue());
    ethBlockEntity.setTimestamp(block.getTimestamp().longValue());
    return ethBlockEntity;
  }

  private void transactionReceipts(Map<String, EthTxEntity> txMap, int retryCount, String network) {
    if (retryCount > 1000) {
      throw new IllegalStateException("Can't fetch all receipts");
    }
    Stream<Optional<TransactionReceipt>> receipts =
        web3Functions.fetchTransactionReceiptBatch(txMap.keySet(), network);
    receipts
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(receipt -> {
          EthTxEntity ethTxEntity = txMap.get(receipt.getTransactionHash());
          if (ethTxEntity == null) {
            log.error("Can't map receipt to tx! ");
            return;
          }

          fillTxFromReceipt(ethTxEntity, receipt);
          txMap.remove(receipt.getTransactionHash());
        });
    if (!txMap.isEmpty()) {
      log.error("Got {} empty receipts, retry with timeout", txMap.size());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }
      transactionReceipts(txMap, retryCount + 1, network);
    }
  }

  private void fillTxFromReceipt(EthTxEntity tx, TransactionReceipt receipt) {
    tx.setCumulativeGasUsed(receipt.getCumulativeGasUsed().longValue());
    tx.setGasUsed(receipt.getCumulativeGasUsed().longValue());
    tx.setRoot(receipt.getRoot());
    tx.setStatus(receipt.getStatus());
    tx.setRevertReason(receipt.getRevertReason());

    if (receipt.getContractAddress() != null) {
      tx.setContractAddress(new EthAddressEntity(receipt.getContractAddress().toLowerCase()));
    }

    tx.setLogs(new LinkedHashSet<>(receipt.getLogs().stream()
        .map(l -> ethLogToEntity(l, tx))
        .collect(Collectors.toList()))
    );
  }

  private EthLogEntity ethLogToEntity(Log ethLog, EthTxEntity tx) {
    EthLogEntity ethLogEntity = new EthLogEntity();

    ethLogEntity.setLogId(ethLog.getLogIndex().longValue());
    ethLogEntity.setAddress(new EthAddressEntity(ethLog.getAddress().toLowerCase()));
    ethLogEntity.setRemoved(ethLog.isRemoved() ? 1 : 0);
    ethLogEntity.setTransactionIndex(ethLog.getTransactionIndex().longValue());
    ethLogEntity.setData(ethLog.getData());
    ethLogEntity.setType(ethLog.getType());
    ethLogEntity.setTx(tx);

    if (ethLog.getTopics() != null && !ethLog.getTopics().isEmpty()) {
      ethLogEntity.setFirstTopic(new EthHashEntity(ethLog.getTopics().remove(0).toLowerCase()));
      ethLogEntity.setTopics(String.join(",", ethLog.getTopics()));
    }

    return ethLogEntity;
  }

  public BlockingQueue<EthBlockEntity> getOutput() {
    return output;
  }

  public Instant getLastTx() {
    return lastTx;
  }
}
