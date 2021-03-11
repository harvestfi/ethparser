package pro.belbix.ethparser.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EtherscanServiceTest {

  private EtherscanService etherscanService = new EtherscanService();

  @Test
  void contractSourceCode() {
    assertNotNull(etherscanService.contractSourceCode(
        "0xBB9bc244D798123fDe783fCc1C72d3Bb8C189413", "YourApiKeyToken"));
  }
}
