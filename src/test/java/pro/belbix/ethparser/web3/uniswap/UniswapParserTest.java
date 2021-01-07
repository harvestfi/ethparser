package pro.belbix.ethparser.web3.uniswap;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_GRAIN_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_WBTC_BADGER;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_WETH_FARM;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.USDC_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.WBTC_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.WETH_NAME;

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
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class UniswapParserTest {

    private static final int LOG_ID = 0;

    @Autowired
    private UniswapLpLogParser uniswapLpLogParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;
    @Autowired
    private UniToHarvestConverter uniToHarvestConverter;

    @Before
    public void setUp() throws Exception {
        priceProvider.setUpdateTimeout(0);
    }

    @Test
    public void parseUNI_LP_USDC_FARM_ADD2() {
        UniswapDTO dto = uniswapParseTest(UNI_LP_USDC_FARM,
            10777202,
            2,
            "0x2b9e215b27dea420509c81a218119a77bc91931d017cc6eb9f00a090d5ba1eb6_27",
            "0xe2a6b5d2c244b0f9ba4c4abe8ed3d3ef0a89bfeb",
            "4,08147853",
            "ADD",
            USDC_NAME,
            "945,85450400",
            "231,74310394"
        );
        HarvestDTO harvestDTO = uniToHarvestConverter.convert(dto);
        assertNotNull(harvestDTO);
        assertAll(
            () -> assertEquals("Amount", "0,00000000", String.format("%.8f", harvestDTO.getAmount())),
            () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
            () -> assertEquals("Vault", "UNI_LP_USDC_FARM", harvestDTO.getVault()),
            () -> assertEquals("UsdAmount", "1892", String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
            () -> assertEquals("LastUsdTvl", "0", String.format("%.0f", harvestDTO.getLastUsdTvl())),
            () -> assertEquals("LpStat", "{\"coin1\":\"FARM\",\"coin2\":\"USDC\",\"amount1\":0.0,\"amount2\":0.0}",
                harvestDTO.getLpStat()),
            () -> assertEquals("LastTvl", "0,00000000", String.format("%.8f",  harvestDTO.getLastTvl()))
        );
    }

    @Test
    public void parseUNI_LP_GRAIN_FARM_BUY() {
        uniswapParseTest(UNI_LP_GRAIN_FARM,
            11420092,
            1,
            "0x1248c50025f40dcb1df7af6d1d96cbf744a59ef56f210f2415498fa04854458d_79",
            "0x2b01755949aec1c8479b4d6dd1ea1e725091b21e",
            "1784,61622059",
            "BUY",
            FARM_NAME,
            "2,02599871",
            "0,11354850"
        );
    }

    @Test
    public void parseUNI_LP_WBTC_BADGER_SELL() {
        uniswapParseTest(UNI_LP_WBTC_BADGER,
            11382099,
            4,
            "0x63a3381fcb0f02abba6f73a0230066a1e653a625733db294d06fa363ecf0b363_66",
            "0xe54bee8258a2fe65095516f199034a08c02e35fe",
            "315,00000000",
            "SELL",
            WBTC_NAME,
            "0,06232325",
            "3,85605455"
        );
    }

    @Test
    public void parseUNI_LP_WBTC_BADGER_ADD() {
        uniswapParseTest(UNI_LP_WBTC_BADGER,
            11381218,
            3,
            "0xf23b1fdecffd8dc3a2eb4976c0d0d3de320efa1e17349d3bfafafe308a3ee0bf_161",
            "0x794c9af9c29e89b5cdec5a081c3a5ce30ace638a",
            "49,74418086",
            "ADD",
            WBTC_NAME,
            "0,00031472",
            "0,12263252"
        );
    }

    @Test
    public void parseUNI_LP_WBTC_BADGER_BUY() {
        uniswapParseTest(UNI_LP_WBTC_BADGER,
            11381227,
            1,
            "0x45653b6a3da7a7f5a2e1b6676f2a947c97d25646d29dc5550ac445497e23a2cd_62",
            "0xfe56a0dbdad44dd14e4d560632cc842c8a13642b",
            "2,00000000",
            "BUY",
            WBTC_NAME,
            "0,00001290",
            "0,12461304"
        );
    }

    @Test
    public void parseUNI_LP_WETH_FARM_SELL() {
        uniswapParseTest(UNI_LP_WETH_FARM,
            11379770,
            1,
            "0x9ba68a3a8d578eb6658ba14c47954c9e1c56fa60cd4080f8a04ef0d4e97d4aae_286",
            "0x05310c5594a3c961f212308317ff3a4fdd8f82af",
            "1,00000000",
            "SELL",
            WETH_NAME,
            "0,14924968",
            "91,38680102"
        );
    }

    @Test
    public void parseUNI_LP_USDC_FARM_BUY() {
        uniswapParseTest(UNI_LP_USDC_FARM,
            11379014,
            1,
            "0xd57e7afaf8da1be10aebecc7f509990e180234b42e39cc56422b40bb913ab48c_126",
            "0xbd9fe62f10df42f6d58d88a280c798de865fac86",
            "10,00000000",
            "BUY",
            USDC_NAME,
            "891,77695300",
            "88,91016221"
        );
    }

    @Test
    public void parseUNI_LP_USDC_FARM_ADD() {
        UniswapDTO dto = uniswapParseTest(UNI_LP_USDC_FARM,
            10826472,
            3,
            "0x00088d2d18d024d462e3fc9e513063eff1545c10b43678e6bf605d08bddce0d2_243",
            "0x0d089508d5fcdc92363fe84c84a44738863d9201",
            "4,02461788",
            "ADD",
            USDC_NAME,
            "941,51653400",
            "233,93936051"
        );

        HarvestDTO harvestDTO = uniToHarvestConverter.convert(dto);
        assertNotNull(harvestDTO);
        assertAll(
            () -> assertEquals("Amount", "0,00005882", String.format("%.8f", harvestDTO.getAmount())),
            () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
            () -> assertEquals("Vault", "UNI_LP_USDC_FARM", harvestDTO.getVault()),
            () -> assertEquals("UsdAmount", "1883", String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
            () -> assertEquals("LastUsdTvl", "9657721", String.format("%.0f", harvestDTO.getLastUsdTvl())),
            () -> assertEquals("LpStat", "{\"coin1\":\"FARM\",\"coin2\":\"USDC\",\"amount1\":20641.504241330007,\"amount2\":4828860.303375672}",
                harvestDTO.getLpStat()),
            () -> assertEquals("LastTvl", "0,30169333", String.format("%.8f",  harvestDTO.getLastTvl()))
        );
    }

    @Test
    public void parseUNI_LP_WETH_FARM_ADD() {
        UniswapDTO dto = uniswapParseTest(UNI_LP_WETH_FARM,
            11414884,
            2,
            "0x0c3805658bbad43cfa7745ec749f35479364e857dddc57517b7a2932becf6228_284",
            "0x51d2c880493ac63140ffe0e645cc99afc228ab59",
            "250,00000000",
            "ADD",
            WETH_NAME,
            "44,45189785",
            "99,88726471"
        );
        HarvestDTO harvestDTO = uniToHarvestConverter.convert(dto);
        assertNotNull(harvestDTO);
        assertAll(
            () -> assertEquals("Amount", "92,94539587", String.format("%.8f", harvestDTO.getAmount())),
            () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
            () -> assertEquals("Vault", "UNI_LP_WETH_FARM", harvestDTO.getVault()),
            () -> assertEquals("UsdAmount", "50220", String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
            () -> assertEquals("LastUsdTvl", "282796", String.format("%.0f", harvestDTO.getLastUsdTvl())),
            () -> assertEquals("LpStat", "{\"coin1\":\"FARM\",\"coin2\":\"ETH\",\"amount1\":1411.6707756227604,\"amount2\":251.00578046192655}",
                harvestDTO.getLpStat()),
            () -> assertEquals("LastTvl", "523,39010775", String.format("%.8f",  harvestDTO.getLastTvl()))
        );
    }

    @Test
    public void parseUNI_LP_GRAIN_FARM_ADD() {
        UniswapDTO dto = uniswapParseTest(UNI_LP_GRAIN_FARM,
            11417637,
            2,
            "0x072a25cbdc61c4302483593044c536055f7ddd9dcca48fc25c66af1d4f56edfb_104",
            "0xbb830af11a5bf085e63ee04bf4f3da50955e8eb5",
            "127744,24825310",
            "ADD",
            FARM_NAME,
            "140,02624240",
            "0,10707515"
        );
        HarvestDTO harvestDTO = uniToHarvestConverter.convert(dto);
        assertNotNull(harvestDTO);
        assertAll(
            () -> assertEquals("Amount", "4170,06518775", String.format("%.8f", harvestDTO.getAmount())),
            () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
            () -> assertEquals("Vault", "UNI_LP_GRAIN_FARM", harvestDTO.getVault()),
            () -> assertEquals("UsdAmount", "27356", String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
            () -> assertEquals("LastUsdTvl", "219248", String.format("%.0f", harvestDTO.getLastUsdTvl())),
            () -> assertEquals("LpStat", "{\"coin1\":\"GRAIN\",\"coin2\":\"FARM\",\"amount1\":1023805.5470726851,\"amount2\":1122.239518907442}",
                harvestDTO.getLpStat()),
            () -> assertEquals("LastTvl", "33420,96359919", String.format("%.8f",  harvestDTO.getLastTvl()))
        );
    }

    private void shouldNotParse(String contract, int onBlock, int logId) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), onBlock, onBlock);
        assertTrue("Log smaller then necessary " + logResults.size(), logId < logResults.size());
        UniswapDTO dto = uniswapLpLogParser.parseUniswapLog((Log) logResults.get(logId).get());
        assertNull(dto);
    }

    private UniswapDTO uniswapParseTest(
        String contract,
        int onBlock,
        int logId,
        String id,
        String owner,
        String amount,
        String type,
        String otherCoin,
        String otherAmount,
        String lastPrice
    ) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), onBlock, onBlock);
        assertTrue("Log smaller then necessary " + logResults.size(), logId < logResults.size());
        UniswapDTO dto = uniswapLpLogParser.parseUniswapLog((Log) logResults.get(logId).get());
        assertDto(dto,
            id,
            owner,
            amount,
            type,
            otherCoin,
            otherAmount,
            lastPrice
        );
        return dto;
    }

    private void assertDto(UniswapDTO dto,
                           String id,
                           String owner,
                           String amount,
                           String type,
                           String otherCoin,
                           String otherAmount,
                           String lastPrice) {
        assertNotNull("Dto is null", dto);
        assertAll(() -> assertEquals("owner", owner, dto.getOwner()),
            () -> assertEquals("Id", id, dto.getId()),
            () -> assertEquals("Amount", amount, String.format("%.8f", dto.getAmount())),
            () -> assertEquals("Type", type, dto.getType()),
            () -> assertEquals("OtherCoin", otherCoin, dto.getOtherCoin()),
            () -> assertEquals("otherAmount", otherAmount, String.format("%.8f", dto.getOtherAmount())),
            () -> assertEquals("lastPrice", lastPrice, String.format("%.8f", dto.getLastPrice()))
        );
    }
}
