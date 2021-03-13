package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
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
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultDecoder;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestVaultDecoderTest {

  @Autowired
  private ContractLoader contractLoader;

  @Autowired
  private Web3Service web3Service;
  private final HarvestVaultDecoder harvestVaultDecoder = new HarvestVaultDecoder();
  private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();

   @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }

  @Test
  @Disabled
  public void parseVault_WBTC() {
    Map<String, Integer> topics = new HashMap<>();
    List<LogResult> logResults = web3Service.fetchContractLogs(
        singletonList("0x5d9d25c7C457dD82fc8668FFC6B9746b674d4EcB"), 11164503, null);
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
            HarvestTx harvestTx;
            try {
                harvestTx = harvestVaultLogDecoder.decode(log);
            } catch (Exception e) {
                System.out.println("error with " + e.getMessage() + " " + log);
                e.printStackTrace();
                continue;
            }
            System.out.println(harvestTx);
        }

        topics.forEach((key, value) -> System.out.println(key + " " + value));
    }

    @Test
    public void parseTxFromVault_WBTC() throws IOException {
        Transaction transaction = web3Service
            .findTransaction("0x2c832ad9081512251dd172fc9de36bed1035649d51278b2a3bff501039885376");
        assertNotNull(transaction);
        HarvestTx harvestTx = (HarvestTx) harvestVaultDecoder.decodeInputData(transaction);
        assertNotNull(harvestTx);
    }

    @Test
    public void fetchLogTransaction() {
        TransactionReceipt receipt = web3Service.fetchTransactionReceipt(
            "0xc33899ec1de810b99071a2883e7f65300f1f1db5ca0987cc517f5e3a2551500d");
        assertNotNull(receipt);
    }
}
