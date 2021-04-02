package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.CONTROLLER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.FACTORY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GOVERNANCE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.LP_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MOONISWAP_FACTORY_GOVERNANCE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.OWNER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.STRATEGY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MOONISWAP_FACTORY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PAIR_TYPE_ONEINCHE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PAIR_TYPE_SUSHI;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PAIR_TYPE_UNISWAP;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.SUSHI_FACTORY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.UNISWAP_FACTORY;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.TokenToUniPairEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.TokenRepository;
import pro.belbix.ethparser.repositories.eth.TokenToUniPairRepository;
import pro.belbix.ethparser.repositories.eth.UniPairRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.web3.AddressType;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionsNames;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
@Log4j2
public class ContractLoader {

  private final AppProperties appProperties;
  private final FunctionsUtils functionsUtils;
  private final EthBlockService ethBlockService;
  private final ContractRepository contractRepository;
  private final PoolRepository poolRepository;
  private final VaultRepository vaultRepository;
  private final UniPairRepository uniPairRepository;
  private final TokenRepository tokenRepository;
  private final TokenToUniPairRepository tokenToUniPairRepository;
  private final SourceResolver sourceResolver;

  Set<String> loaded = new HashSet<>();

  private static final ContractsCache ethCache = new ContractsCache();
  private static final ContractsCache bscCache = new ContractsCache();

  public ContractLoader(AppProperties appProperties,
      FunctionsUtils functionsUtils,
      EthBlockService ethBlockService,
      ContractRepository contractRepository,
      PoolRepository poolRepository,
      VaultRepository vaultRepository,
      UniPairRepository uniPairRepository,
      TokenRepository tokenRepository,
      TokenToUniPairRepository tokenToUniPairRepository,
      SourceResolver sourceResolver) {
    this.appProperties = appProperties;
    this.functionsUtils = functionsUtils;
    this.ethBlockService = ethBlockService;
    this.contractRepository = contractRepository;
    this.poolRepository = poolRepository;
    this.vaultRepository = vaultRepository;
    this.uniPairRepository = uniPairRepository;
    this.tokenRepository = tokenRepository;
    this.tokenToUniPairRepository = tokenToUniPairRepository;
    this.sourceResolver = sourceResolver;

    //todo TEMPORALLY! remove with removing all static code
    ContractUtils.setGeneralNetwork(appProperties.getNetwork());
  }

  public synchronized void load(String... networks) {
    if (networks == null || networks.length == 0) {
      networks = new String[]{ETH_NETWORK};
    }
    for (String network : networks) {
      if (loaded.contains(network)) {
        log.info("Contracts for {} already loaded", network);
        continue;
      }
      loaded.add(network); // on the first line for avoiding hundreds attempts to load broken config
      functionsUtils.setCurrentNetwork(network);
      long block = ethBlockService.getLastBlock(network);
      log.info("Start load contracts for {} on block {}", network, block);
      loadNetwork(network, block);
      getCache(network).initAllContract();
      log.info("Contracts loading for {} ended", network);
    }
  }

  private void loadNetwork(String network, long block) {
    loadVaults(network, block);
    loadPools(network, block);
    loadTokens(network, block);
    loadUniPairs(network, block);
    fillKeyTokenForLps(network, block);
    linkUniPairsToTokens(network, block);
  }

  private void loadTokens(String network, long block) {
    for (TokenContract contract : sourceResolver.getTokens(network)) {
      if (contract.getCreatedOnBlock() > block) {
        log.info("Token not created yet, skip {}", contract.getName());
      }
      log.debug("Load {}", contract.getName());
      ContractEntity tokenContract = findOrCreateContract(
          contract.getAddress(),
          contract.getName(),
          ContractType.TOKEN.getId(),
          contract.getCreatedOnBlock(),
          true,
          network);
      TokenEntity tokenEntity = tokenRepository.findFirstByContract(tokenContract);
      if (tokenEntity == null) {
        tokenEntity = new TokenEntity();
        tokenEntity.setContract(tokenContract);
        enrichToken(tokenEntity, block);
        tokenRepository.save(tokenEntity);
      } else if (appProperties.isUpdateContracts()) {
        enrichToken(tokenEntity, block);
        tokenRepository.save(tokenEntity);
      }

      getCache(network).addToken(tokenEntity);
    }
  }

