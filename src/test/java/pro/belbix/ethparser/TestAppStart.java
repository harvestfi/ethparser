package pro.belbix.ethparser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.harvest.parser.RewardParser;

@SpringBootTest(classes = Application.class)
public class TestAppStart {

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private RewardParser rewardParser;

    @Test
    public void startTest() throws InterruptedException {
        rewardParser.setWaitNewBlock(false);
        if (appProperties.isDevMod()) {
            Application.main(new String[]{});
        }
        while (appProperties.isDevMod()) {
            Thread.sleep(1000);
        }
    }
}
