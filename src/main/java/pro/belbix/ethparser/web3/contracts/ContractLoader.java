package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.FunctionsNames.CONTROLLER;
import static pro.belbix.ethparser.web3.FunctionsNames.FACTORY;
import static pro.belbix.ethparser.web3.FunctionsNames.GOVERNANCE;
import static pro.belbix.ethparser.web3.FunctionsNames.LP_TOKEN;
import static pro.belbix.ethparser.web3.FunctionsNames.MOONISWAP_FACTORY_GOVERNANCE;
import static pro.belbix.ethparser.web3.FunctionsNames.OWNER;
import static pro.belbix.ethparser.web3.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.FunctionsNames.STRATEGY;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.KEY_BLOCKS_FOR_LOADING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MOONISWAP_FACTORY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PAIR_TYPE_ONEINCHE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PAIR_TYPE_SUSHI;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PAIR_TYPE_UNISWAP;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.SUSHI_FACTORY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.UNISWAP_FACTORY;
import static pro.belbix.ethparser.web3.contracts.HarvestPoolAddresses.POOLS;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.ContractTypeEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.TokenToUniPairEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.contracts.VaultToPoolEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.SubscriptionsProperties;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.ContractTypeRepository;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.TokenRepository;
import pro.belbix.ethparser.repositories.eth.TokenToUniPairRepository;
import pro.belbix.ethparser.repositories.eth.UniPairRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.repositories.eth.VaultToPoolRepository;
import pro.belbix.ethparser.web3.AddressType;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.FunctionsNames;
import pro.belbix.ethparser.web3.FunctionsUtils;

@Service
@Log4j2
public class ContractLoader {

    private final AppProperties appProperties;
    private final FunctionsUtils functionsUtils;
    private final EthBlockService ethBlockService;
    private final ContractRepository contractRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final PoolRepository poolRepository;
    private final VaultRepository vaultRepository;
    private final UniPairRepository uniPairRepository;
    private final TokenRepository tokenRepository;
    private final TokenToUniPairRepository tokenToUniPairRepository;
    private final VaultToPoolRepository vaultToPoolRepository;
    private final SubscriptionsProperties subscriptionsProperties;

    private Long currentBlock;
    boolean loaded = false;
    private ContractTypeEntity vaultType;
    private ContractTypeEntity poolType;
    private ContractTypeEntity uniPairType;
    private ContractTypeEntity infrastructureType;
    private ContractTypeEntity tokenType;

    static final Map<String, PoolEntity> poolsCacheByAddress = new HashMap<>();
    static final Map<String, VaultEntity> vaultsCacheByAddress = new HashMap<>();
    static final Map<String, UniPairEntity> uniPairsCacheByAddress = new HashMap<>();
    static final Map<String, TokenEntity> tokensCacheByAddress = new HashMap<>();
    static final Map<String, PoolEntity> poolsCacheByName = new HashMap<>();
    static final Map<String, VaultEntity> vaultsCacheByName = new HashMap<>();
    static final Map<String, UniPairEntity> uniPairsCacheByName = new HashMap<>();
    static final Map<String, TokenEntity> tokensCacheByName = new HashMap<>();
    static final Map<Integer, VaultToPoolEntity> vaultToPoolsCache = new HashMap<>();
    static final Map<Integer, TokenToUniPairEntity> tokenToUniPairCache = new HashMap<>();

    public ContractLoader(AppProperties appProperties,
                          FunctionsUtils functionsUtils,
                          EthBlockService ethBlockService,
                          ContractRepository contractRepository,
                          ContractTypeRepository contractTypeRepository,
                          PoolRepository poolRepository,
                          VaultRepository vaultRepository,
                          UniPairRepository uniPairRepository,
                          TokenRepository tokenRepository,
                          TokenToUniPairRepository tokenToUniPairRepository,
                          VaultToPoolRepository vaultToPoolRepository,
                          SubscriptionsProperties subscriptionsProperties) {
        this.appProperties = appProperties;
        this.functionsUtils = functionsUtils;
        this.ethBlockService = ethBlockService;
        this.contractRepository = contractRepository;
        this.contractTypeRepository = contractTypeRepository;
        this.poolRepository = poolRepository;
        this.vaultRepository = vaultRepository;
        this.uniPairRepository = uniPairRepository;
        this.tokenRepository = tokenRepository;
        this.tokenToUniPairRepository = tokenToUniPairRepository;
        this.vaultToPoolRepository = vaultToPoolRepository;
        this.subscriptionsProperties = subscriptionsProperties;
    }

