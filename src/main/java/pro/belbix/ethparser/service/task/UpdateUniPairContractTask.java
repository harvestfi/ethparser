package pro.belbix.ethparser.service.task;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UpdateUniPairContractTask {
  ContractRepository contractRepository;
  ContractLoader contractLoader;

  @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
  public void start() {
    contractRepository.findAllUniPairContractWithoutData().stream()
        .forEach(this::fetchUniPairContact);
  }

  public void fetchUniPairContact(ContractEntity contract) {
    log.info("Begin fetch uniPairsToToken for {} {}", contract.getAddress(), contract.getNetwork());
    var token = contractLoader.loadToken(contract, contract.getNetwork(), contract.getCreated());
    var uniPairsToToken = contractLoader.linkUniPairsToToken(contract.getAddress(), contract.getCreated(), token, contract.getNetwork());
    log.info("Finish fetch uniPairsToToken - {}", uniPairsToToken);
  }
}
