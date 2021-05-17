package pro.belbix.ethparser.web3.prices.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.Collections;
import java.util.List;
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
public class PriceLogParserEthTest {

  @Autowired
  private PriceLogParser priceLogParser;
  @Autowired
  private Web3Functions web3Functions;

  @Test
  public void priceParseUNI_LP_WETH_CRV_weird_swap() {
    web3Functions
        .fetchContractLogs(Collections.singletonList("0x3da1313ae46132a397d90d95b1424a9a7e3e0fce"),
            10829173, 10829173, ETH_NETWORK);
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
        () -> assertEquals("otherTokenAmount", otherTokenAmount, dto.getOtherTokenAmount(),
            0.000001),
        () -> assertEquals("price", price, dto.getPrice(), 0.000001)
    );
    return dto;
  }

}
