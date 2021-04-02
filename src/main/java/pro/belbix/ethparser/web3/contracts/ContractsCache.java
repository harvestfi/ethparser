package pro.belbix.ethparser.web3.contracts;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.TokenToUniPairEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.contracts.VaultToPoolEntity;

public class ContractsCache {

  private final Map<String, PoolEntity> poolsCacheByAddress = new LinkedHashMap<>();
  private final Map<String, VaultEntity> vaultsCacheByAddress = new LinkedHashMap<>();
  private final Map<String, UniPairEntity> uniPairsCacheByAddress = new LinkedHashMap<>();
  private final Map<String, TokenEntity> tokensCacheByAddress = new LinkedHashMap<>();
  private final Map<String, PoolEntity> poolsCacheByName = new LinkedHashMap<>();
  private final Map<String, VaultEntity> vaultsCacheByName = new LinkedHashMap<>();
  private final Map<String, UniPairEntity> uniPairsCacheByName = new LinkedHashMap<>();
  private final Map<String, TokenEntity> tokensCacheByName = new LinkedHashMap<>();
  private final Map<Integer, VaultToPoolEntity> vaultToPoolsCache = new LinkedHashMap<>();
  private final Map<Integer, TokenToUniPairEntity> tokenToUniPairCache = new LinkedHashMap<>();
  private final Set<String> contracts = new LinkedHashSet<>();


  public Collection<String> getAllVaultNames() {
    return vaultsCacheByName.keySet();
  }

  public Collection<VaultEntity> getAllVaults() {
    return vaultsCacheByAddress.values();
  }

  public Optional<VaultEntity> getVaultByName(String name) {
    return Optional.ofNullable(vaultsCacheByName.get(name));
  }

  public Optional<VaultEntity> getVaultByAddress(String address) {
    if(address == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(vaultsCacheByAddress.get(address.toLowerCase()));
  }

  public Collection<PoolEntity> getAllPools() {
    return poolsCacheByAddress.values();
  }

  public Optional<PoolEntity> getPoolByName(String name) {
    return Optional.ofNullable(poolsCacheByName.get(name));
  }

  public Optional<PoolEntity> getPoolByAddress(String address) {
    if(address == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(poolsCacheByAddress.get(address.toLowerCase()));
  }

  public Collection<TokenEntity> getAllTokens() {
    return tokensCacheByAddress.values();
  }

  public Optional<TokenEntity> getTokenByName(String name) {
    return Optional.ofNullable(tokensCacheByName.get(name));
  }

  public Optional<TokenEntity> getTokenByAddress(String address) {
    if(address == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(tokensCacheByAddress.get(address.toLowerCase()));
  }

  public Collection<UniPairEntity> getAllUniPairs() {
    return uniPairsCacheByAddress.values();
  }

  public Optional<UniPairEntity> getUniPairByName(String name) {
    return Optional.ofNullable(uniPairsCacheByName.get(name));
  }

  public Optional<UniPairEntity> getUniPairByAddress(String address) {
    if(address == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(uniPairsCacheByAddress.get(address.toLowerCase()));
  }

  public Collection<String> getAllUniPairAddressesWithKeys() {
    return uniPairsCacheByAddress.values().stream()
        .filter(u -> u.getKeyToken() != null)
        .map(UniPairEntity::getContract)
        .map(ContractEntity::getAddress)
        .collect(Collectors.toList());
  }

  public Set<String> getAllContractAddresses() {
    return contracts;
  }

  public Optional<ContractEntity> getContractByAddress(String address) {
    address = address.toLowerCase();
    Optional<ContractEntity> contract = getVaultByAddress(address)
        .map(VaultEntity::getContract);

    if (contract.isEmpty()) {
      contract = getPoolByAddress(address)
          .map(PoolEntity::getContract);
    }
    if (contract.isEmpty()) {
      contract = getUniPairByAddress(address)
          .map(UniPairEntity::getContract);
    }
    if (contract.isEmpty()) {
      contract = getTokenByAddress(address)
          .map(TokenEntity::getContract);
    }
    return contract;
  }

  Collection<PoolEntity> getPoolEntities() {
    return poolsCacheByAddress.values();
  }

  Collection<VaultEntity> getVaultEntities() {
    return vaultsCacheByAddress.values();
  }

  Collection<TokenEntity> getTokenEntities() {
    return tokensCacheByAddress.values();
  }

  Collection<UniPairEntity> getLpEntities() {
    return uniPairsCacheByAddress.values();
  }

  Collection<TokenToUniPairEntity> getTokenToLpEntities() {
    return tokenToUniPairCache.values();
  }

  // ------------------ INTERNAL METHODS ------------------

  void addToken(TokenEntity entity) {
    tokensCacheByAddress.put(entity.getContract().getAddress(), entity);
    tokensCacheByName.put(entity.getContract().getName(), entity);
  }

  void addVault(VaultEntity vaultEntity) {
    vaultsCacheByAddress.put(vaultEntity.getContract().getAddress(), vaultEntity);
    vaultsCacheByName.put(vaultEntity.getContract().getName(), vaultEntity);
  }

  void addPool(PoolEntity poolEntity) {
    poolsCacheByAddress.put(poolEntity.getContract().getAddress(), poolEntity);
    poolsCacheByName.put(poolEntity.getContract().getName(), poolEntity);
  }

  void addLp(UniPairEntity uniPairEntity) {
    uniPairsCacheByAddress.put(uniPairEntity.getContract().getAddress(), uniPairEntity);
    uniPairsCacheByName.put(uniPairEntity.getContract().getName(), uniPairEntity);
  }

  void addVaultToPool(VaultToPoolEntity vaultToPoolEntity) {
    vaultToPoolsCache.put(vaultToPoolEntity.getId(), vaultToPoolEntity);
  }

  void addTokenToLp(TokenToUniPairEntity link) {
    tokenToUniPairCache.put(link.getId(), link);
  }

  void initAllContract() {
    contracts.clear();
    contracts.addAll(vaultsCacheByAddress.keySet());
    contracts.addAll(poolsCacheByAddress.keySet());
    contracts.addAll(uniPairsCacheByAddress.values().stream()
        .map(u -> u.getContract().getAddress())
        .collect(Collectors.toSet())
    );
    contracts.addAll(tokensCacheByAddress.values().stream()
        .map(u -> u.getContract().getAddress())
        .collect(Collectors.toSet())
    );
  }


}
