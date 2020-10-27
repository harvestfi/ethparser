package pro.belbix.ethparser.web3.uniswap;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class UniswapTransactionsParserTest {

    private UniswapPoolDecoder uniswapPoolDecoder = new UniswapPoolDecoder();
    @Autowired
    private Web3Service web3Service;

    @Test
    public void shouldEnrichUniTxCorrect() {
        UniswapTx tx = new UniswapTx();
        tx.setHash("0x266519b5e5756ea500d505afdfaa7d8cbb1fa0acc895fb9b9e6dbfefd3e7ce48");
        TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
        uniswapPoolDecoder.enrichUniTx(tx, transactionReceipt.getLogs());

        System.out.println(tx.toString());
        assertEquals(new BigInteger("3369976790396557"), tx.getAmountIn());
        assertEquals(new BigInteger("11966348304870486"), tx.getAmountOut());
    }

    @Test
    public void shouldEnrichUniTxCorrect2() {
        UniswapTx tx = new UniswapTx();
        tx.setHash("0xa16181b55838897e58747c60c605e2b9c19866e82b5e8eab8d1b67d4b1c44039");
        TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
        uniswapPoolDecoder.enrichUniTx(tx, transactionReceipt.getLogs());
        System.out.println(tx.toString());
        assertEquals(new BigInteger("72961935183"), tx.getAmountIn());
        assertEquals(new BigInteger("184601190385645437377"), tx.getAmountOut());
    }
}
