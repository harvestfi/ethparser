package pro.belbix.ethparser.web3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.PROFITSHARING_NUMERATOR;

import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

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
        "rewardToken", "0xa81363950847aC250A2165D9Fb2513cA0895E786", 11616080L, ETH_NETWORK)
        .orElseThrow();
    assertNotNull(result, "reward token ");
  }

  @Test
  public void testprofitSharingDenominator() {
    double result = functionsUtils.callIntByName(PROFITSHARING_NUMERATOR, "0x9e315822a18f8d332782d1c3f3f24bb10d2161ad", 12086139L).orElse(BigInteger.ZERO).doubleValue();
    assertEquals(30.0, result, "PROFITSHARING_NUMERATOR");
  }

  @Test
  public void testBoolByName() {
    Boolean result = functionsUtils.callBoolByName("liquidateRewardToWethInSushi", "0x636A37802dA562F7d562c1915cC2A948A1D3E5A0", 11694023L).orElse(null);
    assertEquals(true, result, "liquidateRewardToWethInSushi");
  }
}
