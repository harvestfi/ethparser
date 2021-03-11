package pro.belbix.ethparser.web3.abi.gen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class ContractGeneratorTest {

  @Autowired
  private ContractGenerator contractGenerator;
  @Autowired
  private ContractLoader contractLoader;

  @BeforeEach
  void setUp() {
    contractLoader.load();
  }

  @Test
  void contractGeneratorShouldCreateFile() {
    contractGenerator.setDestinationDir(".");
    contractGenerator.setDestinationRootPackage("tmp");
    contractGenerator.generateFromAddress("0x07dbe6aa35ef70dad124f4e2b748ffa6c9e1963a", "");
  }
}
