package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ETH_CONTROLLER;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ONE_DOLLAR_TOKENS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_UNI_PAIRS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_ADDRESSES;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.TokenToUniPairEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;

public class ContractUtils {

  private ContractUtils() {
  }

  // todo !!! TEMPORALLY SOLUTION FOR TEST PURPOSES !!!
  private static String network;
  private static String generalNetwork;

  public static void setGeneralNetwork(String newNetwork) {
    network = newNetwork;
    generalNetwork = newNetwork;
  }

  public static void setNetwork(String newNetwork) {
    network = newNetwork;
  }

  public static void resetNetwork() {
    network = generalNetwork;
  }

  // ------------------------------------------------------

  public static Optional<String> getNameByAddress(String address) {
    Optional<String> name = getCache().getVaultByAddress(address)
        .map(VaultEntity::getContract)
        .map(ContractEntity::getName);

    if (name.isEmpty()) {
      name = getCache().getPoolByAddress(address)
          .map(PoolEntity::getContract)
          .map(ContractEntity::getName);
    }
    if (name.isEmpty()) {
      name = getCache().getUniPairByAddress(address)
          .map(UniPairEntity::getContract)
          .map(ContractEntity::getName);
    }
    if (name.isEmpty()) {
      name = getCache().getTokenByAddress(address)
          .map(TokenEntity::getContract)
          .map(ContractEntity::getName);
    }
    return name;
  }

  public static Optional<String> getAddressByName(String name, ContractType type) {
    if (type == ContractType.VAULT) {
      return getCache().getVaultByName(name)
          .map(VaultEntity::getContract)
          .map(ContractEntity::getAddress);
    }
    if (type == ContractType.POOL) {
      return getCache().getPoolByName(name)
          .map(PoolEntity::getContract)
          .map(ContractEntity::getAddress);
    }
    if (type == ContractType.UNI_PAIR) {
      return getCache().getUniPairByName(name)
          .map(UniPairEntity::getContract)
          .map(ContractEntity::getAddress);
    }
    if (type == ContractType.TOKEN) {
      return getCache().getTokenByName(name)
          .map(TokenEntity::getContract)
          .map(ContractEntity::getAddress);
    }
    throw new IllegalStateException("unknown type" + type);
  }

  public static boolean isLp(String address) {
    return getCache().getUniPairByAddress(address).isPresent();
  }

  public static Optional<PoolEntity> poolByVaultName(String name) {
    if (name.endsWith("_V0")) {
      name = name.replace("_V0", "");
    }
    return getCache().getVaultByName(name)
        .map(VaultEntity::getContract)
        .map(ContractEntity::getAddress)
        .flatMap(adr -> getCache().getPoolEntities().stream()
            .filter(pool -> pool.getLpToken().getAddress().equals(adr))
            .findFirst());
  }

  public static Optional<PoolEntity> poolByVaultAddress(String address) {
    // PS_V0 doesn't have pool
    if ("0x59258f4e15a5fc74a7284055a8094f58108dbd4f".equals(address)) {
      return getCache()
          .getPoolByAddress("0x59258f4e15a5fc74a7284055a8094f58108dbd4f");
    }
    Optional<PoolEntity> poolEntity = getCache()
        .getPoolEntities().stream()
        .filter(pool -> pool.getLpToken() != null
            && pool.getLpToken().getAddress().equalsIgnoreCase(address))
        .findFirst();
    if (poolEntity.isPresent()) {
      return poolEntity;
    }
    // try to find pool by name, it should work for old vaults and PS pools
    String vaultName = getNameByAddress(address)
        .orElseThrow(() -> new IllegalStateException("Vault not found for " + address));
    return getCache().getPoolByName("ST_" + vaultName);
  }

  public static Optional<VaultEntity> vaultByPoolAddress(String address) {
    return getCache().getPoolByAddress(address.toLowerCase())
        .map(PoolEntity::getLpToken)
        .flatMap(c -> getCache().getVaultByAddress(c.getAddress()));
  }

  public static boolean isVaultName(String name) {
    return getCache().getVaultByName(name).isPresent();
  }

  public static boolean isPoolName(String name) {
    return getCache().getPoolByName(name).isPresent();
  }

  public static boolean isUniPairName(String name) {
    return getCache().getUniPairByName(name).isPresent();
  }

  public static boolean isTokenName(String name) {
    return getCache().getTokenByName(name).isPresent();
  }

