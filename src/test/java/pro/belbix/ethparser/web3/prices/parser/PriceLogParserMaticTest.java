package pro.belbix.ethparser.web3.prices.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

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
public class PriceLogParserMaticTest {

  @Autowired
  private PriceLogParser priceLogParser;
  @Autowired
  private Web3Functions web3Functions;

  @Test
  void parsePriceDTO_SUSHI_LP_USDC_WETH() throws Exception {

    long block = 16572688;
    String lpAddress = "0x853ee4b2a13f8a742d64c8f088be7ba2131f670d";
    String network = MATIC_NETWORK;
    assertModel(
        PriceDTO.builder()
            .block(block)
            .network(network)
            .token("WETH")
            .tokenAddress("0x7ceb23fd6bc0add59e62ac25578270cff1b9f619")
            .tokenAmount(0.000360099538084126)
            .otherToken("USDC")
            .otherTokenAddress("0x2791bca1f2de4661ed88a30c99a7a9449aa84174")
            .otherTokenAmount(0.838788)
            .price(2329.3226213582184)
            .buy(1)
            .source("UNI_LP_USDC_WETH")
            .sourceAddress(lpAddress)
            .build(),
        load(
            lpAddress,
            (int) block,
            1,
            network
        ));
  }

  @Test
  public void priceParseSUSHI_LP_USDC_WETH() {
    List<LogResult> logResults = web3Functions.fetchContractLogs(Collections.singletonList("0x853ee4b2a13f8a742d64c8f088be7ba2131f670d"),
        16572688, 16572688, MATIC_NETWORK);
    assertEquals(11,logResults.size());
  }

  private PriceDTO load(String contract, int onBlock, int logId, String network) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(Collections.singletonList(contract), onBlock, onBlock, network);
    assertNotNull(logResults);
    Assertions.assertFalse(logResults.isEmpty());
    PriceDTO dto = priceLogParser.parse((Log) logResults.get(logId), network);
    assertNotNull(dto);
    return dto;
  }

}