  private void loadVaults(String network, long block) {
    log.info("Start load vaults on block {}", block);
    for (Contract vault : sourceResolver.getVaults(network)) {
      if (vault.getCreatedOnBlock() > block) {
        log.info("Vault {} not created yet, skip", vault.getName());
        continue;
      }
      log.debug("Load {}", vault.getName());
      ContractEntity vaultContract =
          findOrCreateContract(
              vault.getAddress(),
              vault.getName(),
              ContractType.VAULT.getId(),
              vault.getCreatedOnBlock(),
              true,
              network);
      VaultEntity vaultEntity = vaultRepository.findFirstByContract(vaultContract);
      if (vaultEntity == null) {
        vaultEntity = new VaultEntity();
        vaultEntity.setContract(vaultContract);
        enrichVault(vaultEntity, block, network);
        vaultRepository.save(vaultEntity);
      } else if (appProperties.isUpdateContracts()) {
        enrichVault(vaultEntity, block, network);
        vaultRepository.save(vaultEntity);
      }

      getCache(network).addVault(vaultEntity);
    }
  }

  private void loadPools(String network, long block) {
    log.info("Start load pools on block {}", block);
    for (Contract pool : sourceResolver.getPools(network)) {
      if (pool.getCreatedOnBlock() > block) {
        log.info("Pool {} not created yet, skip", pool.getName());
        continue;
      }
      String name = pool.getName();
      String hash = pool.getAddress();
      log.debug("Load {}", name);
      ContractEntity poolContract =
          findOrCreateContract(
              hash,
              name,
              ContractType.POOL.getId(),
              pool.getCreatedOnBlock(),
              true,
              network);
      PoolEntity poolEntity = poolRepository.findFirstByContract(poolContract);
      if (poolEntity == null) {
        poolEntity = new PoolEntity();
        poolEntity.setContract(poolContract);
        enrichPool(poolEntity, block, network);
        poolRepository.save(poolEntity);
      } else if (appProperties.isUpdateContracts()) {
        enrichPool(poolEntity, block, network);
        poolRepository.save(poolEntity);
      }

      getCache(network).addPool(poolEntity);
    }
  }

  private void loadUniPairs(String network, long block) {
    log.info("Start load uni pairs on block {}", block);
    for (LpContract uniPair : sourceResolver.getLps(network)) {
      log.debug("Load {}", uniPair.getName());
      ContractEntity poolContract = findOrCreateContract(
          uniPair.getAddress(),
          uniPair.getName(),
          ContractType.UNI_PAIR.getId(),
          uniPair.getCreatedOnBlock(),
          true,
          network);
      UniPairEntity uniPairEntity = uniPairRepository.findFirstByContract(poolContract);
      if (uniPairEntity == null) {
        uniPairEntity = new UniPairEntity();
        uniPairEntity.setContract(poolContract);
        enrichUniPair(uniPairEntity, block, network);
        uniPairRepository.save(uniPairEntity);
      } else if (appProperties.isUpdateContracts()) {
        enrichUniPair(uniPairEntity, block, network);
        uniPairRepository.save(uniPairEntity);
      }

      getCache(network).addLp(uniPairEntity);
    }
  }

