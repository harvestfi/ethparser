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
    public static final String FARM_SUSHI_TOKEN_CONTRACT = "0x53df6664b3ddE086DCe6315c317d1002b14B87E3".toLowerCase();
    public static final String FARM_USDC_UNI_CONTRACT = "0x514906FC121c7878424a5C928cad1852CC545892".toLowerCase();
    public static final String FARM_WETH_UNI_CONTRACT = "0x56feAccb7f750B997B36A68625C7C596F0B41A58".toLowerCase();
    private final UniswapRouterDecoder uniswapRouterDecoder = new UniswapRouterDecoder();
    private final Web3Service web3Service;
    private double lastFarmPrice = 0.0; //todo shortcut
    private final static double LAST_ETH_PRICE = 390.0; //todo shortcut
    private long parsedTxCount = 0;
    private final BlockingQueue<Transaction> transactions = new ArrayBlockingQueue<>(10_000);
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
                } catch (InterruptedException e) {
                }
                parseUniswapTransaction(transaction);
            }
        }).start();
    }

    void parseUniswapTransaction(Transaction tx) {
        parsedTxCount++;
        if (parsedTxCount % 10_000 == 0) {
            log.info("Parsed " + parsedTxCount);
        }
        if (tx == null) {
            log.error("Null clear tx!");
            return;
        }
        if (tx.getTo() == null) {
            log.error("Null tx.To " + tx.getHash());
            return;
        }
        if (!UNI_ROUTER.equals(tx.getTo().toLowerCase())) {
            return;
        }

        UniswapTx uniswapTx;
        try {
            uniswapTx = uniswapRouterDecoder.decodeInputData(tx);

            if (uniswapTx == null) {
                if (tx.getInput().length() > 70) {
                    log.error("tx not parsed " + tx.getHash());
                }
                return;
            }

            if (!uniswapTx.isContainsAddress(FARM_TOKEN_CONTRACT)) {
                return;
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
            return;
        }

        Printable printable = uniswapTx.toPrintable(FARM_TOKEN_CONTRACT);
        if (printable.getAmount() == 0.0 && lastFarmPrice != 0.0) {
            if ("WETH".equals(printable.getOtherCoin())) {
                double farmAmount = (printable.getOtherAmount() * LAST_ETH_PRICE) / lastFarmPrice;
                printable.setAmount(farmAmount);
            } else {
                log.warn("not eth!");
            }
        } else {
            if ("USDC".equals(printable.getOtherCoin())) {
                lastFarmPrice = printable.getOtherAmount() / printable.getAmount();
            }
        }
        if (printable.isConfirmed()) {
            log.info(printable.print() + " " + lastFarmPrice);
        } else {
            log.debug(printable.print() + " " + lastFarmPrice);
        }

    }

}