    @PostConstruct
    private void init() {
        currentBlock = ethBlockService.getLastBlock();
        vaultType = findOrCreateContractType(ContractType.VAULT);
        poolType = findOrCreateContractType(ContractType.POOL);
        uniPairType = findOrCreateContractType(ContractType.UNI_PAIR);
        infrastructureType = findOrCreateContractType(ContractType.INFRASTRUCTURE);
        tokenType = findOrCreateContractType(ContractType.TOKEN);
        if (appProperties.isDevMod()) {
            load();
        }
    }

    public synchronized void load() {
        if (loaded) {
            log.info("Contracts already loaded");
            return;
        }
        log.info("Start load contracts on block {}", currentBlock);
        loadVaults();
        loadPools();
        loadTokens();
        loadUniPairs();
        linkVaultToPools();
        fillKeyTokenForLps();
        linkUniPairsToTokens();
        log.info("Contracts loading ended");
        // should subscribe only after contract loading
        subscriptionsProperties.init();
        loaded = true;
    }

    public void loadKeyBlocks() {
        for (Integer block : KEY_BLOCKS_FOR_LOADING) {
            currentBlock = block.longValue();
            load();
        }
    }

    private void loadTokens() {
        for (TokenContract contract : TokenAddresses.TOKENS) {
            if (contract.getCreatedOnBlock() > currentBlock) {
                log.info("Token not created yet, skip {}", contract.getName());
            }
            log.info("Load {}", contract.getName());
            ContractEntity tokenContract = findOrCreateContract(
                contract.getAddress(),
                contract.getName(),
                tokenType,
                contract.getCreatedOnBlock(),
                true);
            TokenEntity tokenEntity = tokenRepository.findFirstByContract(tokenContract);
            if (tokenEntity == null) {
                tokenEntity = new TokenEntity();
                tokenEntity.setContract(tokenContract);
                enrichToken(tokenEntity);
                tokenRepository.save(tokenEntity);
            } else if (appProperties.isUpdateContracts()) {
                enrichToken(tokenEntity);
                tokenRepository.save(tokenEntity);
            }
            tokensCacheByAddress.put(contract.getAddress(), tokenEntity);
            tokensCacheByName.put(contract.getName(), tokenEntity);
        }
    }

    private void loadVaults() {
        log.info("Start load vaults on block {}", currentBlock);
        for (Contract vault : HarvestVaultAddresses.VAULTS) {
            if (vault.getCreatedOnBlock() > currentBlock) {
                log.info("Vault {} not created yet, skip", vault.getName());
                continue;
            }
            String name = vault.getName();
            String hash = vault.getAddress();
            log.info("Load {}", name);
            ContractEntity vaultContract =
                findOrCreateContract(hash, name, vaultType, vault.getCreatedOnBlock(), true);
            VaultEntity vaultEntity = vaultRepository.findFirstByContract(vaultContract);
            if (vaultEntity == null) {
                vaultEntity = new VaultEntity();
                vaultEntity.setContract(vaultContract);
                enrichVault(vaultEntity);
                vaultRepository.save(vaultEntity);
            } else if (appProperties.isUpdateContracts()) {
                enrichVault(vaultEntity);
                vaultRepository.save(vaultEntity);
            }
            vaultsCacheByAddress.put(hash, vaultEntity);
            vaultsCacheByName.put(name, vaultEntity);
        }
    }

    private void loadPools() {
        log.info("Start load pools on block {}", currentBlock);
        for (Contract pool : POOLS) {
            if (pool.getCreatedOnBlock() > currentBlock) {
                log.info("Pool {} not created yet, skip", pool.getName());
                continue;
            }
            String name = pool.getName();
            String hash = pool.getAddress();
            log.info("Load {}", name);
            ContractEntity poolContract =
                findOrCreateContract(hash, name, poolType, pool.getCreatedOnBlock(), true);
            PoolEntity poolEntity = poolRepository.findFirstByContract(poolContract);
            if (poolEntity == null) {
                poolEntity = new PoolEntity();
                poolEntity.setContract(poolContract);
                enrichPool(poolEntity);
                poolRepository.save(poolEntity);
            } else if (appProperties.isUpdateContracts()) {
                enrichPool(poolEntity);
                poolRepository.save(poolEntity);
            }
            poolsCacheByAddress.put(hash, poolEntity);
            poolsCacheByName.put(name, poolEntity);
        }
    }

