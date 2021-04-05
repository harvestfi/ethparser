package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.numberFormat;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.ImportantEventsDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class ImportantEventsParserTest {

  @Autowired
  private ImportantEventsParser importantEventsParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private PriceProvider priceProvider;

  @Autowired
  private ContractLoader contractLoader;

   @BeforeEach
  public void setUp() {
    contractLoader.load();
    priceProvider.setUpdateBlockDifference(1);
  }

  @Test
  public void shouldParseStrategyChange() {
    parserTest(
            "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE",
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
            "0xab7FA2B2985BCcfC13c6D86b1D5A17486ab1e04C",
            11517785,
            0,
            "0x180c496709023CE8952003A9FF385a3bBEB8b2C3",
            "0xfde5dfb79d4a65913cb72ddd9148a768705e98d4",
            "2020-12-24T17:55:58Z",
            "null"
        );
    }

    @Test
    public void shouldParseMint() {
        parserTest(
            ContractConstants.FARM_TOKEN,
            10776715, 
            0, 
            "null",
            "null", 
            "2020-09-01T17:41:09Z",
            "11513,82000000"
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
      mintAmount = numberFormat(mintAmount);
        List<LogResult> logResults = web3Functions
            .fetchContractLogs(singletonList(contract), onBlock, onBlock, ETH_NETWORK);
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
        assertNotNull(dto, "Dto is null");
        assertAll(
            () -> assertEquals("newStrategy", newStrategy.toLowerCase(), String.valueOf(dto.getNewStrategy())),
            () -> assertEquals("oldStrategy", oldStrategy.toLowerCase(), String.valueOf(dto.getOldStrategy())),
            () -> assertEquals("blockDate",blockDate, Instant.ofEpochSecond(dto.getBlockDate()).toString()),
            () -> assertEquals("mintAmount", mintAmount, String.format("%.8f", dto.getMintAmount()))
        );
    }
}
