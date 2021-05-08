package pro.belbix.ethparser.web3.prices.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.web3.Web3Functions;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceLogParserBscTest {

  @Autowired
  private PriceLogParser priceLogParser;
  @Autowired
  private Web3Functions web3Functions;

  @Test
  public void priceParsePC_WBNB_BUSD() throws Exception {
    assertModel(
        PriceDTO.builder()
            .block(6442026L)
            .network(BSC_NETWORK)
            .token("WBNB")
            .tokenAddress("0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c")
            .tokenAmount(0.18967699605476643)
            .otherToken("BUSD")
            .otherTokenAddress("0xe9e7cea3dedca5984780bafc599bd69add087d56")
            .otherTokenAmount(91.77635486931689)
            .price(483.85601194790024)
            .buy(1)
            .source("PC_WBNB_BUSD")
//            .lpTotalSupply(8660817.747093258)
//            .lpToken0Pooled(453476.31838750903)
//            .lpToken1Pooled(2.1896660626009792E8)
            .build(),
        loadPrice(
            "0x1b96b92314c44b159149f7e0303511fb2fc4774f",
            6442026,
            1
        ));
  }

  private PriceDTO loadPrice(String contract, int onBlock, int logId) {
    @SuppressWarnings("rawtypes")
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(Collections.singletonList(contract), onBlock, onBlock, BSC_NETWORK);
    assertNotNull(logResults);
    Assertions.assertFalse(logResults.isEmpty());
    PriceDTO dto = priceLogParser.parse((Log) logResults.get(logId), BSC_NETWORK);
    assertNotNull(dto);
    return dto;
  }

}
