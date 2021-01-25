package pro.belbix.ethparser.web3.harvest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_NAME;
import static pro.belbix.ethparser.web3.harvest.parser.HardWorkParser.CONTROLLER;

import java.util.Collections;
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
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DoHardWorkTest {

    @Autowired
    private HardWorkParser hardWorkParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;
    @Autowired
    private HardWorkDbService hardWorkDbService;

    @Before
    public void setUp() throws Exception {
        priceProvider.setUpdateBlockDifference(1);
    }

    @Test
    public void parseDAI_BSGS() {
        HardWorkDTO dto = assertOnBlock(
            11698881,
            "0x640b6f13a78e17860e4bd0a111a0d1fe7d31cc73554c48963d3e5ae5aa0287ff_214",
            "DAI_BSGS",
            "0,000000",
            "16,773041",
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
    public void parseUSDC_V0_negative_share_change() {
        assertOnBlock(
            10772976,
            "0xc1b16dd8552046d08237d48d243f79308d1069b9f7c52ecf69088398f9dd673e_251",
            "USDC_V0",
            "-0,000405",
            "0,000000",
            "0,000000"
        );
    }

    @Test
    public void parseUSDC_V0() {
        assertOnBlock(
            10875947,
            "0x839df6bfc0e08d5ef6b516f9662e5f70807b2b416dcac142ac82fc1ff5048345_51",
            "USDC_V0",
            "0,000395",
            "27857,188467",
            "55,126335"
        );
    }

    @Test
    public void parseSUSHI_ETH_USDC() {
        assertOnBlock(
            11299287,
            "0xfaff1b27f5f17252bea0a2a1cc452f282fc644ec8ccfa963941f2c83ce6414aa_207",
            "SUSHI_ETH_USDC",
            "0,000073",
            "3059,633751",
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
            "4454,536837",
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
            "8256,895390",
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
            "19,726441",
            "8,454189"
        );
    }

    private HardWorkDTO assertOnBlock(int onBlock,
                                      String id,
                                      String vault,
                                      String sharePriceChange,
                                      String sharePriceUsd,
                                      String farmBuyback
    ) {
        List<LogResult> logResults = web3Service
            .fetchContractLogs(Collections.singletonList(CONTROLLER), onBlock, onBlock);
        assertNotNull(logResults);
        assertFalse(logResults.isEmpty());
        HardWorkDTO dto = hardWorkParser.parseLog((Log) logResults.get(0));
        assertNotNull(dto);
        assertAll(
            () -> assertEquals("id", id, dto.getId()),
            () -> assertEquals("vault", vault, dto.getVault()),
            () -> assertEquals("sharePriceChage", sharePriceChange, String.format("%f", dto.getShareChange())),
            () -> assertEquals("sharePriceUsd", sharePriceUsd, String.format("%f", dto.getShareChangeUsd())),
            () -> assertEquals("farmBuyback", farmBuyback, String.format("%f", dto.getFarmBuyback()))
        );

        double vaultRewardUsd = dto.getShareChangeUsd();
        if (vaultRewardUsd != 0) {
            double psReward = dto.getFarmBuyback();
            double farmPrice = priceProvider.getPriceForCoin(FARM_NAME, dto.getBlock());
            double psRewardUsd = psReward * farmPrice;
            double wholeRewardBasedOnPsReward = psRewardUsd / 0.3;
            double wholeRewardBasedOnVaultReward = vaultRewardUsd / 0.7;
            double diff =
                Math.abs(wholeRewardBasedOnPsReward - wholeRewardBasedOnVaultReward) / wholeRewardBasedOnPsReward;
            assertEquals("whole balance check", 0.0, diff, 1.0);
        }
        return dto;
    }

}