  public static boolean isVaultAddress(String address) {
    return getCache()
        .getVaultByAddress(address.toLowerCase()).isPresent();
  }

  public static boolean isPoolAddress(String address) {
    return getCache()
        .getPoolByAddress(address.toLowerCase()).isPresent();
  }

  public static boolean isUniPairAddress(String address) {
    return getCache()
        .getUniPairByAddress(address.toLowerCase()).isPresent();
  }

  public static boolean isTokenAddress(String address) {
    return getCache()
        .getTokenByAddress(address.toLowerCase()).isPresent();
  }

  public static boolean isPsName(String name) {
    return isPsAddress(getAddressByName(name, ContractType.POOL).orElse(""))
        || isPsAddress(getAddressByName(name, ContractType.VAULT).orElse(""));
  }

  public static boolean isPsAddress(String address) {
    return PS_ADDRESSES.contains(address);
  }

  public static boolean isStableCoin(String name) {
    return ONE_DOLLAR_TOKENS.contains(name);
  }

  public static boolean isTokenCreated(String tokenName, long block) {
    return getCache().getTokenByName(tokenName)
        .map(TokenEntity::getContract)
        .map(ContractEntity::getCreated)
        .filter(c -> c < block)
        .isPresent();
  }

  public static boolean isUniPairCreated(String uniPairName, long block) {
    return getCache().getUniPairByName(uniPairName)
        .map(UniPairEntity::getContract)
        .map(ContractEntity::getCreated)
        .filter(c -> c < block)
        .isPresent();

  }

  public static Tuple2<String, String> tokenAddressesByUniPairAddress(String address) {
    UniPairEntity uniPair = getCache().getUniPairByAddress(address)
        .orElseThrow(() -> new IllegalStateException("Not found uni pair by " + address));
    return new Tuple2<>(
        uniPair.getToken0().getAddress(),
        uniPair.getToken1().getAddress()
    );
  }

  public static String findUniPairForTokens(String token0, String token1) {
    for (UniPairEntity uniPair : getCache().getLpEntities()) {
      String t0 = uniPair.getToken0().getAddress();
      String t1 = uniPair.getToken1().getAddress();
      if (
          (t0.equalsIgnoreCase(token0)
              || t1.equalsIgnoreCase(token0))
              && (
              t0.equalsIgnoreCase(token1)
                  || t1.equalsIgnoreCase(token1)
          )
      ) {
        return uniPair.getContract().getAddress();
      }
    }
    throw new IllegalStateException("Not found LP for " + token0 + " and " + token1);
  }

  public static BigDecimal getDividerByAddress(String address) {
    address = address.toLowerCase();
    long decimals;
    // unique addresses
    if (isPsAddress(address)) {
      decimals = 18L;
    } else if (isPoolAddress(address)) {
      String vaultAddress = getCache().getPoolByAddress(address)
          .orElseThrow().getLpToken().getAddress();
      String vaultName = getNameByAddress(vaultAddress).orElseThrow();
      if (vaultName.endsWith("_V0")) {
        vaultAddress = getAddressByName(vaultName.replace("_V0", ""), ContractType.VAULT)
            .orElseThrow(() -> new IllegalStateException("Not found address for " + vaultName));
      }
      decimals = getCache().getVaultByAddress(vaultAddress)
          .orElseThrow().getDecimals();
    } else if (isVaultAddress(address)) {
      decimals = getCache().getVaultByAddress(address)
          .orElseThrow().getDecimals();
    } else if (isUniPairAddress(address)) {
      decimals = getCache().getUniPairByAddress(address)
          .orElseThrow().getDecimals();
    } else if (isTokenAddress(address)) {
      decimals = getCache().getTokenByAddress(address)
          .orElseThrow().getDecimals();
    } else {
      throw new IllegalStateException("Unknown address " + address);
    }
    return new BigDecimal(10L).pow((int) decimals);
  }

  public static Tuple2<TokenEntity, TokenEntity> getUniPairTokens(String address) {
    UniPairEntity uniPair = getCache().getUniPairByAddress(address)
        .orElseThrow(() -> new IllegalStateException("Not found uniPair by " + address));
    return new Tuple2<>(
        getCache().getTokenByAddress(uniPair.getToken0().getAddress())
            .orElseThrow(() -> new IllegalStateException(
                "Not found token by " + uniPair.getToken0().getAddress())),
        getCache().getTokenByAddress(uniPair.getToken1().getAddress())
            .orElseThrow(() -> new IllegalStateException(
                "Not found token by " + uniPair.getToken1().getAddress()))
    );
  }

