package pro.belbix.ethparser.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;

class AbiProviderServiceTest {

  @Test
  void contractSourceCode() {
    AbiProviderService abiProviderService = new AbiProviderService(ETH_NETWORK);
    assertNotNull(abiProviderService.contractSourceCode(
        "0xBB9bc244D798123fDe783fCc1C72d3Bb8C189413", "YourApiKeyToken"));
  }
}
