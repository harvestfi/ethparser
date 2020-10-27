package pro.belbix.ethparser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class ApplicationTest {

    @Autowired
    private Web3Service web3Service;
    @Autowired
    private UniswapTransactionsParser uniswapTransactionsParser;

    @Test
    public void startSubscription() throws InterruptedException {
        web3Service.subscribeTransactionFlowable();
        uniswapTransactionsParser.startParse();
        while (true) {
            Thread.sleep(1000);
        }
    }
}
