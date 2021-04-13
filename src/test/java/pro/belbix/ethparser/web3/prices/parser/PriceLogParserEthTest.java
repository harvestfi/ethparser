package pro.belbix.ethparser.web3.prices.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceLogParserEthTest {

  @Autowired
  private PriceLogParser priceLogParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private ContractLoader contractLoader;

   @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }

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
    public void priceParseUNI_LP_ETH_DAI() {
        assertOnBlock(
            "0xa478c2975ab1ea89e8196811f51a7b7ade33eb11",
            11942484,
            1,
            "0x2c8b2763ab51c386dccc27e53787b5d2b7548639d5d2377f49e30267509a464d_220_eth",
            "UNI_LP_ETH_DAI",
            1,
            "DAI",
            9.44601483E-10,
            "ETH",
            1.948E-15,
            2.0622453331464862E-6
        );
    }

    @Test
    public void priceParseUNI_LP_DAI_BSG() {
        assertOnBlock(
            "0x4a9596e5d2f9bef50e4de092ad7181ae3c40353e",
            11644538,
            1,
            "0xa96edf5c1858ab62d8bcf10d54e2adee1f1bdf38fd36c8f4450d3eb3ad8f7223_101_eth",
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
        List<LogResult> logResults = web3Functions
            .fetchContractLogs(Collections.singletonList(contract), onBlock, onBlock, ETH_NETWORK);
        assertNotNull(logResults);
        assertFalse(logResults.isEmpty());
        PriceDTO dto = priceLogParser.parse((Log) logResults.get(logId), ETH_NETWORK);
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
