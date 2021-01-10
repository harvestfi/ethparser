package pro.belbix.ethparser.web3;

import static org.junit.Assert.assertNotNull;
import static pro.belbix.ethparser.web3.ContractConstants.D18;

import java.math.BigInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class FunctionsTest {

    @Autowired
    private Functions functions;

    @Test
    public void testSellFloor() {
        BigInteger floor = functions.callSellFloor("0xa81363950847ac250a2165d9fb2513ca0895e786", 11616080L);
        assertNotNull(floor);
        double r = floor.doubleValue() / D18;
        System.out.println("floor " + r);
    }

    @Test
    public void testRewardToken() {
        String result = functions.callRewardToken("0xa81363950847aC250A2165D9Fb2513cA0895E786", 11616080L);
        assertNotNull(result);
        System.out.println("reward token " + result);
    }

    @Test
    public void testRewardPool() {
        String result = functions.callRewardPool("0xa81363950847aC250A2165D9Fb2513cA0895E786", 11616080L);
        assertNotNull(result);
        System.out.println("reward pool " + result);
        System.out.println("t " + 285462361296456991989.0 / D18);
    }
}
