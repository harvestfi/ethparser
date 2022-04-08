package pro.belbix.ethparser.web3;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.Log;

public class SimpleDecoderTest {

  SimpleDecoder decoder = new SimpleDecoder();


  @Test
  public void decodeUniswapV3EthLogTest_deposit() {
    var log = new Log();
    log.setTopics(List.of(
        "0x90890809c654f11d6e72a28fa60149770a0d11ec6c92319d6ceb2bb0a4ea1a15",
        "0x000000000000000000000000b45844826d2757e5bc43518c084e9774d6715f4a"
    ));
    log.setData("0x00000000000000000000000000000000000000000000005053b0b935131f65ee00000000000000000000000000000000000000000000000000000000578f2940");

    var result = decoder.decodeEthLogForDepositAndWithdraw(log).orElseThrow();

    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(3);
    assertThat(result.get(2).getValue()).isEqualTo(BigInteger.valueOf(1469000000));
  }

  // txHash = 0x318bd2dff3c071b883cbaee68bde0a306f704c445ceea404916546fbc456199b
  @Test
  public void decodeOnlyTopics_deposit() {
    var log = new Log();
    log.setTopics(List.of(
        "0xe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c",
        "0x0000000000000000000000008369e7900ff2359bb36ef1c40a60e5f76373a6ed",
        "0x0000000000000000000000000000000000000000000000018493fba64ef00000"
    ));
    log.setData("");

    var result = decoder.decodeOnlyTopics(log);
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(1).getValue()).isEqualTo(new BigInteger("28000000000000000000"));
  }
}
