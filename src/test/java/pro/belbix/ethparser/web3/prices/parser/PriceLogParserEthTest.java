package pro.belbix.ethparser.web3.prices.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

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
public class PriceLogParserEthTest {

  @Autowired
  private PriceLogParser priceLogParser;
  @Autowired
  private Web3Functions web3Functions;

  @Test
  void parsePriceUNI_LP_WETH_FARM() throws Exception {
    long block = 12546934;
    String lpAddress = "0x56feaccb7f750b997b36a68625c7c596f0b41a58";
    String network = ETH_NETWORK;
    assertModel(
        PriceDTO.builder()
            .block(block)
            .network(network)
            .token("FARM")
            .tokenAddress("0xa0246c9032bc3a600820415ae600c6388619a14d")
            .tokenAmount(5.11485287070358)
            .otherToken("WETH")
            .otherTokenAddress("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
            .otherTokenAmount(0.1394245470562827)
            .price(0.02725876004271145)
            .buy(0)
            .source("UNI_LP_FARM_WETH")
            .sourceAddress(lpAddress)
            .lpTotalSupply(5780.756871444212)
            .lpToken0Pooled(44277.61955154786)
            .lpToken1Pooled(1210.4449168215203)
            .build(),
        load(
            lpAddress,
            (int) block,
            1,
            network
        ));
  }

  @Test
  public void priceParseUNI_LP_WETH_CRV_weird_swap() {
    web3Functions
        .fetchContractLogs(Collections.singletonList("0x3da1313ae46132a397d90d95b1424a9a7e3e0fce"),
            10829173, 10829173, ETH_NETWORK);
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
