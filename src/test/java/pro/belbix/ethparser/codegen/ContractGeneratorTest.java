package pro.belbix.ethparser.codegen;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.Web3Service;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class ContractGeneratorTest {

  @Autowired
  private ContractGenerator contractGenerator;
  @Autowired
  private Web3Service web3Service;

  @Test
  void contractGeneratorShouldCreateFile() {
    String className =
        contractGenerator.generateFromAddress(
            "0x07dbe6aa35ef70dad124f4e2b748ffa6c9e1963a", ".", "tmp");
    assertNotNull(className);
  }

  @Test
  void contractGeneratorShouldCreateFile2() {
    String className =
        contractGenerator.generateFromAddress(
            "0x1494ca1f11d487c2bbe4543e90080aeba4ba3c2b", ".", "tmp");
    assertNotNull(className);
  }

  @Test
  void getWrapperClassByAddressTest() {
    Class<?> clazz =
        contractGenerator.getWrapperClassByAddress("0x07dbe6aa35ef70dad124f4e2b748ffa6c9e1963a");
    assertNotNull(clazz);
  }
}
