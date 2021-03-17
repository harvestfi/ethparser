package pro.belbix.ethparser.repositories.eth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.contracts.ContractEntity;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class UniPairRepositoryTest {

  @Autowired
  private UniPairRepository uniPairRepository;

  @Test
  void findFirstByContractSmokeTest() {
    ContractEntity contract = new ContractEntity();
    contract.setId(306);
    assertNotNull(uniPairRepository.findFirstByContract(contract));
  }
}
