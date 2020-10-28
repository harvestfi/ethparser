package pro.belbix.ethparser.web3.uniswap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_TOKEN_CONTRACT;

import java.io.IOException;
import java.math.BigInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.Printable;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class UniswapTransactionsParserTest {

    private UniswapPoolDecoder uniswapPoolDecoder = new UniswapPoolDecoder();
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private UniswapTransactionsParser uniswapTransactionsParser;

    @Test
    public void shouldEnrichUniTxCorrect() {
        UniswapTx tx = new UniswapTx();
        tx.setCoinIn(new Address("0x0dafE2b22323dAc2940D43CeDe16cD8790C5D4e6"));
        tx.setCoinOut(new Address(FARM_TOKEN_CONTRACT));
        tx.setHash("0x266519b5e5756ea500d505afdfaa7d8cbb1fa0acc895fb9b9e6dbfefd3e7ce48");
        TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
        uniswapPoolDecoder.enrichUniTx(tx, transactionReceipt.getLogs());

        System.out.println(tx.toString());
        assertEquals(new BigInteger("3369976790396557"), tx.getAmountIn());
        assertEquals(new BigInteger("11966348304870486"), tx.getAmountOut());
    }

    @Test
    public void shouldEnrichUniTxCorrect_swapExactTokensForETH() {
        UniswapTx tx = new UniswapTx();
        tx.setCoinIn(new Address(FARM_TOKEN_CONTRACT));
        tx.setHash("0xa16181b55838897e58747c60c605e2b9c19866e82b5e8eab8d1b67d4b1c44039");
        TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
        uniswapPoolDecoder.enrichUniTx(tx, transactionReceipt.getLogs());
        System.out.println(tx.toString());
        assertEquals(new BigInteger("648788196931566749633"), tx.getAmountIn());
        assertEquals(new BigInteger("72961935183"), tx.getAmountOut());
    }

    @Test
    public void shouldEnrichUniTx_swapExactTokensForETH() {
        UniswapTx tx = new UniswapTx();
        tx.setCoinIn(new Address(FARM_TOKEN_CONTRACT));
        tx.setHash("0xd0c2a327772fcb4894688b4528909d98095ea77123719718d639dbd00cc11b41");
        TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
        uniswapPoolDecoder.enrichUniTx(tx, transactionReceipt.getLogs());
        System.out.println(tx.toString());
        assertEquals(new BigInteger("850000000000000000"), tx.getAmountIn());
        assertEquals(new BigInteger("92415767"), tx.getAmountOut());
    }



    @Test
    public void parseUniswapTransactionTest() throws IOException {
        Transaction tx = web3Service.findTransaction("0xd0c2a327772fcb4894688b4528909d98095ea77123719718d639dbd00cc11b41");
        uniswapTransactionsParser.parseUniswapTransaction(tx);
    }

    @Test
    public void parseUniswapTransactionTest_swapTokensForExactETH() throws IOException, InterruptedException {
        Transaction tx = web3Service.findTransaction("0x5026a67dd0577b0370f483a48d0869d6adca68e5a0339443fcadd3644a9a9e32");
        Printable printable = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertEquals("FARM", printable.getCoin());
        assertEquals("USDC", printable.getOtherCoin());
        assertEquals(55.440214001258916, printable.getAmount(), 0.0);
        assertEquals(5851.100961, printable.getOtherAmount(), 0.0);
    }

    @Test
    public void parseUniswapTransaction_swapTokensForExactTokens() throws IOException {
        Transaction tx = web3Service.findTransaction("0x5a9d8fa3fb5097ba4a75aad475497fab49b67efb31f3dc248e66fdab578b6208");
        Printable printable = uniswapTransactionsParser.parseUniswapTransaction(tx);
        assertFalse(printable.isConfirmed());
        assertEquals("FARM", printable.getCoin());
        assertEquals("USDC", printable.getOtherCoin());
        assertEquals(1.0121780834180945, printable.getAmount(), 0.0);
        assertEquals(1.0, printable.getOtherAmount(), 0.0);
    }
}
