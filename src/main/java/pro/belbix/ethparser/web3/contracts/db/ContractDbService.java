package pro.belbix.ethparser.web3.contracts.db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
import pro.belbix.ethparser.web3.contracts.ContractUtils;

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

  public Optional<ContractEntity> getContractByAddressAndType(
      String address,
      ContractType type,
      String network) {
    return Optional.ofNullable(contractRepository
        .findFirstByAddressAndType(address.toLowerCase(), type.getId(), network));
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

  public Optional<ContractEntity> getPoolContractByVaultAddress(String address, String network) {
    return Optional.ofNullable(contractRepository
        .findPoolByVaultAddress(address.toLowerCase(), network));
  }

  public double parseAmount(BigInteger amount, String address, String network) {
    if (amount == null) {
      return 0.0;
    }
    return new BigDecimal(amount)
        .divide(getDividerByAddress(address, network), 99, RoundingMode.HALF_UP)
        .doubleValue();
  }

  public BigDecimal getDividerByAddress(String address, String network) {
    address = address.toLowerCase();
    long decimals;
    // unique addresses
    if (ContractUtils.isPsAddress(address)) {
      decimals = 18L;
    } else {
      ContractEntity contract = getContractByAddress(address, network).orElseThrow();
      if (contract.getType() == ContractType.VAULT.getId()) {
        decimals = vaultRepository.findFirstByContract(address, network).getDecimals();
      } else if (contract.getType() == ContractType.POOL.getId()) {
        decimals = 18L;
      } else if (contract.getType() == ContractType.UNI_PAIR.getId()) {
        decimals = uniPairRepository.findFirstByContract(address, network).getDecimals();
      } else if (contract.getType() == ContractType.TOKEN.getId()) {
        decimals = tokenRepository.findFirstByContract(address, network).getDecimals();
      } else {
        throw new IllegalStateException("Unknown address " + address);
      }
    }
    return new BigDecimal(10L).pow((int) decimals);
  }

}