  private void enrichToken(TokenEntity tokenEntity, long block) {
    if (appProperties.isOnlyApi()) {
      return;
    }
    String address = tokenEntity.getContract().getAddress();
    tokenEntity.setName(
        functionsUtils.callStrByName(FunctionsNames.NAME, address, block).orElse(""));
    tokenEntity.setSymbol(
        functionsUtils.callStrByName(FunctionsNames.SYMBOL, address, block).orElse(""));
    tokenEntity.setDecimals(
        functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, block)
            .orElse(BigInteger.ZERO).longValue());
    tokenEntity.setUpdatedBlock(block);
  }

  private void enrichVault(VaultEntity vaultEntity, long block, String network) {
    if (appProperties.isOnlyApi()) {
      return;
    }
    vaultEntity.setUpdatedBlock(block);
    String address = vaultEntity.getContract().getAddress();
    vaultEntity.setController(findOrCreateContract(
        functionsUtils.callAddressByName(CONTROLLER, address, block).orElse(""),
        AddressType.CONTROLLER.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    vaultEntity.setGovernance(findOrCreateContract(
        functionsUtils.callAddressByName(GOVERNANCE, address, block).orElse(""),
        AddressType.GOVERNANCE.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    //exclude PS vaults
    if (address.equalsIgnoreCase("0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50")
        || address.equalsIgnoreCase("0x59258F4e15A5fC74A7284055A8094F58108dbD4f")) {
      vaultEntity.setName("PS vault");
      vaultEntity.setDecimals(18L);
      return;
    }
    vaultEntity.setStrategy(findOrCreateContract(
        functionsUtils.callAddressByName(STRATEGY, address, block).orElse(""),
        AddressType.UNKNOWN_STRATEGY.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    vaultEntity.setUnderlying(findOrCreateContract(
        functionsUtils.callAddressByName(UNDERLYING, address, block).orElse(""),
        AddressType.UNKNOWN_UNDERLYING.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    vaultEntity.setName(
        functionsUtils.callStrByName(FunctionsNames.NAME, address, block).orElse(""));
    vaultEntity.setSymbol(
        functionsUtils.callStrByName(FunctionsNames.SYMBOL, address, block).orElse(""));
    vaultEntity.setDecimals(
        functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, block)
            .orElse(BigInteger.ZERO).longValue());
    vaultEntity.setUnderlyingUnit(
        functionsUtils.callIntByName(FunctionsNames.UNDERLYING_UNIT, address, block)
            .orElse(BigInteger.ZERO).longValue());
  }

  private void enrichPool(PoolEntity poolEntity, long block, String network) {
    if (appProperties.isOnlyApi()) {
      return;
    }
    String address = poolEntity.getContract().getAddress();
    poolEntity.setController(findOrCreateContract(
        functionsUtils.callAddressByName(CONTROLLER, address, block).orElse(""),
        AddressType.CONTROLLER.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    poolEntity.setGovernance(findOrCreateContract(
        functionsUtils.callAddressByName(GOVERNANCE, address, block).orElse(""),
        AddressType.GOVERNANCE.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    poolEntity.setOwner(findOrCreateContract(
        functionsUtils.callAddressByName(OWNER, address, block).orElse(""),
        AddressType.OWNER.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    poolEntity.setLpToken(findOrCreateContract(
        functionsUtils.callAddressByName(LP_TOKEN, address, block).orElse(""),
        AddressType.UNKNOWN_VAULT.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    poolEntity.setRewardToken(findOrCreateContract(
        functionsUtils.callAddressByName(REWARD_TOKEN, address, block).orElse(""),
        AddressType.UNKNOWN_REWARD_TOKEN.name(),
        ContractType.INFRASTRUCTURE.getId(),
        0,
        false,
        network
    ));
    poolEntity.setUpdatedBlock(block);
  }

  private void enrichUniPair(UniPairEntity uniPairEntity, long block, String network) {
    if (appProperties.isOnlyApi()) {
      return;
    }
    String address = uniPairEntity.getContract().getAddress();
    uniPairEntity.setDecimals(
        functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, block)
            .orElse(BigInteger.ZERO).longValue());
    uniPairEntity.setToken0(findOrCreateContract(
        functionsUtils.callAddressByName(TOKEN0, address, block).orElse(""),
        AddressType.UNKNOWN_TOKEN.name(),
        ContractType.TOKEN.getId(),
        0,
        false,
        network
    ));
    uniPairEntity.setToken1(findOrCreateContract(
        functionsUtils.callAddressByName(TOKEN1, address, block).orElse(""),
        AddressType.UNKNOWN_TOKEN.name(),
        ContractType.TOKEN.getId(),
        0,
        false,
        network
    ));

    uniPairEntity.setType(defineUniPairType(address, block));
    uniPairEntity.setUpdatedBlock(block);
  }

  private int defineUniPairType(String address, long block) {
    int type = 0;
    String factoryAdr = null;
    try {
      factoryAdr = functionsUtils.callAddressByName(FACTORY, address, block)
          .orElse("");
    } catch (Exception ignored) {
    }
    if (UNISWAP_FACTORY.equalsIgnoreCase(factoryAdr)) {
      type = PAIR_TYPE_UNISWAP;
    } else if (SUSHI_FACTORY.equalsIgnoreCase(factoryAdr)) {
      type = PAIR_TYPE_SUSHI;
    } else {
      try {
        factoryAdr = functionsUtils
            .callAddressByName(MOONISWAP_FACTORY_GOVERNANCE, address, block)
            .orElse("");
      } catch (Exception ignored) {
      }

      if (MOONISWAP_FACTORY.equalsIgnoreCase(factoryAdr)) {
        type = PAIR_TYPE_ONEINCHE;
      }
    }
    return type;
  }

  private void fillKeyTokenForLps(String network, long block) {
    log.info("Start fill key tokens for LPs on block {}", block);
    for (LpContract lpContract : sourceResolver.getLps(network)) {

      String keyTokenName = lpContract.getKeyToken();
      if (keyTokenName.isBlank()) {
        continue;
      }
      TokenEntity tokenEntity = getCache(network).getTokenByName(keyTokenName).orElse(null);
      if (tokenEntity == null) {
        log.warn("Not found token for name " + keyTokenName);
        continue;
      }

      UniPairEntity uniPairEntity = getCache(network).getUniPairByAddress(lpContract.getAddress())
          .orElseThrow();
      if (uniPairEntity.getKeyToken() == null ||
          !tokenEntity.getContract().getAddress()
              .equals(uniPairEntity.getKeyToken().getContract().getAddress())) {
        uniPairEntity.setKeyToken(tokenEntity);
        uniPairRepository.save(uniPairEntity);
      }
    }
  }

  private void linkUniPairsToTokens(String network, long block) {
    log.info("Start link UniPairs to Tokens on block {}", block);
    for (TokenContract tokenContract : sourceResolver.getTokens(network)) {
      for (Entry<String, Integer> lp : tokenContract.getLps().entrySet()) {
        UniPairEntity uniPair = getCache(network).getUniPairByName(lp.getKey()).orElse(null);
        if (uniPair == null) {
          log.error("Not found lp for {} on {}", lp.getKey(), network);
          continue;
        }
        TokenEntity tokenEntity = getCache(network).getTokenByAddress(tokenContract.getAddress())
            .orElse(null);
        if (tokenEntity == null) {
          log.error("Not found token for " + tokenContract.getAddress());
          continue;
        }
        TokenToUniPairEntity link =
            findOrCreateTokenToUniPair(tokenEntity, uniPair, lp.getValue());
        if (link == null) {
          log.warn("Not found token to uni {}", lp);
          continue;
        }
        getCache(network).addTokenToLp(link);
      }
    }
  }

  private TokenToUniPairEntity findOrCreateTokenToUniPair(
      TokenEntity token,
      UniPairEntity uniPair,
      long blockStart) {
    TokenToUniPairEntity tokenToUniPairEntity =
        tokenToUniPairRepository.findFirstByTokenAndUniPair(token, uniPair);
    if (appProperties.isOnlyApi()) {
      return tokenToUniPairEntity;
    }
    if (tokenToUniPairEntity == null) {
      if (tokenToUniPairRepository.findFirstByToken(token) != null) {
        log.info("We already had linked " + token.getContract().getName());
      }
      if (tokenToUniPairRepository.findFirstByUniPair(uniPair) != null) {
        log.info("We already had linked " + uniPair.getContract().getName());
      }
      tokenToUniPairEntity = new TokenToUniPairEntity();
      tokenToUniPairEntity.setToken(token);
      tokenToUniPairEntity.setUniPair(uniPair);
      tokenToUniPairEntity.setBlockStart(blockStart);
      tokenToUniPairRepository.save(tokenToUniPairEntity);
      log.info("Create new {} to {} link",
          token.getContract().getName(), uniPair.getContract().getName());
    }
    return tokenToUniPairEntity;
  }

  private ContractEntity findOrCreateContract(String address,
      String name,
      int type,
      long created,
      boolean rewrite,
      String network
  ) {
    if (address == null || address.isBlank()) {
      return null;
    }
    ContractEntity entity = contractRepository.findFirstByAddress(address);
    if (appProperties.isOnlyApi()) {
      return entity;
    }
    if (entity == null) {
      entity = new ContractEntity();
      entity.setAddress(address.toLowerCase());
      entity.setName(name);
      entity.setType(type);
      entity.setCreated(created);
      entity.setNetwork(network);
      log.info("Created new contract {}", name);
      contractRepository.save(entity);
    } else if (rewrite) {
      // for db optimization
      if ((!name.equals(entity.getName())
          || type != entity.getType()
          || created != entity.getCreated())
          && !"0x59258f4e15a5fc74a7284055a8094f58108dbd4f".equals(address)
      ) {
        entity.setName(name);
        entity.setType(type);
        entity.setCreated(created);
        contractRepository.save(entity);
      }
    }
    return entity;
  }

  static ContractsCache getCache(String network) {
    switch (network) {
      case ETH_NETWORK:
        return ethCache;
      case BSC_NETWORK:
        return bscCache;
      default:
        throw new IllegalStateException("Unknown network " + network);
    }
  }
}
