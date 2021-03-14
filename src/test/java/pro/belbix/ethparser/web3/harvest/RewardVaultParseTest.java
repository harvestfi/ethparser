package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.AppConfig;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RewardVaultParseTest {

  @Autowired
  private Web3Service web3Service;
  @Autowired
  private ContractLoader contractLoader;

  private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();

   @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }

  @Test
  @Disabled
  public void ycrvTest() {
    List<LogResult> logResults = web3Service
        .fetchContractLogs(singletonList("0x6D1b6Ea108AA03c6993d8010690264BA96D349A8"), null, null);
    for (LogResult logResult : logResults) {
      Log ethLog = (Log) logResult.get();
      HarvestTx tx = harvestVaultLogDecoder.decode(ethLog);
      if ("RewardAdded".equals(tx.getMethodName())) {
                System.out.println(ethLog);
            }
        }
    }

    @Test
    public void ycrvTest_RewardDenied() {
        List<LogResult> logResults = web3Service
            .fetchContractLogs(singletonList("0x6D1b6Ea108AA03c6993d8010690264BA96D349A8"), 11413701, 11413701);
        for (LogResult logResult : logResults) {
            Log ethLog = (Log) logResult.get();
            harvestVaultLogDecoder.decode(ethLog);
        }
    }
}
