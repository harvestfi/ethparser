package pro.belbix.ethparser.web3.blocks.parser;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.blocks.db.EthBlockDbService;

@Service
@Log4j2
public class EthBlockParser implements Web3Parser {

    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<EthBlock> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final Web3Service web3Service;
    private final AppProperties appProperties;
    private final ParserInfo parserInfo;
    private final EthBlockDbService ethBlockDbService;
    private Instant lastTx = Instant.now();
    private long count = 0;

    public EthBlockParser(Web3Service web3Service,
                          AppProperties appProperties, ParserInfo parserInfo,
                          EthBlockDbService ethBlockDbService) {
        this.web3Service = web3Service;
        this.appProperties = appProperties;
        this.parserInfo = parserInfo;
        this.ethBlockDbService = ethBlockDbService;
    }

    @Override
    public void startParse() {
        log.info("Start parse Blocks");
        parserInfo.addParser(this);
        web3Service.subscribeOnBlocks(logs);
        new Thread(() -> {
            while (run.get()) {
                EthBlock ethBlock = null;
                try {
                    ethBlock = logs.poll(1, TimeUnit.SECONDS);
                    count++;
                    if (count % 100 == 0) {
                        log.info(this.getClass().getSimpleName() + " handled " + count);
                    }
                    EthBlockEntity entity = parse(ethBlock);
                    if (entity != null) {
                        lastTx = Instant.now();
                        ethBlockDbService.save(entity)
                            .thenAccept(persistedBlock -> {
                                if (persistedBlock != null) {
                                    try {
                                        output.put(persistedBlock);
                                    } catch (InterruptedException ignored) {
                                    }
                                }
                            });
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

    public EthBlockEntity parse(EthBlock ethBlock) {
        if (ethBlock == null) {
            return null;
        }
        Block block = ethBlock.getBlock();
        EthBlockEntity ethBlockEntity = blockToEntity(block);

        Set<EthTxEntity> ethTxEntities = new HashSet<>();
        Map<String, EthTxEntity> txMap = new HashMap<>();
        //noinspection unchecked
        for (TransactionResult<Transaction> transactionResult : block.getTransactions()) {
            Transaction transaction = transactionResult.get();
            EthTxEntity ethTxEntity = transactionToEntity(transaction, ethBlockEntity);
            ethTxEntities.add(ethTxEntity);
            txMap.put(ethTxEntity.getHash().getHash(), ethTxEntity);
        }
        ethBlockEntity.setTransactions(ethTxEntities);

        transactionReceipts(txMap, 0);

        return ethBlockEntity;
    }

    private EthTxEntity transactionToEntity(Transaction transaction, EthBlockEntity ethBlockEntity) {
        EthTxEntity ethTxEntity = new EthTxEntity();

        ethTxEntity.setHash(new EthHashEntity(transaction.getHash()));
        ethTxEntity.setNonce(transaction.getNonce().toString());
        ethTxEntity.setTransactionIndex(transaction.getTransactionIndex().longValue());
        ethTxEntity.setBlockNumber(ethBlockEntity);
        ethTxEntity.setFromAddress(new EthAddressEntity(transaction.getFrom()));
        ethTxEntity.setToAddress(new EthAddressEntity(transaction.getTo()));
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
        ethBlockEntity.setHash(new EthHashEntity(block.getHash()));
        ethBlockEntity.setParentHash(new EthHashEntity(block.getParentHash()));
        ethBlockEntity.setNonce(block.getNonce().toString());
        ethBlockEntity.setAuthor(block.getAuthor());
        ethBlockEntity.setMiner(new EthAddressEntity(block.getMiner()));
        ethBlockEntity.setDifficulty(block.getDifficulty().toString());
        ethBlockEntity.setTotalDifficulty(block.getTotalDifficulty().toString());
        ethBlockEntity.setExtraData(block.getExtraData());
        ethBlockEntity.setSize(block.getSize().longValue());
        ethBlockEntity.setGasLimit(block.getGasLimit().longValue());
        ethBlockEntity.setGasUsed(block.getGasUsed().longValue());
        ethBlockEntity.setTimestamp(block.getTimestamp().longValue());
        return ethBlockEntity;
    }

    private void transactionReceipts(Map<String, EthTxEntity> txMap, int retryCount) {
        if (retryCount > 10) {
            return;
        }
        Stream<Optional<TransactionReceipt>> receipts =
            web3Service.fetchTransactionReceiptBatch(txMap.keySet());
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
            transactionReceipts(txMap, retryCount + 1);
        }
    }

    private void fillTxFromReceipt(EthTxEntity tx, TransactionReceipt receipt) {
        tx.setCumulativeGasUsed(receipt.getCumulativeGasUsed().longValue());
        tx.setGasUsed(receipt.getCumulativeGasUsed().longValue());
        tx.setContractAddress(receipt.getContractAddress());
        tx.setRoot(receipt.getRoot());
        tx.setStatus(receipt.getStatus());
        tx.setRevertReason(receipt.getRevertReason());

        tx.setLogs(receipt.getLogs().stream()
            .map(l -> ethLogToEntity(l, tx))
            .collect(Collectors.toSet())
        );
    }

    private EthLogEntity ethLogToEntity(Log ethLog, EthTxEntity tx) {
        EthLogEntity ethLogEntity = new EthLogEntity();

        ethLogEntity.setLogId(ethLog.getLogIndex().longValue());
        ethLogEntity.setRemoved(ethLog.isRemoved() ? 1 : 0);
        ethLogEntity.setTransactionIndex(ethLog.getTransactionIndex().longValue());
        ethLogEntity.setData(ethLog.getData());
        ethLogEntity.setType(ethLog.getType());
        ethLogEntity.setTx(tx);

        if (ethLog.getTopics() != null && !ethLog.getTopics().isEmpty()) {
            ethLogEntity.setFirstTopic(new EthHashEntity(ethLog.getTopics().remove(0)));
            ethLogEntity.setTopics(String.join(",", ethLog.getTopics()));
        }

        return ethLogEntity;
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    @Override
    public Instant getLastTx() {
        return lastTx;
    }
}
