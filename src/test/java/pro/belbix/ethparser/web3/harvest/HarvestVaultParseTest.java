package pro.belbix.ethparser.web3.harvest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.harvest.Vaults.WBTC;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.HarvestVaultDecoder;
import pro.belbix.ethparser.web3.harvest.HarvestVaultLogDecoder;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HarvestVaultParseTest {

    @Autowired
    private Web3Service web3Service;
    private final HarvestVaultDecoder harvestVaultDecoder = new HarvestVaultDecoder();
    private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();

    @Test
    @Ignore
    public void parseVault_WBTC() {
        Map<String, Integer> topics = new HashMap<>();
        List<LogResult> logResults = web3Service.fetchContractLogs(WBTC, DefaultBlockParameter
            .valueOf(new BigInteger("11164503")), LATEST);
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
            HarvestTx harvestTx = new HarvestTx();
            try {
                harvestVaultLogDecoder.enrichFromLog(harvestTx, log);
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
