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
}
