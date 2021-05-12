package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.repositories.v0.DeployerRepository;
import pro.belbix.ethparser.web3.deployer.parser.DeployerEventToContractTransformer;

@Service
@Log4j2
public class DeployerRecalculation {

  private final DeployerRepository deployerRepository;
  private final DeployerEventToContractTransformer transformer;

  public DeployerRecalculation(
      DeployerRepository deployerRepository, DeployerEventToContractTransformer transformer) {
    this.deployerRepository = deployerRepository;
    this.transformer = transformer;
  }

  public void start() {
    try {
      List<DeployerDTO> dtos = deployerRepository.findAll(Sort.by("block"));
      for (DeployerDTO dto : dtos) {
        transformer.handleAndSave(dto);
      }
    } catch (Exception e) {
      log.error("Error", e);
    }
  }

}