  public static boolean isDivisionSequenceSecondDividesFirst(String uniPairAddress,
      String tokenAddress) {
    Tuple2<TokenEntity, TokenEntity> tokens = getUniPairTokens(uniPairAddress);
    if (tokens.component1().getContract().getAddress().equalsIgnoreCase(tokenAddress)) {
      return true;
    } else if (tokens.component2().getContract().getAddress().equalsIgnoreCase(tokenAddress)) {
      return false;
    } else {
      throw new IllegalStateException("UniPair doesn't contain " + tokenAddress);
    }
  }

  public static Optional<String> findKeyTokenForUniPair(String address) {
    return getCache().getUniPairByAddress(address)
        .map(UniPairEntity::getKeyToken)
        .map(TokenEntity::getContract)
        .map(ContractEntity::getAddress);
  }

  public static int getUniPairType(String address) {
    return getCache().getUniPairByAddress(address)
        .map(UniPairEntity::getType)
        .orElse(0);
  }

  public static Optional<String> findUniPairNameForTokenName(String tokenName, long block) {
    TokenToUniPairEntity freshest = null;
    for (TokenToUniPairEntity tokenToUniPairEntity : getCache()
        .getTokenToLpEntities()) {
      if (!tokenToUniPairEntity.getToken().getContract().getName().equals(tokenName)) {
        continue;
      }
      if (freshest == null
          || freshest.getBlockStart() < tokenToUniPairEntity.getBlockStart()) {
        if (tokenToUniPairEntity.getBlockStart() > block) {
          continue;
        }
        freshest = tokenToUniPairEntity;
      }
    }
    return Optional.ofNullable(freshest)
        .map(TokenToUniPairEntity::getUniPair)
        .map(UniPairEntity::getContract)
        .map(ContractEntity::getName);
  }

  public static Optional<ContractEntity> getContractByAddress(String address) {
    address = address.toLowerCase();
    Optional<ContractEntity> contract = getCache().getVaultByAddress(address)
        .map(VaultEntity::getContract);

    if (contract.isEmpty()) {
      contract = getCache().getPoolByAddress(address)
          .map(PoolEntity::getContract);
    }
    if (contract.isEmpty()) {
      contract = getCache().getUniPairByAddress(address)
          .map(UniPairEntity::getContract);
    }
    if (contract.isEmpty()) {
      contract = getCache().getTokenByAddress(address)
          .map(TokenEntity::getContract);
    }
    return contract;
  }

  public static Collection<String> vaultNames() {
    return getCache().getAllVaultNames();
  }

  public static List<String> getSubscriptions() {
    if (ETH_NETWORK.equals(network)) {
      return getEthSubscriptions();
    } else if (BSC_NETWORK.equals(network)) {
      return List.of();
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  private static List<String> getEthSubscriptions() {
    Set<String> contracts = new HashSet<>();

    // hard work parsing
    contracts.add(ETH_CONTROLLER);

    // FARM token Mint event parsing + transfers parsing
    contracts.add(ContractConstants.FARM_TOKEN);

    // harvest events
    contracts.addAll(getCache().getAllVaults().stream()
        .map(v -> v.getContract().getAddress())
        .collect(Collectors.toList()));
    contracts.addAll(getCache().getAllPools().stream()
        .map(v -> v.getContract().getAddress())
        .collect(Collectors.toList()));
    // price parsing
    contracts.addAll(getCache().getLpEntities().stream()
        .filter(u -> u.getKeyToken() != null)
        .map(UniPairEntity::getContract)
        .map(ContractEntity::getAddress)
        .collect(Collectors.toList()));
    // uni events
    contracts.addAll(PARSABLE_UNI_PAIRS);

    return new ArrayList<>(contracts);
  }

  public static Collection<VaultEntity> getAllVaults() {
    return getCache().getAllVaults();
  }

  public static Collection<PoolEntity> getAllPools() {
    return getCache().getAllPools();
  }

  public static Collection<TokenEntity> getAllTokens() {
    return getCache().getAllTokens();
  }

  public static Collection<UniPairEntity> getAllUniPairs() {
    return getCache().getAllUniPairs();
  }

  public static Set<String> getAllContractAddresses() {
    return getCache().getAllContractAddresses();
  }

  private static ContractsCache getCache() {
    return ContractLoader.getCache(network);
  }
}
