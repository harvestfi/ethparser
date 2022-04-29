package pro.belbix.ethparser.web3.abi;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class FunctionsUtilsTest {

  @Autowired
  FunctionsUtils functionsUtils;

  @Test
  void callReserves_cyber_with_two_outputParam() {
    var result = functionsUtils.callReserves("0xa0fb4487c0935f01cbf9f0274fe3cdb21a965340", null, "matic");
    assertThat(result).isNotNull();
  }

  @Test
  void callReserves_uniSwap_with_three_outputParam() {
    var result = functionsUtils.callReserves("0x853Ee4b2A13f8a742d64C8F088bE7bA2131f670d", null, "matic");
    assertThat(result).isNotNull();
  }
 }
