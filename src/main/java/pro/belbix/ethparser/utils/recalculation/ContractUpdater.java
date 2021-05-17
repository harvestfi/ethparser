package pro.belbix.ethparser.utils.recalculation;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;
import pro.belbix.ethparser.web3.deployer.transform.DeployerEventToContractTransformer;
import pro.belbix.ethparser.web3.deployer.transform.TokenTransformer;

@Service
@Log4j2
public class ContractUpdater {

  private final AppProperties appProperties;
  private final ContractRepository contractRepository;
  private final TokenTransformer tokenTransformer;
  private final DeployerEventToContractTransformer deployerEventToContractTransformer;

  @Value("${contract-updater.block: 0}")
  private int block;
  @Value("${contract-updater.token:}")
  private String token;

  public ContractUpdater(AppProperties appProperties,
      ContractRepository contractRepository,
      TokenTransformer tokenTransformer,
      DeployerEventToContractTransformer deployerEventToContractTransformer) {
    this.appProperties = appProperties;
    this.contractRepository = contractRepository;
    this.tokenTransformer = tokenTransformer;
    this.deployerEventToContractTransformer = deployerEventToContractTransformer;
  }

  public void start() {
    try {
      if (Strings.isBlank(token)) {
        updateAll();
      } else {
        updateToken(token.toLowerCase());
      }
    } catch (Exception e) {
      log.error("Error", e);
    }
  }

  private void updateToken(String token) {
    List<PureEthContractInfo> infos = new ArrayList<>();
    tokenTransformer.createTokenAndLpContracts(
        token, block, appProperties.getUtilNetwork(), infos);
    deployerEventToContractTransformer.uploadContracts(infos);
  }

  private void updateAll() {
    List<ContractEntity> contracts = contractRepository.findAll();
    List<PureEthContractInfo> infos = new ArrayList<>();

    for (ContractEntity contract : contracts) {
      if (contract.getType() != ContractType.TOKEN.getId()
          || !appProperties.getUtilNetwork().equalsIgnoreCase(contract.getNetwork())) {
        continue;
      }

      tokenTransformer.createTokenAndLpContracts(
          contract.getAddress(), block, contract.getNetwork(), infos);
    }

    deployerEventToContractTransformer.uploadContracts(infos);
  }

}
