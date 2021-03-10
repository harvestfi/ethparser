package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.TestUtils.numberFormat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RewardParserTest {

    @Autowired
    private RewardParser rewardParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;

    @Autowired
    private ContractLoader contractLoader;

    @BeforeEach
    public void setUp() {
        contractLoader.load();
        priceProvider.setUpdateBlockDifference(1);
        rewardParser.setWaitNewBlock(false);
    }

    @Test
    public void shouldParseRewardST_WETH() {
        parserTest(
            "0x3DA9D911301f8144bdF5c3c67886e5373DCdff8e",
            11778576,
            0,
            "0x94897c3575bdf2c715e8b8bb563b492b7342a4dd235f88eed9f804bf9d053728_19",
            "WETH",
            "108.54400000",
            1612897280
        );
    }

    @Test
    public void shouldParseRewardST_SUSHI_ETH_DAI() {
        parserTest(
            "0x76Aef359a33C02338902aCA543f37de4b01BA1FA",
            11733131,
            0,
            "0xe27248ecb7576f83b643b8a8a9d134bc41d481c1d1f336c966c5b1c00339c2fb_118",
            "SUSHI_ETH_DAI",
            "96,04999599",
            1612292480
        );
    }

    @Test
    public void shouldParseRewardST_WBTC() {
        parserTest(
            "0x917d6480Ec60cBddd6CbD0C8EA317Bcc709EA77B",
            11733131,
            0,
            "0xe27248ecb7576f83b643b8a8a9d134bc41d481c1d1f336c966c5b1c00339c2fb_91",
            "WBTC",
            "19,39506533",
            1612292480
        );
    }

    @Test
    public void shouldParseRewardDAI_BSG() {
        parserTest(
            "0xf5b221E1d9C3a094Fb6847bC3E241152772BbbF8",
            11662009,
            0,
            "0x58a005b6d7bb6534361076cda6c86c74ae4b43e753d67d2e276ac600ae8b0c5d_5",
            "DAI_BSG",
            "40,00045138",
            1611348622
        );
    }

    @Test
    public void shouldParseRewardPS() {
        parserTest(
            "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C",
            11434688,
            0,
            "0x05ea65f5a5954a1b7d7422b566164009cdbcfa90e099c5ea9b345de1156543fe_103",
            "PS",
            "681,97415761",
            1607816667
        );
    }

    private void parserTest(
        String contract,
        int onBlock,
        int logId,
        String id,
        String vault,
        String _reward,
        int period
    ) {
        String reward = numberFormat(_reward);
        List<LogResult> logResults = web3Service
            .fetchContractLogs(singletonList(contract), onBlock, onBlock);
        assertTrue("Log smaller then necessary", logId < logResults.size());
        try {
            RewardDTO dto = rewardParser.parseLog((Log) logResults.get(logId).get());

            assertNotNull("Dto is null", dto);
            assertAll(
                () -> assertEquals("id", id, dto.getId()),
                () -> assertEquals("vault", vault, dto.getVault()),
                () -> assertEquals("reward", reward, String.format("%.8f", dto.getReward())),
                () -> assertEquals("period", period, dto.getPeriodFinish())
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
