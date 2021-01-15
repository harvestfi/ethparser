package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_TOKEN;
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
import pro.belbix.ethparser.web3.harvest.db.ImportantEventsDbService;
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
    private ImportantEventsDbService importantEventsDbService;

    @Test
    public void shouldParseStrategyChange() {
        parserTest(
            USDC,
            11521350,
            0,
            "0x5DB1B2128bCCC5B49f9cA7E3086b14fd4cf2ef64",
            "0x93cee333c690cb91c39ac7b3294740651dc79c3d",
            "2020-12-25T07:12:19Z",
            "null"
        );
    }
    
    @Test
    public void shouldParseStrategyAnnounce() {
        parserTest(
            DAI,
            11517785,
            0,
            "0x180c496709023CE8952003A9FF385a3bBEB8b2C3",
            "null",
            "2020-12-24T17:55:58Z",
            "null"
        );
    }

    @Test
    public void shouldParseMintTx() {
        parserTest(
            FARM_TOKEN,
            11550180, 
            0, 
            "null",
            "null", 
            "2020-12-29T17:12:30Z",
            "9129,32300000"
            );
    }    

    @Test
    public void shouldParseTokenMinted() {
        ImportantEventsDTO dto = new ImportantEventsDTO();
        dto.setBlock(Long.valueOf(11550180));
        dto.setEvent(ImportantEventsParser.TOKEN_MINT_TX);
        dto.setHash("0x33336e62c644b763776aae64b6f1ed27903405a92cca4501816515e19949ac4f");

        ImportantEventsDTO tokenMintDto = importantEventsDbService.updateTokenMinted(dto);
 
        assertNotNull("Dto is null", tokenMintDto);
        assertAll(
            // needs transactions in DB
            //() -> assertEquals("mintAmount", tokenMintDto.getMintAmount(), Double.valueOf(13041.89)),
            () -> assertEquals("event", tokenMintDto.getEvent(), ImportantEventsParser.TOKEN_MINT),
            () -> assertEquals("id", tokenMintDto.getId(), "0x33336e62c644b763776aae64b6f1ed27903405a92cca4501816515e19949ac4f_sum")
        );
    }

    private void parserTest(
        String contract,
        int onBlock,
        int logId,
        String newStrategy,
        String oldStrategy,
        String blockDate,
        String mintAmount
    ) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(contract), onBlock, onBlock);
        assertTrue("Log smaller then necessary", logId < logResults.size());
        ImportantEventsDTO dto = null;
        try {
            dto = importantEventsParser.parseLog((Log) logResults.get(logId).get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertDto(dto, newStrategy, oldStrategy, blockDate, mintAmount);
    }

    private void assertDto(ImportantEventsDTO dto, String newStrategy, String oldStrategy, String blockDate, String mintAmount) {
        assertNotNull("Dto is null", dto);
        assertAll(
            () -> assertEquals("newStrategy", newStrategy.toLowerCase(), String.valueOf(dto.getNewStrategy())),
            () -> assertEquals("oldStrategy", oldStrategy.toLowerCase(), String.valueOf(dto.getOldStrategy())),
            () -> assertEquals("blockDate",blockDate, Instant.ofEpochSecond(dto.getBlockDate()).toString()),
            () -> assertEquals("mintAmount", mintAmount, String.format("%.8f", dto.getMintAmount()))
        );
    }
}
