package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.repositories.v0.DeployerRepository;
import pro.belbix.ethparser.web3.deployer.parser.DeployerEventToContractTransformer;

@Service
public class DeployerRecalculation {

  private final DeployerRepository deployerRepository;
  private final DeployerEventToContractTransformer transformer;

  public DeployerRecalculation(
      DeployerRepository deployerRepository, DeployerEventToContractTransformer transformer) {
    this.deployerRepository = deployerRepository;
    this.transformer = transformer;
  }

  public void start() {
    List<DeployerDTO> dtos = deployerRepository.findAll(Sort.by("block"));

    for (DeployerDTO dto : dtos) {
      transformer.handleAndSave(dto);
    }

  }

}
