package pro.belbix.ethparser.web3.contracts.db;

import java.util.Optional;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.TokenRepository;
import pro.belbix.ethparser.repositories.eth.TokenToUniPairRepository;
import pro.belbix.ethparser.repositories.eth.UniPairRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.web3.contracts.ContractType;

@Service
public class ContractDbService {

  private final ContractRepository contractRepository;
  private final PoolRepository poolRepository;
  private final VaultRepository vaultRepository;
  private final UniPairRepository uniPairRepository;
  private final TokenRepository tokenRepository;
  private final TokenToUniPairRepository tokenToUniPairRepository;


  public ContractDbService(
      ContractRepository contractRepository,
      PoolRepository poolRepository,
      VaultRepository vaultRepository,
      UniPairRepository uniPairRepository,
      TokenRepository tokenRepository,
      TokenToUniPairRepository tokenToUniPairRepository) {
    this.contractRepository = contractRepository;
    this.poolRepository = poolRepository;
    this.vaultRepository = vaultRepository;
    this.uniPairRepository = uniPairRepository;
    this.tokenRepository = tokenRepository;
    this.tokenToUniPairRepository = tokenToUniPairRepository;
  }

  public Optional<ContractEntity> getContractByAddress(String address, String network) {
    return Optional.ofNullable(contractRepository.findFirstByAddress(address, network));
  }

  public Optional<ContractEntity> getContractByNameAndType(
      String name, ContractType type, String network) {
    return Optional.ofNullable(contractRepository
        .findFirstByName(name, type.getId(), network));
  }

  public Optional<String> getNameByAddress(String address, String network) {
    return getContractByAddress(address, network)
        .map(ContractEntity::getName);
  }

  public Optional<String> getAddressByName(String name, ContractType type, String network) {
    return getContractByNameAndType(name, type, network)
        .map(ContractEntity::getAddress);
  }


}
