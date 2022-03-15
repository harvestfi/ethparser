package pro.belbix.ethparser.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUniPairContractTask {
  @Value("${task.uni-pair.enable}")
  private Boolean enable;
  private final ContractRepository contractRepository;
  private final ContractLoader contractLoader;

  @Scheduled(fixedRateString = "${task.uni-pair.fixedRate}")
  public void start() {
    if (enable == null || !enable) {
      log.info("UpdateUniPairContractTask disabled");
      return;
    }
    log.info("Start UpdateUniPairContractTask");
    contractRepository.findAllUniPairContractWithoutData()
        .forEach(this::fetchUniPairContact);
  }

  public void fetchUniPairContact(ContractEntity contract) {
    try {
      log.info("Begin fetch uniPairsToToken for {} {}", contract.getAddress(), contract.getNetwork());
      var token = contractLoader.loadToken(contract, contract.getNetwork(), contract.getCreated());
      var uniPairsToToken = contractLoader.linkUniPairsToToken(contract.getAddress(), contract.getCreated(), token, contract.getNetwork());
      log.info("Finish fetch uniPairsToToken - {}", uniPairsToToken);
    } catch (Exception e) {
      log.error("Can not fetch uniPair token: {}", contract);
    }
  }
}
