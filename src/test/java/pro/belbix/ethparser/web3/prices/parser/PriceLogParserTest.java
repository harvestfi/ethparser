package pro.belbix.ethparser.web3.prices.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_DAI_BSG;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.PriceDTO;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceLogParserTest {

    @Autowired
    private PriceLogParser priceLogParser;
    @Autowired
    private Web3Service web3Service;

    @Test
    public void priceParse() {
        assertOnBlock(
            UNI_LP_DAI_BSG,
            11644538,
            1,
            "0xa96edf5c1858ab62d8bcf10d54e2adee1f1bdf38fd36c8f4450d3eb3ad8f7223_101",
            "UNI_LP_DAI_BSG",
            true,
            "BSG",
            9.95014151163403E14,
            "DAI",
            2.0016815759936366E20,
            201171.166626445
        );
    }

    private PriceDTO assertOnBlock(String contract,
                                   int onBlock,
                                   int logId,
                                   String id,
                                   String source,
                                   boolean buy,
                                   String token,
                                   double tokenAmount,
                                   String otherToken,
                                   double otherTokenAmount,
                                   double price
    ) {
        List<LogResult> logResults = web3Service
            .fetchContractLogs(Collections.singletonList(contract), onBlock, onBlock);
        assertNotNull(logResults);
        assertFalse(logResults.isEmpty());
        PriceDTO dto = priceLogParser.parse((Log) logResults.get(logId));
        assertNotNull(dto);
        assertAll(
            () -> assertEquals("id", id, dto.getId()),
            () -> assertEquals("source", source, dto.getSource()),
            () -> assertEquals("buy", buy, dto.getBuy()),
            () -> assertEquals("token", token, dto.getToken()),
            () -> assertEquals("tokenAmount", tokenAmount, dto.getTokenAmount(), 0.000001),
            () -> assertEquals("otherToken", otherToken, dto.getOtherToken()),
            () -> assertEquals("otherTokenAmount", otherTokenAmount, dto.getOtherTokenAmount(), 0.000001),
            () -> assertEquals("price", price, dto.getPrice(), 0.000001)
        );
        return dto;
    }

}
