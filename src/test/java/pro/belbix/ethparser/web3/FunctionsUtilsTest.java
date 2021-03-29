package pro.belbix.ethparser.web3;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import java.math.BigInteger;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.PROFITSHARING_DENOMINATOR;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class FunctionsUtilsTest {

  @Autowired
  private FunctionsUtils functionsUtils;
  @Autowired
  private ContractLoader contractLoader;

   @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }

  @Test
  public void testRewardToken() {
    String result = functionsUtils.callAddressByName(
        "rewardToken", "0xa81363950847aC250A2165D9Fb2513cA0895E786", 11616080L)
        .orElseThrow();
    assertNotNull(result);
    System.out.println("reward token " + result);
  }

  @Test
  public void testprofitSharingDenominator() {
    double result = functionsUtils.callIntByName(PROFITSHARING_DENOMINATOR, "0x9e315822a18f8d332782d1c3f3f24bb10d2161ad", 12086139L).orElse(BigInteger.ZERO).doubleValue();
    assertEquals(result, 30.0);
    System.out.println("profitSharingDenominator " + result);
  }
}
