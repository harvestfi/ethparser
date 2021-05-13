package pro.belbix.ethparser.utils.recalculation;

import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.repositories.v0.DeployerRepository;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.deployer.parser.DeployerEventToContractTransformer;

@Service
@Log4j2
public class DeployerRecalculation {

  private final DeployerRepository deployerRepository;
  private final DeployerEventToContractTransformer transformer;
  private final Web3Functions web3Functions;

  @Value("${deployer-recalculation.fill-addresses:false}")
  private boolean fillAddresses;

  public DeployerRecalculation(
      DeployerRepository deployerRepository, DeployerEventToContractTransformer transformer,
      Web3Functions web3Functions) {
    this.deployerRepository = deployerRepository;
    this.transformer = transformer;
    this.web3Functions = web3Functions;
  }

  public void start() {
    try {
      List<DeployerDTO> dtos = deployerRepository.findAll(Sort.by("block"));
      for (DeployerDTO dto : dtos) {
        if (fillAddresses) {
          fillAddresses(dto);
        } else {
          transformer.handleAndSave(dto);
        }
      }
    } catch (Exception e) {
      log.error("Error", e);
    }
  }

  private void fillAddresses(DeployerDTO dto) {
    if (dto.getToAddress() != null
        || !CONTRACT_CREATION.name().equals(dto.getType())) {
      return;
    }

    TransactionReceipt transactionReceipt =
        web3Functions.fetchTransactionReceipt(dto.getId(), dto.getNetwork());
    dto.setToAddress(transactionReceipt.getContractAddress());
    deployerRepository.save(dto);
    log.info("Filled {}", dto);
  }

}
