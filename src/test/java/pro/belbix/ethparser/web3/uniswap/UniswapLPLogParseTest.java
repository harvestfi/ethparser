package pro.belbix.ethparser.web3.uniswap;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.uniswap.decoder.UniswapLpLogDecoder;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class UniswapLPLogParseTest {

  @Autowired
  private Web3Service web3Service;
  @Autowired
  private ContractLoader contractLoader;
  private final UniswapLpLogDecoder uniswapLpLogDecoder = new UniswapLpLogDecoder();

  @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }

  @Test
  @Disabled
  public void parseUNI_LP_USDC_FARM() {
    parseLp("0x514906fc121c7878424a5c928cad1852cc545892", 11165610, null);
  }

  @Test
  @Disabled
  public void parseEthFarmLp() {
    parseLp("0x56feaccb7f750b997b36a68625c7c596f0b41a58", 11165610, null);
  }

  @Test
  @Disabled
  public void parseBtcBadgerLp() {
    parseLp("0xcd7989894bc033581532d2cd88da5db0a4b12859", 11381099, 11382099);
  }

  private void parseLp(String contract, Integer start, Integer end) {
    Map<String, Integer> topics = new HashMap<>();
    List<LogResult> logResults = web3Service
        .fetchContractLogs(singletonList(contract), start, end);
    assertFalse(logResults.isEmpty());
    for (LogResult logResult : logResults) {
            Log log = (Log) logResult.get();
            assertFalse(log.getTopics().isEmpty());
            String topic0 = log.getTopics().get(0);
            if (topics.containsKey(topic0)) {
                topics.put(topic0, topics.get(topic0) + 1);
            } else {
                topics.put(topic0, 1);
            }
            UniswapTx tx = new UniswapTx();
            try {
                uniswapLpLogDecoder.decode(tx, log);
                if (tx.getHash() == null) {
                    continue;
                }
                UniswapDTO dto = tx.toDto();
                dto.setBlockDate(Instant.now().getEpochSecond());
                System.out.println(dto.print());
            } catch (Exception e) {
                System.out.println("error with " + e.getMessage() + " " + log);
                e.printStackTrace();
            }
        }

        topics.forEach((key, value) -> System.out.println(key + " " + value));
    }

}
