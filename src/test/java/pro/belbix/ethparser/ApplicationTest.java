package pro.belbix.ethparser;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser;
import pro.belbix.ethparser.ws.WsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class ApplicationTest {

    @Autowired
    private Web3Service web3Service;
    @Autowired
    private UniswapTransactionsParser uniswapTransactionsParser;
    @Autowired
    private WsService wsService;

    @Test
    @Ignore
    public void startSubscription() throws InterruptedException {
        Application.startParse(web3Service, uniswapTransactionsParser, wsService);
    }
}
