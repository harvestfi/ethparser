package pro.belbix.ethparser.web3.harvest.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.web3.Web3Functions;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.TestUtils.numberFormat;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RewardParserTest {

    @Autowired
    private RewardParser rewardParser;
    @Autowired
    private Web3Functions web3Functions;

    @BeforeEach
    public void setUp() {
        rewardParser.setWaitNewBlock(false);
    }

    @Test
    public void shouldParseReward_ST_UNI_WBTC_KLON() throws Exception {
        assertModel(RewardDTO.builder()
                        .id("0xf310afbf5090cb8dc7834760029f7dd692069514adde0c1ff3bd71161f6337d3_167")
                        .vault("UNI_WBTC_KlonX")
                        .vaultAddress("0xb4e3fc276532f27bd0f738928ce083a3b064ba61")
                        .poolAddress("0x719d70457658358f2e785b38307cfe24071b7417")
                        .block(12014122)
                        .blockDate(1615423085)
                        .network("eth")
                        .reward(110.79433213950709)
                        .periodFinish(1616027885)
                        .apy(0.0)
                        .weeklyApy(0)
                        .tvl(0)
                        .farmBalance(184.5208303250852)
                        .isWeeklyReward(0)
                        .build(),
                parseDto("0x719d70457658358f2e785b38307cfe24071b7417",
                        12014122, 0));
    }

    @Test
    public void shouldParseRewardST_WETH() throws Exception {
        assertModel(RewardDTO.builder()
                        .id("0x94897c3575bdf2c715e8b8bb563b492b7342a4dd235f88eed9f804bf9d053728_19")
                        .vault("WETH_#V1")
                        .vaultAddress("0xfe09e53a81fe2808bc493ea64319109b5baa573e")
                        .poolAddress("0x3da9d911301f8144bdf5c3c67886e5373dcdff8e")
                        .block(11778576)
                        .blockDate(1612292480)
                        .network("eth")
                        .reward(108.54399999999971)
                        .periodFinish(1612897280)
                        .apy(0.0)
                        .weeklyApy(0)
                        .tvl(0)
                        .farmBalance(175.68799034993148)
                        .isWeeklyReward(1)
                        .build(),
                parseDto("0x3DA9D911301f8144bdF5c3c67886e5373DCdff8e",
                        11778576, 0));
    }

    @Test
    public void shouldParseRewardST_SUSHI_ETH_DAI() throws Exception {
        assertModel(RewardDTO.builder()
                        .id("0xe27248ecb7576f83b643b8a8a9d134bc41d481c1d1f336c966c5b1c00339c2fb_118")
                        .vault("SUSHI_DAI_WETH")
                        .vaultAddress("0x203e97aa6eb65a1a02d9e80083414058303f241e")
                        .poolAddress("0x76aef359a33c02338902aca543f37de4b01ba1fa")
                        .block(11733131)
                        .blockDate(1611687680)
                        .network("eth")
                        .reward(96.0499959904417)
                        .periodFinish(1612292480)
                        .apy(0.0)
                        .weeklyApy(0)
                        .tvl(0)
                        .farmBalance(135.73124832210462)
                        .isWeeklyReward(1)
                        .build(),
                parseDto("0x76Aef359a33C02338902aCA543f37de4b01BA1FA",
                        11733131, 0));
    }

    @Test
    public void shouldParseRewardST_WBTC() throws Exception {
        assertModel(RewardDTO.builder()
                        .id("0xe27248ecb7576f83b643b8a8a9d134bc41d481c1d1f336c966c5b1c00339c2fb_91")
                        .vault("WBTC")
                        .vaultAddress("0x5d9d25c7c457dd82fc8668ffc6b9746b674d4ecb")
                        .poolAddress("0x917d6480ec60cbddd6cbd0c8ea317bcc709ea77b")
                        .block(11733131)
                        .blockDate(1611687680)
                        .network("eth")
                        .reward(19.395065329757912)
                        .periodFinish(1612292480)
                        .apy(0.0)
                        .weeklyApy(0)
                        .tvl(0)
                        .farmBalance(43.76826635680345)
                        .isWeeklyReward(1)
                        .build(),
                parseDto("0x917d6480Ec60cBddd6CbD0C8EA317Bcc709EA77B",
                        11733131, 0));
    }

    @Test
    public void shouldParseRewardDAI_BSG() throws Exception {
        assertModel(RewardDTO.builder()
                        .id("0x58a005b6d7bb6534361076cda6c86c74ae4b43e753d67d2e276ac600ae8b0c5d_5")
                        .vault("UNI_DAI_BSG_#V1")
                        .vaultAddress("0x639d4f3f41daa5f4b94d63c2a5f3e18139ba9e54")
                        .poolAddress("0xf5b221e1d9c3a094fb6847bc3e241152772bbbf8")
                        .block(11662009)
                        .blockDate(1610743822)
                        .network("eth")
                        .reward(40.00045137784376)
                        .periodFinish(1611348622)
                        .apy(0)
                        .weeklyApy(0.0)
                        .tvl(0.0)
                        .farmBalance(40.000458966205365)
                        .isWeeklyReward(0)
                        .build(),
                parseDto("0xf5b221E1d9C3a094Fb6847bC3E241152772BbbF8",
                        11662009, 0));
    }

    @Test
    public void shouldParseRewardPS() throws Exception {
        assertModel(RewardDTO.builder()
                .id("0x05ea65f5a5954a1b7d7422b566164009cdbcfa90e099c5ea9b345de1156543fe_103")
                .vault("PS_#V1")
                .vaultAddress("0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50")
                .poolAddress("0x8f5adc58b32d4e5ca02eac0e293d35855999436c")
                .block(11434688)
                .blockDate(1607730267)
                .network("eth")
                .reward(681.9741576131219)
                .periodFinish(1607816667)
                .apy(0)
                .weeklyApy(0)
                .tvl(0)
                .farmBalance(217790.04080427627)
                .isWeeklyReward(0)
                .build(),
        parseDto("0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C",
                11434688, 0));
    }

    private RewardDTO parseDto(
            String contract,
            int onBlock,
            int logId
    ) {
        List<LogResult> logResults = web3Functions
                .fetchContractLogs(singletonList(contract), onBlock, onBlock, ETH_NETWORK);
        Assertions.assertTrue(logId < logResults.size(), "Log smaller then necessary");

        return rewardParser.parse((Log) logResults.get(logId).get(), ETH_NETWORK);

    }
}
