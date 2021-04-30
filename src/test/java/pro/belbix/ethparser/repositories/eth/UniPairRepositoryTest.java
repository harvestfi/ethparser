package pro.belbix.ethparser.repositories.eth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class UniPairRepositoryTest {

  @Autowired
  private UniPairRepository uniPairRepository;

  @Test
  void findFirstByContractSmokeTest() {
    List<UniPairEntity> uniPairs = uniPairRepository.findAll();
    assertNotNull(uniPairs);
    assertFalse(uniPairs.isEmpty());
    assertNotNull(uniPairRepository.findFirstByAddress(
        uniPairs.get(0).getContract().getAddress(),
        uniPairs.get(0).getContract().getNetwork()));
  }
}
