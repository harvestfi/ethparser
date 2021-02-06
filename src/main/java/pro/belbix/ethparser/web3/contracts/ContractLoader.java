package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.FunctionsNames.CONTROLLER;
import static pro.belbix.ethparser.web3.FunctionsNames.GOVERNANCE;
import static pro.belbix.ethparser.web3.FunctionsNames.LP_TOKEN;
import static pro.belbix.ethparser.web3.FunctionsNames.OWNER;
import static pro.belbix.ethparser.web3.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.FunctionsNames.STRATEGY;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.KEY_BLOCKS_FOR_LOADING;
import static pro.belbix.ethparser.web3.contracts.HarvestPoolAddresses.POOLS;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.eth.ContractEntity;
import pro.belbix.ethparser.entity.eth.ContractTypeEntity;
import pro.belbix.ethparser.entity.eth.ContractTypeEntity.Type;
import pro.belbix.ethparser.entity.eth.PoolEntity;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.entity.eth.UniPairEntity;
import pro.belbix.ethparser.entity.eth.VaultEntity;
import pro.belbix.ethparser.entity.eth.VaultToPoolEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.SubscriptionsProperties;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.ContractTypeRepository;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.TokenRepository;
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
    private final VaultToPoolRepository vaultToPoolRepository;
    private final SubscriptionsProperties subscriptionsProperties;

    private long currentBlock;
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

    public ContractLoader(AppProperties appProperties,
                          FunctionsUtils functionsUtils,
                          EthBlockService ethBlockService,
                          ContractRepository contractRepository,
                          ContractTypeRepository contractTypeRepository,
                          PoolRepository poolRepository,
                          VaultRepository vaultRepository,
                          UniPairRepository uniPairRepository,
                          TokenRepository tokenRepository,
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
        this.vaultToPoolRepository = vaultToPoolRepository;
        this.subscriptionsProperties = subscriptionsProperties;
    }

    @PostConstruct
    private void init() {
        currentBlock = ethBlockService.getLastBlock();
        vaultType = findOrCreateContractType(Type.VAULT);
        poolType = findOrCreateContractType(Type.POOL);
        uniPairType = findOrCreateContractType(Type.UNI_PAIR);
        infrastructureType = findOrCreateContractType(Type.INFRASTRUCTURE);
        tokenType = findOrCreateContractType(Type.TOKEN);
        if(appProperties.isDevMod()) {
            load();
        }
        subscriptionsProperties.init();
    }

    public synchronized void load() {
        if(loaded) {
            log.info("Contracts already loaded");
            return;
        }
        log.info("Start load contracts on block {}", currentBlock);
        loadVaults();
        loadPools();
        loadUniPairs();
        loadTokens();
        linkVaultToPools();
        log.info("Contracts loading ended");
    }

    public void loadKeyBlocks() {
        for (Integer block : KEY_BLOCKS_FOR_LOADING) {
            currentBlock = block;
            load();
        }
    }

    private void loadTokens() {
        for (TokenInfo tokenInfo : Tokens.tokenInfos) {
            if (tokenInfo.getCreatedOnBlock() > currentBlock) {
                log.info("Token not created yet, skip {}", tokenInfo.getTokenName());
            }
            ContractEntity tokenContract = findOrCreateContract(
                tokenInfo.getTokenAddress(),
                tokenInfo.getTokenName(),
                tokenType,
                tokenInfo.getCreatedOnBlock(),
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
            tokensCacheByAddress.put(tokenEntity.getContract().getAddress(), tokenEntity);
            tokensCacheByName.put(tokenInfo.getTokenName(), tokenEntity);
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
        for (Contract uniPair : UniPairAddresses.UNI_PAIRS) {
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
        uniPairEntity.setUpdatedBlock(currentBlock);
    }

    private void linkVaultToPools() {
        log.info("Start link pools to vaults on block {}", currentBlock);
        for (PoolEntity poolEntity : poolsCacheByName.values()) {
            if (poolEntity.getLpToken() == null) {
                continue;
            }
            ContractEntity currentLpToken = poolEntity.getLpToken();
            String lpAddress = currentLpToken.getAddress();
            if (currentLpToken.getType().getType() == Type.VAULT.getId()) {
                VaultEntity vaultEntity = vaultsCacheByAddress.get(lpAddress);
                if (vaultEntity == null) {
                    log.warn("Not found vault for address {}", lpAddress);
                    continue;
                }
                VaultToPoolEntity vaultToPoolEntity = findOrCreateVaultToPool(vaultEntity, poolEntity);
                vaultToPoolsCache.put(vaultToPoolEntity.getId(), vaultToPoolEntity);
            } else if (currentLpToken.getType().getType() == Type.UNI_PAIR.getId()) {
                //todo create another one link entity
                log.info("Uni lp links temporally disabled");
//                UniPairEntity uniPairEntity = uniPairsCacheByAddress.get(lpAddress);
//                if (uniPairEntity == null) {
//                    log.warn("Not found vault for address {}", lpAddress);
//                    continue;
//                }
            } else {
                log.error("Unknown lp token type {} in pool {}",
                    currentLpToken.getType().getType(), poolEntity.getContract().getName());
            }
        }
    }

    private VaultToPoolEntity findOrCreateVaultToPool(VaultEntity vaultEntity, PoolEntity poolEntity) {
        VaultToPoolEntity vaultToPoolEntity =
            vaultToPoolRepository.findFirstByVaultAndPool(vaultEntity, poolEntity);
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

    private ContractEntity findOrCreateContract(String address,
                                                String name,
                                                ContractTypeEntity type,
                                                long created,
                                                boolean rewrite) {
        if (address == null || address.isBlank()) {
            return null;
        }
        ContractEntity entity = contractRepository.findFirstByAddress(address);
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
            if (!name.equals(entity.getName()) || !type.getType().equals(entity.getType().getType())) {
                entity.setName(name);
                entity.setType(type);
                contractRepository.save(entity);
            }
        }
        return entity;
    }

    private ContractTypeEntity findOrCreateContractType(Type type) {
        if (type == null) {
            return null;
        }
        return contractTypeRepository.findById(type.getId())
            .orElseGet(() -> {
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