    private void loadUniPairs() {
        log.info("Start load uni pairs on block {}", currentBlock);
        for (LpContract uniPair : UniPairAddresses.UNI_PAIRS) {
            String name = uniPair.getName();
            String hash = uniPair.getAddress();
            log.info("Load {}", name);
            ContractEntity poolContract =
                findOrCreateContract(hash, name, uniPairType, uniPair.getCreatedOnBlock(), true);
            UniPairEntity uniPairEntity = uniPairRepository.findFirstByContract(poolContract);
            if (uniPairEntity == null) {
                uniPairEntity = new UniPairEntity();
                uniPairEntity.setContract(poolContract);
                enrichUniPair(uniPairEntity);
                uniPairRepository.save(uniPairEntity);
            } else if (appProperties.isUpdateContracts()) {
                enrichUniPair(uniPairEntity);
                uniPairRepository.save(uniPairEntity);
            }
            uniPairsCacheByAddress.put(hash, uniPairEntity);
            uniPairsCacheByName.put(name, uniPairEntity);
        }
    }

    private void enrichToken(TokenEntity tokenEntity) {
        if (appProperties.isOnlyApi()) {
            return;
        }
        String address = tokenEntity.getContract().getAddress();
        tokenEntity.setName(
            functionsUtils.callStrByName(FunctionsNames.NAME, address, currentBlock).orElse(""));
        tokenEntity.setSymbol(
            functionsUtils.callStrByName(FunctionsNames.SYMBOL, address, currentBlock).orElse(""));
        tokenEntity.setDecimals(
            functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, currentBlock)
                .orElse(BigInteger.ZERO).longValue());
        tokenEntity.setUpdatedBlock(currentBlock);
    }

    private void enrichVault(VaultEntity vaultEntity) {
        if (appProperties.isOnlyApi()) {
            return;
        }
        vaultEntity.setUpdatedBlock(currentBlock);
        String address = vaultEntity.getContract().getAddress();
        vaultEntity.setController(findOrCreateContract(
            functionsUtils.callAddressByName(CONTROLLER, address, currentBlock).orElse(""),
            AddressType.CONTROLLER.name(),
            infrastructureType,
            0,
            false
        ));
        vaultEntity.setGovernance(findOrCreateContract(
            functionsUtils.callAddressByName(GOVERNANCE, address, currentBlock).orElse(""),
            AddressType.GOVERNANCE.name(),
            infrastructureType,
            0,
            false
        ));
        //exclude PS vaults
        if (address.equalsIgnoreCase("0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50")
            || address.equalsIgnoreCase("0x59258F4e15A5fC74A7284055A8094F58108dbD4f")) {
            vaultEntity.setName("PS vault");
            vaultEntity.setDecimals(18L);
            return;
        }
        vaultEntity.setStrategy(findOrCreateContract(
            functionsUtils.callAddressByName(STRATEGY, address, currentBlock).orElse(""),
            AddressType.UNKNOWN_STRATEGY.name(),
            infrastructureType,
            0,
            false
        ));
        vaultEntity.setUnderlying(findOrCreateContract(
            functionsUtils.callAddressByName(UNDERLYING, address, currentBlock).orElse(""),
            AddressType.UNKNOWN_UNDERLYING.name(),
            infrastructureType,
            0,
            false
        ));
        vaultEntity.setName(
            functionsUtils.callStrByName(FunctionsNames.NAME, address, currentBlock).orElse(""));
        vaultEntity.setSymbol(
            functionsUtils.callStrByName(FunctionsNames.SYMBOL, address, currentBlock).orElse(""));
        vaultEntity.setDecimals(
            functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, currentBlock)
                .orElse(BigInteger.ZERO).longValue());
        vaultEntity.setUnderlyingUnit(
            functionsUtils.callIntByName(FunctionsNames.UNDERLYING_UNIT, address, currentBlock)
                .orElse(BigInteger.ZERO).longValue());
    }

    private void enrichPool(PoolEntity poolEntity) {
        if (appProperties.isOnlyApi()) {
            return;
        }
        String address = poolEntity.getContract().getAddress();
        poolEntity.setController(findOrCreateContract(
            functionsUtils.callAddressByName(CONTROLLER, address, currentBlock).orElse(""),
            AddressType.CONTROLLER.name(),
            infrastructureType,
            0,
            false
        ));
        poolEntity.setGovernance(findOrCreateContract(
            functionsUtils.callAddressByName(GOVERNANCE, address, currentBlock).orElse(""),
            AddressType.GOVERNANCE.name(),
            infrastructureType,
            0,
            false
        ));
        poolEntity.setOwner(findOrCreateContract(
            functionsUtils.callAddressByName(OWNER, address, currentBlock).orElse(""),
            AddressType.OWNER.name(),
            infrastructureType,
            0,
            false
        ));
        poolEntity.setLpToken(findOrCreateContract(
            functionsUtils.callAddressByName(LP_TOKEN, address, currentBlock).orElse(""),
            AddressType.UNKNOWN_VAULT.name(),
            infrastructureType,
            0,
            false
        ));
        poolEntity.setRewardToken(findOrCreateContract(
            functionsUtils.callAddressByName(REWARD_TOKEN, address, currentBlock).orElse(""),
            AddressType.UNKNOWN_REWARD_TOKEN.name(),
            infrastructureType,
            0,
            false
        ));
        poolEntity.setUpdatedBlock(currentBlock);
    }

    private void enrichUniPair(UniPairEntity uniPairEntity) {
        if (appProperties.isOnlyApi()) {
            return;
        }
        String address = uniPairEntity.getContract().getAddress();
        uniPairEntity.setDecimals(
            functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, currentBlock)
                .orElse(BigInteger.ZERO).longValue());
        uniPairEntity.setToken0(findOrCreateContract(
            functionsUtils.callAddressByName(TOKEN0, address, currentBlock).orElse(""),
            AddressType.UNKNOWN_TOKEN.name(),
            tokenType,
            0,
            false
        ));
        uniPairEntity.setToken1(findOrCreateContract(
            functionsUtils.callAddressByName(TOKEN1, address, currentBlock).orElse(""),
            AddressType.UNKNOWN_TOKEN.name(),
            tokenType,
            0,
            false
        ));

        uniPairEntity.setType(defineUniPairType(address));
        uniPairEntity.setUpdatedBlock(currentBlock);
    }

    private int defineUniPairType(String address) {
        int type = 0;
        String factoryAdr = null;
        try {
            factoryAdr = functionsUtils.callAddressByName(FACTORY, address, currentBlock)
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
                    .callAddressByName(MOONISWAP_FACTORY_GOVERNANCE, address, currentBlock)
                    .orElse("");
            } catch (Exception ignored) {
            }

            if (MOONISWAP_FACTORY.equalsIgnoreCase(factoryAdr)) {
                type = PAIR_TYPE_ONEINCHE;
            }
        }
        return type;
    }

    private void linkVaultToPools() {
        log.info("Start link pools to vaults on block {}", currentBlock);
        for (PoolEntity poolEntity : poolsCacheByName.values()) {
            if (poolEntity.getLpToken() == null) {
                continue;
            }
            ContractEntity currentLpToken = poolEntity.getLpToken();
            String lpAddress = currentLpToken.getAddress();
            if (currentLpToken.getType().getType() == ContractType.VAULT.getId()) {
                VaultEntity vaultEntity = vaultsCacheByAddress.get(lpAddress);
                if (vaultEntity == null) {
                    log.warn("Not found vault for address {}", lpAddress);
                    continue;
                }
                VaultToPoolEntity vaultToPoolEntity = findOrCreateVaultToPool(vaultEntity, poolEntity);
                if (vaultToPoolEntity == null) {
                    log.warn("Not found vault to pool {}", lpAddress);
                    continue;
                }
                vaultToPoolsCache.put(vaultToPoolEntity.getId(), vaultToPoolEntity);
            } else if (currentLpToken.getType().getType() == ContractType.UNI_PAIR.getId()) {
                //todo create another one link entity
                log.info("Uni lp links temporally disabled");
//                UniPairEntity uniPairEntity = uniPairsCacheByAddress.get(lpAddress);
//                if (uniPairEntity == null) {
//                    log.warn("Not found vault for address {}", lpAddress);
//                    continue;
//                }
            } else {
                String poolName = poolEntity.getContract().getName();
                // PS pool link not used
                if (currentLpToken.getType().getType() == ContractType.TOKEN.getId()
                    && (poolName.equals("ST_PS")
                    || poolName.equals("ST_PS_V0"))
                ) {
                    continue;
                }
                log.error("Unknown lp token type {} in pool {}",
                    currentLpToken.getType().getType(), poolName);
            }
        }
    }

    private void fillKeyTokenForLps() {
        log.info("Start fill key tokens for LPs on block {}", currentBlock);
        for (LpContract lpContract : UniPairAddresses.UNI_PAIRS) {

            String keyTokenName = lpContract.getKeyToken();
            if (keyTokenName.isBlank()) {
                continue;
            }
            TokenEntity tokenEntity = tokensCacheByName.get(keyTokenName);
            if (tokenEntity == null) {
                log.warn("Not found token for name " + keyTokenName);
                continue;
            }

            UniPairEntity uniPairEntity = uniPairsCacheByAddress.get(lpContract.getAddress());
            uniPairEntity.setKeyToken(tokenEntity);
            uniPairRepository.save(uniPairEntity);
        }
    }

    private void linkUniPairsToTokens() {
        log.info("Start link UniPairs to Tokens on block {}", currentBlock);
        for (TokenContract tokenContract : TokenAddresses.TOKENS) {
            for (Entry<String, Integer> lp : tokenContract.getLps().entrySet()) {
                UniPairEntity uniPair = uniPairsCacheByName.get(lp.getKey());
                if (uniPair == null) {
                    log.error("Not found uni pair for " + lp.getKey());
                    continue;
                }
                TokenEntity tokenEntity = tokensCacheByAddress.get(tokenContract.getAddress());
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
                // can create an object as key
                tokenToUniPairCache.put(link.getId(), link);
            }
        }
    }

    private VaultToPoolEntity findOrCreateVaultToPool(VaultEntity vaultEntity, PoolEntity poolEntity) {
        VaultToPoolEntity vaultToPoolEntity =
            vaultToPoolRepository.findFirstByVaultAndPool(vaultEntity, poolEntity);
        if (appProperties.isOnlyApi()) {
            return vaultToPoolEntity;
        }
        if (vaultToPoolEntity == null) {
            if (vaultToPoolRepository.findFirstByVault(vaultEntity) != null) {
                log.info("We already had linked vault " + vaultEntity.getContract().getName());
            }
            if (vaultToPoolRepository.findFirstByPool(poolEntity) != null) {
                log.info("We already had linked pool " + poolEntity.getContract().getName());
            }
            vaultToPoolEntity = new VaultToPoolEntity();
            vaultToPoolEntity.setPool(poolEntity);
            vaultToPoolEntity.setVault(vaultEntity);
            vaultToPoolEntity.setBlockStart(currentBlock);
            vaultToPoolRepository.save(vaultToPoolEntity);
            log.info("Create new {} to {} link",
                vaultEntity.getContract().getName(), poolEntity.getContract().getName());
        }
        return vaultToPoolEntity;
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
                                                ContractTypeEntity type,
                                                long created,
                                                boolean rewrite) {
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
            log.info("Created new contract {}", name);
            contractRepository.save(entity);
        } else if (rewrite) {
            // for db optimization
            if (!name.equals(entity.getName())
                || !type.getType().equals(entity.getType().getType())
                || created != entity.getCreated()
            ) {
                entity.setName(name);
                entity.setType(type);
                entity.setCreated(created);
                contractRepository.save(entity);
            }
        }
        return entity;
    }

    private ContractTypeEntity findOrCreateContractType(ContractType type) {
        if (type == null) {
            return null;
        }
        return contractTypeRepository.findById(type.getId())
            .orElseGet(() -> {
                if (appProperties.isOnlyApi()) {
                    return null;
                }
                ContractTypeEntity contractTypeEntity = new ContractTypeEntity();
                contractTypeEntity.setType(type.getId());
                contractTypeRepository.save(contractTypeEntity);
                log.info("Created new contract type {}", type);
                return contractTypeEntity;
            });
    }

    static Optional<PoolEntity> getPoolByAddress(String address) {
        return Optional.ofNullable(poolsCacheByAddress.get(address.toLowerCase()));
    }

    static Optional<VaultEntity> getVaultByAddress(String address) {
        return Optional.ofNullable(vaultsCacheByAddress.get(address.toLowerCase()));
    }

    static Optional<UniPairEntity> getUniPairByAddress(String address) {
        return Optional.ofNullable(uniPairsCacheByAddress.get(address.toLowerCase()));
    }

    static Optional<TokenEntity> getTokenByAddress(String address) {
        return Optional.ofNullable(tokensCacheByAddress.get(address.toLowerCase()));
    }

    static Optional<PoolEntity> getPoolByName(String name) {
        return Optional.ofNullable(poolsCacheByName.get(name));
    }

    static Optional<VaultEntity> getVaultByName(String name) {
        return Optional.ofNullable(vaultsCacheByName.get(name));
    }

    static Optional<UniPairEntity> getUniPairByName(String name) {
        return Optional.ofNullable(uniPairsCacheByName.get(name));
    }

    static Optional<TokenEntity> getTokenByName(String name) {
        return Optional.ofNullable(tokensCacheByName.get(name));
    }
}
