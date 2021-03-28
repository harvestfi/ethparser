package pro.belbix.ethparser.web3.harvest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.numberFormat;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLER;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class DoHardWorkTest {

    @Autowired
    private HardWorkParser hardWorkParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;
    @Autowired
    private HardWorkDbService hardWorkDbService;
    @Autowired
    private ContractLoader contractLoader;

    @BeforeEach
    public void setUp() throws Exception {
        contractLoader.load();
        priceProvider.setUpdateBlockDifference(1);
    }

    @Test
    public void parseSUSHI_ETH_DAI() {
        HardWorkDTO dto = assertOnBlock(
            12064923,
            "0x1ce56c85fee98049f2c957b38b72a51925434771303843fc0b0212a4c1ccd0a1_164",
            "SUSHI_ETH_DAI",
            "0.000894",
            "15589.709554",
            "30.101816"
        );
        assertNotNull(dto);
        hardWorkDbService.enrich(dto);
        assertAll(
            () -> assertEquals("Farm buyback sum", "60.993959",
                String.format("%f", dto.getFarmBuybackSum()))
        );
    }

    @Test
    public void parseMIS_USDT() {
        HardWorkDTO dto = assertOnBlock(
            11933706,
            "0x0c9c9faabb9db06667ee0f3f59703e6aec0ee099dea466bd84770d6e13149e7b_40",
            "SUSHI_MIC_USDT",
            "0,000000",
            "17800.133375",
            "24,508470"
        );
    }

    @Test
    public void parseDAI_BSGS() {
        HardWorkDTO dto = assertOnBlock(
            11698881,
            "0x640b6f13a78e17860e4bd0a111a0d1fe7d31cc73554c48963d3e5ae5aa0287ff_214",
            "DAI_BSGS",
            "0,000000",
            "2696,891131",
            "7,188446"
        );
        assertNotNull(dto);
        hardWorkDbService.enrich(dto);
//        assertAll(
//            () -> assertEquals("Farm buyback sum", "49547,177326", String.format("%f", dto.getFarmBuybackSum()))
//        );
    }

    @Test
    public void parseSUSHI_MIC_USDT_without_profit() {
        assertOnBlock(
            11615905,
            "0x5f1a8712f06d8ab408a21d47ddebd509ec0867001b40ac1f811a6b718a81a496_16",
            "SUSHI_MIC_USDT",
            "0,000000",
            "0,000000",
            "0,000000"
        );
    }

    @Test
    public void parseSUSHI_ETH_USDC() {
        assertOnBlock(
            11299287,
            "0xfaff1b27f5f17252bea0a2a1cc452f282fc644ec8ccfa963941f2c83ce6414aa_207",
            "SUSHI_ETH_USDC",
            "0,000073",
            "4278,343750",
            "10,740319"
        );
    }

    @Test
    public void parseUSDC() {
        assertOnBlock(
            11490124,
            "0xb63ea164db21279e9b4a09975ca5e3313cf897cf7118e733ca81708719c87b91_190",
            "USDC",
            "0,000097",
            "6628,467329",
            "16,849470"
        );
    }

    @Test
    public void parseYCRV() {
        assertOnBlock(
            11687882,
            "0x25aed6e95e2ebb3bd66ca906bcb59cb762a286a273c1ce67052701697e4b2bc5_114",
            "YCRV",
            "0,000265",
            "12574,560831",
            "32,585083"
        );
    }

    @Test
    public void parseDAI_BSGS2() {
        assertOnBlock(
            11724037,
            "0x054b92d6bc7e846f40a4aea5a99f3ba77941be41107fad9a72e14d68a407148f_79",
            "DAI_BSGS",
            "0,000000",
            "3673,968759",
            "8,454189"
        );
    }

    @Test
    public void parseSUSHI_ETH_USDC2() {
        assertOnBlock(
            11425987,
            "0xcaf98b04b4d779899e29d36833fc52955b2c887d62bd4b4c277abd998b4e355f_138",
            "SUSHI_ETH_USDC",
            "0,000271",
            "16811,047116",
            "49,378887"
        );
    }

    @Test
    public void parseUNI_ETH_DAI_broken_when_all_profit_went_to_PS() {
        assertOnBlock(
            11058343,
            "0x4a51db032f01770824b42f70d503f013a9a842cb4041836556bfe17aec185d03_159",
            "UNI_ETH_DAI",
            "0,000000",
            "1669,580492",
            "2,804286"
        );
    }

    private HardWorkDTO assertOnBlock(int onBlock,
        String id,
        String vault,
        String _sharePriceChange,
        String _fullRewardUsd,
        String _farmBuyback
    ) {
        String sharePriceChange = numberFormat(_sharePriceChange);
        String fullRewardUsd = numberFormat(_fullRewardUsd);
        String farmBuyback = numberFormat(_farmBuyback);
        List<LogResult> logResults = web3Service
            .fetchContractLogs(Collections.singletonList(CONTROLLER), onBlock, onBlock);
        assertNotNull(logResults);
        assertFalse(logResults.isEmpty());
        HardWorkDTO dto = hardWorkParser.parseLog((Log) logResults.get(0));
        assertNotNull(dto);
        assertAll(
            () -> assertEquals("id", id, dto.getId()),
            () -> assertEquals("vault", vault, dto.getVault()),
            () -> assertEquals("sharePriceChage", sharePriceChange,
                String.format("%f", dto.getShareChange())),
            () -> assertEquals("full reward Usd", fullRewardUsd, String.format("%f", dto.getFullRewardUsd())),
            () -> assertEquals("farmBuyback", farmBuyback, String.format("%f", dto.getFarmBuyback()))
        );

        if (dto.getFullRewardUsd() != 0) {
            double psReward = dto.getFarmBuyback();
            double psRewardUsd = psReward * dto.getFarmPrice();

            double wholeRewardBasedOnPsReward = psRewardUsd / dto.getProfitSharingRate();
            double wholeRewardBasedOnVaultReward = dto.getFullRewardUsd() / (1-dto.getProfitSharingRate());
            // when price volatile we can by less % value for the strategy income
            double diff =
                Math.abs(wholeRewardBasedOnPsReward - wholeRewardBasedOnVaultReward)
                    / wholeRewardBasedOnPsReward;
            assertEquals("% diff balance check", 0.0, diff, 2.0);
        }
        return dto;
    }

}
