package pro.belbix.ethparser.web3.harvest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.model.DtoI;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class HarvestTransactionsParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(HarvestTransactionsParser.class);
    private final HarvestVaultDecoder harvestVaultDecoder = new HarvestVaultDecoder();
    private final Web3Service web3Service;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(10_000);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(10_000);
    private final HarvestDBService harvestDBService;
    private final EthBlockService ethBlockService;
    private long parsedTxCount = 0;

    public HarvestTransactionsParser(Web3Service web3Service,
                                     HarvestDBService harvestDBService,
                                     EthBlockService ethBlockService) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.ethBlockService = ethBlockService;
    }

    public void startParse() {
        log.info("Start parse Harvest");
        web3Service.subscribeOn(transactions);
        new Thread(() -> {
            while (true) {
                Transaction transaction = null;
                try {
                    transaction = transactions.take();
                } catch (InterruptedException ignored) {
                }
                HarvestDTO dto = parseHarvestTransaction(transaction);
                if (dto != null) {
                    try {
                        output.put(dto);
                    } catch (InterruptedException e) {
                    }
                    try {
                        harvestDBService.saveHarvestDTO(dto);
                    } catch (Exception e) {
                        log.error("Can't save " + dto.toString(), e);
                    }
                }
            }
        }).start();
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    HarvestDTO parseHarvestTransaction(Transaction tx) {
        incrementAndPrintCount(tx);
        if (!isValidTransaction(tx)) {
            return null;
        }

        HarvestTx harvestTx = decodeTransaction(tx);
        if (harvestTx == null) {
            return null;
        }

        HarvestDTO dto = harvestTx.toDto();
        dto.setLastGas(web3Service.fetchAverageGasPrice());
        dto.setBlockDate(ethBlockService.getTimestampSecForBlock(tx.getBlockHash()));
        print(dto);
        return dto;
    }

    private HarvestTx decodeTransaction(Transaction tx) {
        HarvestTx harvestTx;
        try {
            harvestTx = (HarvestTx) harvestVaultDecoder.decodeInputData(tx);

            if (harvestTx == null) {
                if (tx.getInput().length() > 70) {
                    log.error("tx not parsed " + tx.getHash());
                }
                return null;
            }

            if (!harvestTx.isContainsAddress(Vaults.vaultNames)) {
                return null;
            }
            TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
            if ("0x1".equals(transactionReceipt.getStatus())) {
                harvestTx.setSuccess(true);
            }

        } catch (Exception e) {
            log.error("Error tx " + tx.getHash(), e);
            return null;
        }
        return harvestTx;
    }

    private void print(HarvestDTO dto) {
        if (dto.isConfirmed()) {
            log.info(dto.print());
        } else {
            log.debug(dto.print());
        }
    }

    private boolean isValidTransaction(Transaction tx) {
        if (tx == null) {
            log.error("Null clear tx!");
            return false;
        }
        if (tx.getTo() == null) {
            //it is contract deploy
            return false;
        }
        return Vaults.vaultNames.containsKey(tx.getTo().toLowerCase());
    }

    private void incrementAndPrintCount(Transaction tx) {
        parsedTxCount++;
        if (parsedTxCount % 10_000 == 0) {
            log.info("Harvest parsed " + parsedTxCount + ", last block: " + tx.getBlockNumber());
        }
    }
}
