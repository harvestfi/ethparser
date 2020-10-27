package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.model.UniswapTx.SWAP;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.model.Printable;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.Web3Service;

@Service
public class UniswapTransactionsParser {

    private static final Logger log = LoggerFactory.getLogger(UniswapTransactionsParser.class);
    public static final String UNI_ROUTER = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d".toLowerCase();
    public static final String FARM_TOKEN_CONTRACT = "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase();
    public static final String FARM_WETH_UNI_CONTRACT = "0x56feAccb7f750B997B36A68625C7C596F0B41A58".toLowerCase();
    private final UniswapRouterDecoder uniswapRouterDecoder = new UniswapRouterDecoder();
    private final Web3Service web3Service;
    private double lastFarmPrice = 0.0;
    private final static double ETH_PRICE = 390.0; //shortcut for pending transactions
    private long parsedTxCount = 0;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(100_000);
    private final UniswapPoolDecoder uniswapPoolDecoder = new UniswapPoolDecoder();

    public UniswapTransactionsParser(Web3Service web3Service) {
        this.web3Service = web3Service;
    }

    public void startParse() {
        log.info("Start parse transactions");
        web3Service.subscribeOn(transactions);
        new Thread(() -> {
            while (true) {
                Transaction transaction = null;
                try {
                    transaction = transactions.take();
                } catch (InterruptedException ignored) {
                }
                parseUniswapTransaction(transaction);
            }
        }).start();
    }

    void parseUniswapTransaction(Transaction tx) {
        incrementAndPrintCount(tx);
        if (!isValidTransaction(tx)) {
            return;
        }

        UniswapTx uniswapTx = decodeTransaction(tx);
        if (uniswapTx == null) {
            return;
        }

        Printable printable = uniswapTx.toPrintable(FARM_TOKEN_CONTRACT);
        calculateNotClearData(printable);
        saveLastPrice(printable);
        print(printable);
    }

    private void incrementAndPrintCount(Transaction tx) {
        parsedTxCount++;
        if (parsedTxCount % 10_000 == 0) {
            log.info("Parsed " + parsedTxCount + ", last block: " + tx.getBlockNumber());
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
        return UNI_ROUTER.equals(tx.getTo().toLowerCase());
    }

    private UniswapTx decodeTransaction(Transaction tx) {
        UniswapTx uniswapTx;
        try {
            uniswapTx = uniswapRouterDecoder.decodeInputData(tx);

            if (uniswapTx == null) {
                if (tx.getInput().length() > 70) {
                    log.error("tx not parsed " + tx.getHash());
                }
                return null;
            }

            if (!uniswapTx.isContainsAddress(FARM_TOKEN_CONTRACT)) {
                return null;
            }
            TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
            String status = transactionReceipt.getStatus();
            if (status.equals("0x1")) {
                uniswapTx.setSuccess(true);
                if (SWAP.equals(uniswapTx.getType())) {
                    uniswapPoolDecoder.enrichUniTx(uniswapTx, transactionReceipt.getLogs());
                }
            }

        } catch (Exception e) {
            log.error("Error tx " + tx.getHash(), e);
            return null;
        }
        return uniswapTx;
    }

    private void print(Printable printable) {
        if (printable.isConfirmed()) {
            log.info(printable.print() + " " + lastFarmPrice);
        } else {
            log.debug(printable.print() + " " + lastFarmPrice);
        }
    }

    private void calculateNotClearData(Printable printable) {
        if (!printable.isConfirmed() && lastFarmPrice != 0.0) {
            if (printable.getAmount() == 0.0) {
                if (printable.getEthAmount() != 0.0) {
                    printable.setAmount((printable.getEthAmount() * ETH_PRICE) / lastFarmPrice);
                } else {
                    printable.setOtherAmount(printable.getAmount() * lastFarmPrice);
                }
            } else if (printable.getOtherAmount() == 0.0) {
                if (printable.getEthAmount() != 0.0) {
                    printable.setOtherAmount(printable.getEthAmount() * ETH_PRICE);
                } else {
                    printable.setAmount(printable.getOtherAmount() / lastFarmPrice);
                }
            }
        }
    }

    private void saveLastPrice(Printable printable) {
        if (printable.isConfirmed() && "USDC".equals(printable.getOtherCoin())) {
            lastFarmPrice = printable.getOtherAmount() / printable.getAmount();
        }
    }

}
