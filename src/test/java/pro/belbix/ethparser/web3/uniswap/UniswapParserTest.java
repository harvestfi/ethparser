package pro.belbix.ethparser.web3.uniswap;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_WBTC_BADGER;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_WETH_FARM;
import static pro.belbix.ethparser.web3.uniswap.Tokens.USDC_NAME;
import static pro.belbix.ethparser.web3.uniswap.Tokens.WBTC_NAME;
import static pro.belbix.ethparser.web3.uniswap.Tokens.WETH_NAME;

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
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;

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

    @Before
    public void setUp() throws Exception {
        priceProvider.setUpdateTimeout(0);
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
            "3,84574305"
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
            "0,12238873"
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
            "0,12481171"
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
            "91,11346064"
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
            "89,17769530"
        );
    }

    private void shouldNotParse(String contract, int onBlock, int logId) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), onBlock, onBlock);
        assertTrue("Log smaller then necessary " + logResults.size(), logId < logResults.size());
        UniswapDTO dto = uniswapLpLogParser.parseUniswapLog((Log) logResults.get(logId).get());
        assertNull(dto);
    }

    private void uniswapParseTest(
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
