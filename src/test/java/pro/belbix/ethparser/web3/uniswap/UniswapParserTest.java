package pro.belbix.ethparser.web3.uniswap;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.uniswap.UniswapLpLogDecoder.FARM_ETH_LP_CONTRACT;

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
    public void parseFarmEth() {
        uniswapParseTest(FARM_ETH_LP_CONTRACT,
            11379770,
            1,
            "0x9ba68a3a8d578eb6658ba14c47954c9e1c56fa60cd4080f8a04ef0d4e97d4aae_286",
            "0x05310c5594a3c961f212308317ff3a4fdd8f82af",
            "1,00000000",
            "WETH",
            "0,14924968",
            "91,11346064"
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
            otherCoin,
            otherAmount,
            lastPrice
        );
    }

    private void assertDto(UniswapDTO dto,
                           String id,
                           String owner,
                           String amount,
                           String otherCoin,
                           String otherAmount,
                           String lastPrice) {
        assertNotNull("Dto is null", dto);
        assertAll(() -> assertEquals("owner", owner, dto.getOwner()),
            () -> assertEquals("Id", id, dto.getId()),
            () -> assertEquals("Amount", amount, String.format("%.8f", dto.getAmount())),
            () -> assertEquals("OtherCoin", otherCoin, dto.getOtherCoin()),
            () -> assertEquals("otherAmount", otherAmount, String.format("%.8f", dto.getOtherAmount())),
            () -> assertEquals("lastPrice", lastPrice, String.format("%.8f", dto.getLastPrice()))
        );
    }
}
