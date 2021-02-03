package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.contracts.StakeContracts.ST_DAI_BSG;
import static pro.belbix.ethparser.web3.contracts.StakeContracts.ST_PS;
import static pro.belbix.ethparser.web3.contracts.StakeContracts.ST_SUSHI_ETH_DAI;
import static pro.belbix.ethparser.web3.contracts.StakeContracts.ST_WBTC;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.web3.prices.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class RewardParserTest {

    @Autowired
    private RewardParser rewardParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;

    @Before
    public void setUp() {
        priceProvider.setUpdateBlockDifference(1);
        rewardParser.setWaitNewBlock(false);
    }

    @Test
    public void shouldParseRewardST_SUSHI_ETH_DAI() {
        parserTest(
            ST_SUSHI_ETH_DAI,
            11733131,
            0,
            "SUSHI_ETH_DAI",
            "96,04999599",
            1612292480
        );
    }

    @Test
    public void shouldParseRewardST_WBTC() {
        parserTest(
            ST_WBTC,
            11733131,
            0,
            "WBTC",
            "19,39506533",
            1612292480
        );
    }

    @Test
    public void shouldParseRewardDAI_BSG() {
        parserTest(
            ST_DAI_BSG,
            11662009,
            0,
            "DAI_BSG",
            "40,00045138",
            1611348622
        );
    }

    @Test
    public void shouldParseRewardPS() {
        parserTest(
            ST_PS,
            11434688,
            0,
            "PS",
            "681,97415761",
            1607816667
        );
    }

    private void parserTest(
        String contract,
        int onBlock,
        int logId,
        String vault,
        String reward,
        int period
    ) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), onBlock, onBlock);
        assertTrue("Log smaller then necessary", logId < logResults.size());
        RewardDTO dto = null;
        try {
            dto = rewardParser.parseLog((Log) logResults.get(logId).get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertDto(dto, vault, reward, period);
    }

    private void assertDto(RewardDTO dto, String vault, String reward, int period) {
        assertNotNull("Dto is null", dto);
        assertAll(
            () -> assertEquals("vault", vault, dto.getVault()),
            () -> assertEquals("reward", reward, String.format("%.8f", dto.getReward())),
            () -> assertEquals("period", period, dto.getPeriodFinish())
        );
    }
}
