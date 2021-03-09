package pro.belbix.ethparser.web3.layers.detector.db;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.repositories.b_layer.ContractEventRepository;

@Service
@Log4j2
public class ContractEventsDbService {

  private final ContractEventRepository contractEventRepository;

  public ContractEventsDbService(
      ContractEventRepository contractEventRepository) {
    this.contractEventRepository = contractEventRepository;
  }

  public boolean save(ContractEventEntity event) {
    contractEventRepository.save(event);
    return true;
  }

}
