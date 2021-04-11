package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLERS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MOONISWAP_FACTORY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MOONISWAP_FACTORY_BSC;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ONE_DOLLAR_TOKENS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_UNI_PAIRS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_ADDRESSES;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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

  private final String network;

  private ContractUtils(String network) {
    this.network = network;
  }

  private static final ContractUtils ETH_INSTANCE = new ContractUtils(ETH_NETWORK);
  private static final ContractUtils BSC_INSTANCE = new ContractUtils(BSC_NETWORK);

  public static ContractUtils getInstance(String network) {
    if (ETH_NETWORK.equals(network)) {
      return ETH_INSTANCE;
    } else if (BSC_NETWORK.equals(network)) {
      return BSC_INSTANCE;
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  public Optional<String> getNameByAddress(String address) {
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

  public Optional<String> getAddressByName(String name, ContractType type) {
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

  public boolean isLp(String address) {
    return getCache().getUniPairByAddress(address).isPresent();
  }

  public Optional<PoolEntity> poolByVaultName(String name) {
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

  public Optional<PoolEntity> poolByVaultAddress(String address) {
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

  public Optional<VaultEntity> vaultByPoolAddress(String address) {
    return getCache().getPoolByAddress(address.toLowerCase())
        .map(PoolEntity::getLpToken)
        .flatMap(c -> getCache().getVaultByAddress(c.getAddress()));
  }

  public boolean isVaultName(String name) {
    return getCache().getVaultByName(name).isPresent();
  }

  public boolean isPoolName(String name) {
    return getCache().getPoolByName(name).isPresent();
  }

  public boolean isUniPairName(String name) {
    return getCache().getUniPairByName(name).isPresent();
  }

  public boolean isTokenName(String name) {
    return getCache().getTokenByName(name).isPresent();
  }

  public boolean isVaultAddress(String address) {
    return getCache()
        .getVaultByAddress(address.toLowerCase()).isPresent();
  }

  public boolean isPoolAddress(String address) {
    return getCache()
        .getPoolByAddress(address.toLowerCase()).isPresent();
  }

  public boolean isUniPairAddress(String address) {
    return getCache()
        .getUniPairByAddress(address.toLowerCase()).isPresent();
  }

  public boolean isTokenAddress(String address) {
    return getCache()
        .getTokenByAddress(address.toLowerCase()).isPresent();
  }

  public boolean isPsName(String name) {
    return isPsAddress(getAddressByName(name, ContractType.POOL).orElse(""))
        || isPsAddress(getAddressByName(name, ContractType.VAULT).orElse(""));
  }

  public boolean isPsAddress(String address) {
    return PS_ADDRESSES.contains(address);
  }

  public boolean isStableCoin(String name) {
    return ONE_DOLLAR_TOKENS.contains(name);
  }

  public boolean isTokenCreated(String tokenName, long block) {
    return getCache().getTokenByName(tokenName)
        .map(TokenEntity::getContract)
        .map(ContractEntity::getCreated)
        .filter(c -> c < block)
        .isPresent();
  }

  public boolean isUniPairCreated(String uniPairName, long block) {
    return getCache().getUniPairByName(uniPairName)
        .map(UniPairEntity::getContract)
        .map(ContractEntity::getCreated)
        .filter(c -> c < block)
        .isPresent();

  }

  public Tuple2<String, String> tokenAddressesByUniPairAddress(String address) {
    UniPairEntity uniPair = getCache().getUniPairByAddress(address)
        .orElseThrow(() -> new IllegalStateException("Not found uni pair by " + address));
    return new Tuple2<>(
        getBaseAddressInsteadOfZero(uniPair.getToken0().getAddress()),
        getBaseAddressInsteadOfZero(uniPair.getToken1().getAddress())
    );
  }

  public String findUniPairForTokens(String token0, String token1) {
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

  public BigDecimal getDividerByAddress(String address) {
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

  public Tuple2<TokenEntity, TokenEntity> getUniPairTokens(String address) {
    UniPairEntity uniPair = getCache().getUniPairByAddress(address)
        .orElseThrow(() -> new IllegalStateException("Not found uniPair by " + address));
    String token0Adr = getBaseAddressInsteadOfZero(uniPair.getToken0().getAddress());
    String token1Adr = getBaseAddressInsteadOfZero(uniPair.getToken1().getAddress());
    return new Tuple2<>(
        getCache().getTokenByAddress(token0Adr)
            .orElseThrow(() -> new IllegalStateException("Not found token by " + token0Adr)),
        getCache().getTokenByAddress(token1Adr)
            .orElseThrow(() -> new IllegalStateException("Not found token by " + token1Adr))
    );
  }

  public String getBaseAddressInsteadOfZero(String address) {
    return ZERO_ADDRESS.equalsIgnoreCase(address) ?
        getBaseNetworkWrappedTokenAddress() :
        address;
  }

  public boolean isDivisionSequenceSecondDividesFirst(String uniPairAddress,
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

  public Optional<String> findKeyTokenForUniPair(String address) {
    return getCache().getUniPairByAddress(address)
        .map(UniPairEntity::getKeyToken)
        .map(TokenEntity::getContract)
        .map(ContractEntity::getAddress);
  }

  public int getUniPairType(String address) {
    return getCache().getUniPairByAddress(address)
        .map(UniPairEntity::getType)
        .orElse(0);
  }

  public Optional<String> findUniPairNameForTokenName(String tokenName, long block) {
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

  public Optional<ContractEntity> getContractByAddress(String address) {
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

  public Collection<String> vaultNames() {
    return getCache().getAllVaultNames();
  }

  public List<String> getSubscriptions() {
    if (ETH_NETWORK.equals(network)) {
      return getEthSubscriptions();
    } else if (BSC_NETWORK.equals(network)) {
      return getBscSubscriptions();
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  private List<String> getEthSubscriptions() {
    Set<String> contracts = new HashSet<>();

    // hard work parsing
    contracts.add(CONTROLLERS.get(ETH_NETWORK));

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
    contracts.addAll(PARSABLE_UNI_PAIRS.get(network));

    return new ArrayList<>(contracts);
  }

  private List<String> getBscSubscriptions() {
    Set<String> contracts = new HashSet<>();

    // hard work parsing
//    contracts.add(BSC_CONTROLLER);

    // harvest events
    contracts.addAll(getCache().getAllVaults().stream()
        .map(v -> v.getContract().getAddress())
        .collect(Collectors.toList()));
    contracts.addAll(getCache().getAllPools().stream()
        .map(v -> v.getContract().getAddress())
        .collect(Collectors.toList()));
//     price parsing
    contracts.addAll(getCache().getLpEntities().stream()
        .filter(u -> u.getKeyToken() != null)
        .map(UniPairEntity::getContract)
        .map(ContractEntity::getAddress)
        .collect(Collectors.toList()));

    return new ArrayList<>(contracts);
  }

  public double parseAmount(BigInteger amount, String address) {
    if (amount == null) {
      return 0.0;
    }
    return new BigDecimal(amount)
        .divide(getDividerByAddress(address), 99, RoundingMode.HALF_UP)
        .doubleValue();
  }

  public boolean isOneInch(String factoryAdr) {
    if (ETH_NETWORK.equals(network)) {
      return MOONISWAP_FACTORY.equalsIgnoreCase(factoryAdr);
    } else if (BSC_NETWORK.equals(network)) {
      return MOONISWAP_FACTORY_BSC.equalsIgnoreCase(factoryAdr);
    }
    return false;
  }

  public String getBaseNetworkWrappedTokenAddress() {
    if (ETH_NETWORK.equals(network)) {
      return "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
    } else if (BSC_NETWORK.equals(network)) {
      return "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c";
    }
    return null;
  }

  public String getSimilarActiveForPrice(String name) {
    if(ETH_NETWORK.equals(network)) {
      return getSimilarActiveForPriceEth(name);
    } else if(BSC_NETWORK.equals(network)) {
      return getSimilarActiveForPriceBsc(name);
    }
    return name;
  }

  public String getSimilarActiveForPriceEth(String name) {
    name = name.replaceFirst("_V0", "");
    switch (name) {
      case "CRV_STETH":
      case "WETH":
        return "ETH";
      case "PS":
      case "iPS":
        return "FARM";
      case "RENBTC":
      case "CRVRENWBTC":
      case "TBTC":
      case "BTC":
      case "CRV_OBTC":
      case "CRV_TBTC":
      case "HBTC":
      case "CRV_HBTC":
      case "CRV_RENBTC":
        return "WBTC";
      case "CRV_EURS":
        return "EURS";
      case "CRV_LINK":
        return "LINK";
      case "SUSHI_HODL":
        return "SUSHI";
    }
    return name;
  }

  public String getSimilarActiveForPriceBsc(String name) {
    //noinspection SwitchStatementWithTooFewBranches
    switch (name) {
      case "RENBTC":
        return "BTCB";
    }
    return name;
  }

  public Collection<VaultEntity> getAllVaults() {
    return getCache().getAllVaults();
  }

  public Collection<PoolEntity> getAllPools() {
    return getCache().getAllPools();
  }

  public Collection<TokenEntity> getAllTokens() {
    return getCache().getAllTokens();
  }

  public Collection<UniPairEntity> getAllUniPairs() {
    return getCache().getAllUniPairs();
  }

  public Set<String> getAllContractAddresses() {
    return getCache().getAllContractAddresses();
  }

  private ContractsCache getCache() {
    return ContractLoader.getCache(network);
  }
}
