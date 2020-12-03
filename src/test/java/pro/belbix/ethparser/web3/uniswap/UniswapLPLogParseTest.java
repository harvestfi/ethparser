package pro.belbix.ethparser.web3.uniswap;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.uniswap.UniswapLpLogDecoder.FARM_ETH_LP_CONTRACT;
import static pro.belbix.ethparser.web3.uniswap.UniswapLpLogDecoder.FARM_USDC_LP_CONTRACT;
import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_TOKEN_CONTRACT;

import java.math.BigInteger;
import java.time.Instant;
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
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class UniswapLPLogParseTest {

    @Autowired
    private Web3Service web3Service;
    private final UniswapLpLogDecoder uniswapLpLogDecoder = new UniswapLpLogDecoder();

    @Test
    @Ignore
    public void parseFarmLp() {
        parseLp(FARM_USDC_LP_CONTRACT, 11165610, null);
    }

    @Test
    @Ignore
    public void parseEthFarmLp() {
        parseLp(FARM_ETH_LP_CONTRACT, 11165610, null);
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
                UniswapDTO dto = tx.toDto(FARM_TOKEN_CONTRACT);
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
