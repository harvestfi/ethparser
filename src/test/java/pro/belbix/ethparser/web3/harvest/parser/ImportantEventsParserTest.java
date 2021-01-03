package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.USDC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.DAI;

import java.util.List;
import java.time.Instant;
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
import pro.belbix.ethparser.dto.ImportantEventsDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class ImportantEventsParserTest {

    @Autowired
    private ImportantEventsParser importantEventsParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;

    @Before
    public void setUp() {
        priceProvider.setUpdateTimeout(0);
    }

    @Test
    public void shouldParseStrategyChange() {
        parserTest(
            USDC,
            11521350,
            0,
            "0x5DB1B2128bCCC5B49f9cA7E3086b14fd4cf2ef64",
            "0x93cee333c690cb91c39ac7b3294740651dc79c3d",
            "2020-12-25T07:12:19Z"
        );
    }
    
    @Test
    public void shouldParseStrategyAnnouced() {
        parserTest(
            DAI,
            11517785,
            0,
            "0x180c496709023CE8952003A9FF385a3bBEB8b2C3",
            String.valueOf("null"),
            "2020-12-24T17:55:58Z"
        );
    }
    private void parserTest(
        String contract,
        int onBlock,
        int logId,
        String newStrategy,
        String oldStrategy,
        String blockDate
    ) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), onBlock, onBlock);
        assertTrue("Log smaller then necessary", logId < logResults.size());
        ImportantEventsDTO dto = null;
        try {
            dto = importantEventsParser.parseLog((Log) logResults.get(logId).get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertDto(dto, newStrategy, oldStrategy, blockDate);
    }

    private void assertDto(ImportantEventsDTO dto, String newStrategy, String oldStrategy, String blockDate) {
        assertNotNull("Dto is null", dto);
        assertAll(
            () -> assertEquals("newStrategy", newStrategy.toLowerCase(), dto.getNewStrategy()),
            () -> assertEquals("oldStrategy", oldStrategy.toLowerCase(), String.valueOf(dto.getOldStrategy())),
            () -> assertEquals("blockDate",blockDate, Instant.ofEpochSecond(dto.getBlockDate()).toString())
        );
    }
}
