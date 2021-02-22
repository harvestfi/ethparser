package pro.belbix.ethparser.web3.prices.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

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

//    @Test
//    public void priceParseUNI_LP_USDC_IDX() {
//        assertOnBlock(
//            "0xc372089019614e5791b08b5036f298d002a8cbef",
//            11856484,
//            1,
//            "0x6107429ff8cbd2f7e6b54f1d337201aa89f2a234a632ea4b0c888ce50d05c250",
//            "",
//            1,
//            "",
//            9.95014151163403E-4,
//            "",
//            200.16815759936367,
//            201171.166626445
//        );
//    }

    @Test
    public void priceParseUNI_LP_DAI_BSG() {
        assertOnBlock(
            "0x4a9596e5d2f9bef50e4de092ad7181ae3c40353e",
            11644538,
            1,
            "0xa96edf5c1858ab62d8bcf10d54e2adee1f1bdf38fd36c8f4450d3eb3ad8f7223_101",
            "UNI_LP_DAI_BSG",
            1,
            "BSG",
            9.95014151163403E-4,
            "DAI",
            200.16815759936367,
            201171.166626445
        );
    }

    private PriceDTO assertOnBlock(String contract,
                                   int onBlock,
                                   int logId,
                                   String id,
                                   String source,
                                   int buy,
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
            () -> assertEquals("buy", buy, dto.getBuy(), 0),
            () -> assertEquals("token", token, dto.getToken()),
            () -> assertEquals("tokenAmount", tokenAmount, dto.getTokenAmount(), 0.000001),
            () -> assertEquals("otherToken", otherToken, dto.getOtherToken()),
            () -> assertEquals("otherTokenAmount", otherTokenAmount, dto.getOtherTokenAmount(), 0.000001),
            () -> assertEquals("price", price, dto.getPrice(), 0.000001)
        );
        return dto;
    }

}
