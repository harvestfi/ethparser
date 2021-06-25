package pro.belbix.ethparser.web3.harvest.hardwork;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractUtils.getDoHardWork;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class DoHardWorkBscTest {

    @Autowired
    private HardWorkParser hardWorkParser;
    @Autowired
    private Web3Functions web3Functions;
    @Autowired
    private HardWorkDbService hardWorkDbService;

    private final Set<String> excludeFields = Set.of(
        "fullRewardUsdTotal",
        "allProfit",
        "psPeriodOfWork",
        "weeklyProfit",
        "weeklyAllProfit",
        "farmBuybackSum",
        "callsQuantity",
        "poolUsers",
        "savedGasFeesSum",
        "weeklyAverageTvl"
    );

    @Test
    @Disabled("Returns missing trie node error")
    public void parse_ONEINCH_BNB() throws Exception {
        HardWorkDTO dto = loadHardWork(6607654);
        assertModel(
            HardWorkDTO.builder()
                .id("0x9ec4b00e31191921f3b5802a875bf778e95f563f435889f7756739028fa6475e_174")
                .vault("ONEINCH_BNB")
                .block(6607654)
                .blockDate(1618551253)
                .shareChange(6.0936797266229E-4)
                .farmBuyback(59.379568222719335)
                .farmBuybackEth(0.30508008675474335)
                .fullRewardUsd(742.2446027839917)
                .callsQuantity(1)
                .fee(6.1353169576909306)
                .weeklyAverageTvl(null)
                .feeEth(0.01187918)
                .gasUsed(1187918.0)
                .invested(100.0)
                .investmentTarget(100.0)
                .ethPrice(2432.9500187296358)
                .profitSharingRate(0.08)
                .build(),
            dto, excludeFields
        );
    }

    @Test
    public void parse_VENUS_VAI() throws Exception {
        HardWorkDTO dto = loadHardWork(6168762);
        assertModel(
            HardWorkDTO.builder()
                .id("0xa244cf7c9a934b526404c03af7b28acb4926a128be85ef85f2911961c0c116b7_329")
                .vault("V_VAI")
                .block(6168762)
                .blockDate(1617219879)
                .shareChange(8.0323703167537E-5)
                .fullRewardUsd(0.07453871625007068)
                .tvl(0.0)
                .periodOfWork(0)
                .psPeriodOfWork(0)
                .weeklyProfit(0.0)
                .psTvlUsd(0.0)
                .psApr(0.0)
                .farmBuyback(0.005963097300005617)
                .callsQuantity(1)
                .poolUsers(0)
                .savedGasFees(0.0)
                .savedGasFeesSum(0.0)
                .fee(2.37311058876429)
                .weeklyAverageTvl(null)
                .farmBuybackEth(3.9102153920770145E-5)
                .feeEth(0.00781472)
                .gasUsed(781472.0)
                .idleTime(0)
                .invested(100.0)
                .investmentTarget(100.0)
                .farmPrice(0.0)
                .ethPrice(1906.2560185585446)
                .profitSharingRate(0.08)
                .buyBackRate(0.0)
                .autoStake(0)
                .build(),
            dto, excludeFields
        );
    }

    @Test
    public void parse_VENUS_BETH_empty() throws Exception {
        HardWorkDTO dto = loadHardWork(6166719);
        assertModel(
            HardWorkDTO.builder()
                .id("0x3cd8edeba0ff0937c692e1fc130e8db08d605c02c7d62352c328c6412f1f56d0_277")
                .vault("V_BETH_#V1")
                .block(6166719)
                .blockDate(1617213744)
                .shareChange(5.84328668143E-7)
                .fullRewardUsd(0.0)
                .tvl(0.0)
                .periodOfWork(0)
                .psPeriodOfWork(0)
                .weeklyProfit(0.0)
                .psTvlUsd(0.0)
                .psApr(0.0)
                .farmBuyback(0.0)
                .callsQuantity(1)
                .poolUsers(0)
                .savedGasFees(0.0)
                .savedGasFeesSum(0.0)
                .fee(6.96817876103766)
                .weeklyAverageTvl(null)
                .farmBuybackEth(0.0)
                .feeEth(0.02291484)
                .gasUsed(2291484.0)
                .idleTime(0)
                .invested(100.0)
                .investmentTarget(100.0)
                .farmPrice(0.0)
                .ethPrice(1850.1615996909184)
                .profitSharingRate(0.08)
                .buyBackRate(0.0)
                .autoStake(0)
                .build(),
            dto, excludeFields
        );
    }

    private HardWorkDTO loadHardWork(int onBlock) {
        //noinspection rawtypes
        List<LogResult> logResults = web3Functions.fetchContractLogs(
            Collections.singletonList(getDoHardWork(onBlock,BSC_NETWORK)),
            onBlock, onBlock, BSC_NETWORK);
        assertNotNull(logResults);
        Assertions.assertFalse(logResults.isEmpty());
        HardWorkDTO dto = hardWorkParser.parse((Log) logResults.get(0), BSC_NETWORK);
        assertNotNull(dto);

//        if (dto.getFullRewardUsd() != 0) {
//            double psReward = dto.getFarmBuyback();
//            double psRewardUsd = psReward * dto.getFarmPrice();
//
//            double wholeRewardBasedOnPsReward = psRewardUsd / dto.getProfitSharingRate();
//            double wholeRewardBasedOnVaultReward =
//                dto.getFullRewardUsd() / (1 - dto.getProfitSharingRate());
//            // when price volatile we can by less % value for the strategy income
//            double diff =
//                Math.abs(wholeRewardBasedOnPsReward - wholeRewardBasedOnVaultReward)
//                    / wholeRewardBasedOnPsReward;
//            Assertions.assertEquals(0.0, diff, 2.0, "% diff balance check");
//        }
        hardWorkDbService.save(dto);
        return dto;
    }

}
