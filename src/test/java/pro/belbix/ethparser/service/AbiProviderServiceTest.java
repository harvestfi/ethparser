package pro.belbix.ethparser.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class AbiProviderServiceTest {

  @Autowired
  EthBlockService ethBlockService;
  @Autowired
  AbiProviderService abiProviderService;
  @Autowired
  NetworkProperties networkProperties;

  @Test
  void contractSourceCode() {
    AbiProviderService abiProviderService = new AbiProviderService();
    assertNotNull(abiProviderService.contractSourceCode(
        "0xBB9bc244D798123fDe783fCc1C72d3Bb8C189413", "YourApiKeyToken", ETH_NETWORK));
  }

  @Test
  void getBlockInDifferentChainAtTheSameTimeTest() {
    var timestamp = ethBlockService.getTimestampSecForBlock(25561696, MATIC_NETWORK);
    var result = abiProviderService.getBlockByTimestamp(String.valueOf(timestamp), BSC_NETWORK, networkProperties.get(BSC_NETWORK).getAbiProviderKey());

    assertEquals(15758669L, result);
  }
}
