package pro.belbix.ethparser;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.web3.Web3Functions;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class BobTest {

  @Autowired
  private Web3Functions web3Functions;

  @Test
  public void bobTest() {

    Transaction f = web3Functions
        .findTransaction(
            "0x51fffd6029ba1e5d5d607a5b62c4d84bb4b9d728458473e9ce84aa8fe16b3880",
            "eth");

    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList("0x33da6b1a05b4afcc5a321aacaa1334bda4345a14"),
            block, block,
            ETH_NETWORK);

    System.out.println();
  }
}
