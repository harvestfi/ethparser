package pro.belbix.ethparser.web3.harvest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.model.TransactionDTO;
import pro.belbix.ethparser.repositories.TransactionsRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class HarvestTransactionsParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(HarvestTransactionsParser.class);
    private final HarvestVaultDecoder harvestVaultDecoder = new HarvestVaultDecoder();
    private final Web3Service web3Service;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(10_000);
    private final BlockingQueue<TransactionDTO> output = new ArrayBlockingQueue<>(10_000);
    private final TransactionsRepository transactionsRepository;
    private final EthBlockService ethBlockService;
    private long parsedTxCount = 0;

    public HarvestTransactionsParser(Web3Service web3Service,
                                     TransactionsRepository transactionsRepository,
                                     EthBlockService ethBlockService) {
        this.web3Service = web3Service;
        this.transactionsRepository = transactionsRepository;
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
                TransactionDTO dto = parseHarvestTransaction(transaction);
                if (dto != null) {
                    try {
                        output.put(dto);
                    } catch (InterruptedException e) {
                    }
                    try {
                        transactionsRepository.save(dto);
                    } catch (Exception e) {
                        log.error("Can't save " + dto.toString(), e);
                    }
                }
            }
        }).start();
    }

    @Override
    public BlockingQueue<TransactionDTO> getOutput() {
        return output;
    }

    TransactionDTO parseHarvestTransaction(Transaction tx) {
        incrementAndPrintCount(tx);
        if (!isValidTransaction(tx)) {
            return null;
        }

        HarvestTx harvestTx = decodeTransaction(tx);
        if (harvestTx == null) {
            return null;
        }

        TransactionDTO transactionDTO = harvestTx.toDto();
        transactionDTO.setLastGas(web3Service.fetchAverageGasPrice());
        transactionDTO.setBlockDate(ethBlockService.getTimestampSecForBlock(tx.getBlockHash()));
        print(transactionDTO);
        return transactionDTO;
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

    private void print(TransactionDTO transactionDTO) {
        if (transactionDTO.isConfirmed()) {
            log.info(transactionDTO.print());
        } else {
            log.debug(transactionDTO.print());
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
