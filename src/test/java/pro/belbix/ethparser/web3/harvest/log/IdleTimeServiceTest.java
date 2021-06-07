package pro.belbix.ethparser.web3.harvest.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class IdleTimeServiceTest {

  @Autowired
  private IdleTimeService idleTimeService;


  @Test
  public void ethVaultTest() {
    var res = idleTimeService.getLastEventBlockDate(
        ETH_NETWORK, "0x00000000000000000000000001bd09a1124960d9be04b638b142df9df942b04a",
        12564067);
    // https://etherscan.io/tx/0xb771e148668184d85df2c18f7382d809facca5badcb35a7926e3091488449efe
    // Jun-03-2021 09:58:24 PM +UTC
    assertEquals(1622754691, res);
  }

  @Test
  public void bscVaultTest() {
    var res = idleTimeService.getLastEventBlockDate(
        BSC_NETWORK, "0x0000000000000000000000006d386490e2367fc31b4acc99ab7c7d4d998a3121",
        7985745);
    //https://bscscan.com/tx/0xb2c1a8c8975281abbe90bc8746a26297dbd0b89d188bfc2758958d3c85ead6e3
    //Jun-03-2021 09:59:06 PM +UTC
    assertEquals(1622757546, res);
  }
}
