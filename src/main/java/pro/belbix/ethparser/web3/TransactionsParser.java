package pro.belbix.ethparser.web3;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.Printable;
import pro.belbix.ethparser.model.UniswapTx;

@Service
public class TransactionsParser {

    private static final Logger log = LoggerFactory.getLogger(TransactionsParser.class);
    public static final String UNI_ROUTER = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d";
    public static final String FARM_TOKEN_CONTRACT = "0xa0246c9032bc3a600820415ae600c6388619a14d";
    public static final String FARM_SUSHI_TOKEN_CONTRACT = "0x53df6664b3ddE086DCe6315c317d1002b14B87E3";
    public static final String FARM_UNI_CONTRACT = "0x514906FC121c7878424a5C928cad1852CC545892".toLowerCase();
    private final UniswapEventDecoder uniswapEventDecoder = new UniswapEventDecoder();
    private final Web3Service web3Service;
    private double lastFarmPrice = 0.0; //todo shortcut
    private double lastEthPrice = 390.0; //todo shortcut
    private long parsedTxCount = 0;

    public TransactionsParser(Web3Service web3Service) {
        this.web3Service = web3Service;
    }

    public void startParse() {
        log.info("Start parse transactions");
        new Thread(() -> {
            while (true) {
                Transaction transaction = null;
                try {
                    transaction = web3Service.getTransactions().take();
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
        if (!UNI_ROUTER.equals(tx.getTo())) {
            return;
        }
//            log.info("Gotcha " + tx.getHash() + " " + tx.getInput());
//                    if(tx.get) //todo skip fails
        UniswapTx uniswapTx;
        try {
            uniswapTx = uniswapEventDecoder.decodeInputData(tx);
        } catch (Exception e) {
            log.error("Tx hash: " + tx.getHash(), e);
            return;
        }
        if (uniswapTx == null) {
            if (tx.getInput().length() > 70) {
                log.error("tx not parsed " + tx.getHash());
            }
            return;
        }

        if (uniswapTx.isContainsAddress(FARM_TOKEN_CONTRACT)) {
            Printable printable = uniswapTx.print(FARM_TOKEN_CONTRACT);
            if (printable.getAmount() == 0.0 && lastFarmPrice != 0.0) {
                if ("WETH".equals(printable.getOtherCoin())) {
                    double farmAmount = (printable.getOtherAmount() * lastEthPrice) / lastFarmPrice;
                    printable.setAmount(farmAmount);
                } else {
                    log.warn("not eth!");
                }
            } else {
                if ("USDC".equals(printable.getOtherCoin())) {
                    lastFarmPrice = printable.getOtherAmount() / printable.getAmount();
                }
            }
            log.info(printable.print() + " " + lastFarmPrice);
        }
    }

    public void fetchEvents(Printable printable) {
        web3Service.fetchEvents(printable.getHash());
    }

}
