package pro.belbix.ethparser.web3;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class FunctionsUtilsTest {

    @Autowired
    private FunctionsUtils functionsUtils;

    @Test
    public void testRewardToken() {
        String result = functionsUtils.callAddressByName(
            "rewardToken", "0xa81363950847aC250A2165D9Fb2513cA0895E786", 11616080L)
            .orElseThrow();
        assertNotNull(result);
        System.out.println("reward token " + result);
    }
}
