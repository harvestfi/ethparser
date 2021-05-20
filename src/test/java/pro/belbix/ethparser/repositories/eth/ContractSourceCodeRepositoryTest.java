package pro.belbix.ethparser.repositories.eth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.ContractSourceCodeDTO;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class ContractSourceCodeRepositoryTest {

  @Autowired
  private ContractSourceCodeRepository contractSourceCodeRepository;

  @Test
  void saveContractSource() {
    ContractSourceCodeDTO csdto = new ContractSourceCodeDTO();
    csdto.setAbi("Some API");
    csdto.setSourceCode("Some source code");
    csdto.setContractName("Some Name");
    csdto.setAddress("Address");
    csdto.setNetwork("eth");
    ContractSourceCodeDTO cr = contractSourceCodeRepository.save(csdto);
    ContractSourceCodeDTO res = contractSourceCodeRepository.findByAddressNetwork(
        "Address", "eth");
    assertEquals("Address", res.getAddress());
    assertEquals("eth", res.getNetwork());
    assertEquals("Some API", res.getAbi());
    assertEquals("Some source code", res.getSourceCode());
    assertEquals("Some Name", res.getContractName());
  }
}
