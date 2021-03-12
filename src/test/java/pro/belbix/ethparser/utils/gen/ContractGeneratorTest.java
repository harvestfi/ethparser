package pro.belbix.ethparser.utils.gen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.utils.gen.ContractGenerator.STUB_CREDENTIALS;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.abi.generated.erc20.FiatTokenProxy_1270266386;
import pro.belbix.ethparser.web3.abi.generated.harvest.VaultProxy_1089971474;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class ContractGeneratorTest {

  @Autowired
  private ContractGenerator contractGenerator;
  @Autowired
  private Web3Service web3Service;

  @Test
  void contractGeneratorShouldCreateFile() {
    contractGenerator.setDestinationDir(".");
    contractGenerator.setDestinationRootPackage("tmp");
    String className =
        contractGenerator.generateFromAddress(
            "0x07dbe6aa35ef70dad124f4e2b748ffa6c9e1963a", "");
    assertNotNull(className);
  }

  @Test
  void contractGeneratorShouldCreateFile2() {
    contractGenerator.setDestinationDir(".");
    contractGenerator.setDestinationRootPackage("tmp");
    String className =
        contractGenerator.generateFromAddress(
            "0x1494ca1f11d487c2bbe4543e90080aeba4ba3c2b", "");
    assertNotNull(className);
  }

  @Test
  void readProxyImpl() throws Exception {
    VaultProxy_1089971474 proxy = VaultProxy_1089971474.load(
        "0x07dbe6aa35ef70dad124f4e2b748ffa6c9e1963a",
        web3Service.getWeb3(),
        STUB_CREDENTIALS,
        null
    );
    String implAddress = proxy.call_implementation().send();
    assertNotNull(implAddress);
    assertEquals("0x9B3bE0cc5dD26fd0254088d03D8206792715588B".toLowerCase(), implAddress.toLowerCase());
  }

  @Test
  @Disabled("USDC proxy doesn't work") //TODO investigate
  void readProxyImpl2() throws Exception {
    FiatTokenProxy_1270266386 proxy = FiatTokenProxy_1270266386.load(
        "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
        web3Service.getWeb3(),
        STUB_CREDENTIALS,
        null
    );
    String implAddress = proxy.call_implementation().send();
    assertNotNull(implAddress);
    assertEquals("0x9B3bE0cc5dD26fd0254088d03D8206792715588B".toLowerCase(), implAddress.toLowerCase());
  }
}
