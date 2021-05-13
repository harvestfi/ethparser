package pro.belbix.ethparser.web3.contracts;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class ContractLoaderTest {

  @Autowired
  private ContractLoader contractLoader;
  @MockBean
  private ContractRepository contractRepository;
  @MockBean
  private ContractDbService contractDbService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void saveContractTest_DuplicateNames() {
    String adr = "adr";
    String net = "fake_net";
    String name = "cName";
    String und = "undrAdr";
    ContractType type = ContractType.VAULT;
    when(contractRepository.findFirstByAddress(eq(adr), eq(net)))
        .thenReturn(null);
    when(contractDbService.getContractByNameAndType(eq(name), eq(type), eq(net)))
        .thenReturn(Optional.of(new ContractEntity()));

    contractLoader.findOrCreateContract(
        adr,
        name,
        type.getId(),
        1,
        true,
        net,
        0,
        und
    );
    ContractEntity result = new ContractEntity();
    result.setAddress(adr);
    result.setName(name + "_#V1");
    result.setCreated(1L);
    result.setCurveUnderlying(und);
    result.setNetwork(net);
    verify(contractRepository, times(1)).save(result);
  }

  @Test
  void saveContractTest_Rewrite() {
    ContractEntity contractEntity = new ContractEntity();
    String adr = "adr";
    String net = "fake_net";
    String name = "cName";
    String und = "undrAdr";

    contractEntity.setAddress(adr);
    contractEntity.setNetwork(net);
    contractEntity.setName("old" + name);
    contractEntity.setCurveUnderlying("old" + und);
    contractEntity.setCreated(0L);

    ContractType type = ContractType.VAULT;
    when(contractRepository.findFirstByAddress(eq(adr), eq(net)))
        .thenReturn(contractEntity);
    when(contractDbService.getContractByNameAndType(eq(name), eq(type), eq(net)))
        .thenReturn(Optional.empty());

    contractLoader.findOrCreateContract(
        adr,
        name,
        type.getId(),
        1,
        true,
        net,
        0,
        und
    );
    ContractEntity result = new ContractEntity();
    result.setAddress(adr);
    result.setName(name);
    result.setCreated(1L);
    result.setCurveUnderlying(und);
    result.setNetwork(net);
    verify(contractRepository, times(1)).save(result);
  }
}
