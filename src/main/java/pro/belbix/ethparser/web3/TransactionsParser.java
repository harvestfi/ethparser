package pro.belbix.ethparser.web3;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.UniswapTx;

@Service
public class TransactionsParser {

    private static final Logger log = LoggerFactory.getLogger(TransactionsParser.class);
    public static final String UNI_ROUTER = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d";
    public static final String FARM_TOKEN_CONTRACT = "0xa0246c9032bc3a600820415ae600c6388619a14d";
    public static final String FARM_SUSHI_TOKEN_CONTRACT = "0x53df6664b3ddE086DCe6315c317d1002b14B87E3";
    private final UniswapEventDecoder uniswapEventDecoder = new UniswapEventDecoder();
    private final Web3Service web3Service;

    public TransactionsParser(Web3Service web3Service) {
        this.web3Service = web3Service;
    }

    @PostConstruct
    private void startParse() throws InterruptedException {
        log.info("Start parse transactions");
        while (true) {
            Transaction transaction = web3Service.getTransactions().take();
            parseUniswapTransaction(transaction);
        }
    }

    void parseUniswapTransaction(Transaction tx) {
        if (!UNI_ROUTER.equals(tx.getTo())) {
            return;
        }
//            log.info("Gotcha " + tx.getHash() + " " + tx.getInput());
//                    if(tx.get) //todo skip fails
        UniswapTx uniswapTx;
        try {
            uniswapTx = uniswapEventDecoder.decodeInputData(tx);
        } catch (Exception e) {
            log.error("" + e.getMessage());
            return;
        }
        if (uniswapTx == null) {
            log.error("tx not parsed " + tx.getHash());
            return;
        }

        if (uniswapTx.isContainsAddress(FARM_TOKEN_CONTRACT)) {
            log.info(uniswapTx.print(FARM_TOKEN_CONTRACT).print());
        }

    }

}
