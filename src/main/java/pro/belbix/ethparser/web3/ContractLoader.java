package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.web3.FunctionsNames.CONTROLLER;
import static pro.belbix.ethparser.web3.FunctionsNames.GOVERNANCE;
import static pro.belbix.ethparser.web3.FunctionsNames.LP_TOKEN;
import static pro.belbix.ethparser.web3.FunctionsNames.OWNER;
import static pro.belbix.ethparser.web3.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.FunctionsNames.STRATEGY;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.HarvestPoolAddresses.POOLS;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.ContractRepository;
import pro.belbix.ethparser.repositories.ContractTypeRepository;
import pro.belbix.ethparser.repositories.PoolRepository;
import pro.belbix.ethparser.repositories.TokenRepository;
import pro.belbix.ethparser.repositories.UniPairRepository;
import pro.belbix.ethparser.repositories.VaultRepository;
import pro.belbix.ethparser.web3.contracts.HarvestVaultAddresses;
import pro.belbix.ethparser.web3.contracts.TokenInfo;
import pro.belbix.ethparser.web3.contracts.Tokens;
import pro.belbix.ethparser.web3.contracts.UniPairAddresses;

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

    private long lastBlock;
    private ContractTypeEntity vaultType;
    private ContractTypeEntity poolType;
    private ContractTypeEntity uniPairType;
    private ContractTypeEntity infrastructureType;
    private ContractTypeEntity tokenType;

    public static final Map<String, PoolEntity> poolsCacheByAddress = new HashMap<>();
    private static final Map<String, VaultEntity> vaultsCacheByAddress = new HashMap<>();
    private static final Map<String, UniPairEntity> uniPairsCacheByAddress = new HashMap<>();
    private static final Map<String, TokenEntity> tokensCacheByAddress = new HashMap<>();
    private static final Map<String, PoolEntity> poolsCacheByName = new HashMap<>();
    private static final Map<String, VaultEntity> vaultsCacheByName = new HashMap<>();
    private static final Map<String, UniPairEntity> uniPairsCacheByName = new HashMap<>();
    private static final Map<String, TokenEntity> tokensCacheByName = new HashMap<>();

    public ContractLoader(AppProperties appProperties,
                          FunctionsUtils functionsUtils,
                          EthBlockService ethBlockService,
                          ContractRepository contractRepository,
                          ContractTypeRepository contractTypeRepository,
                          PoolRepository poolRepository,
                          VaultRepository vaultRepository,
                          UniPairRepository uniPairRepository,
                          TokenRepository tokenRepository) {
        this.appProperties = appProperties;
        this.functionsUtils = functionsUtils;
        this.ethBlockService = ethBlockService;
        this.contractRepository = contractRepository;
        this.contractTypeRepository = contractTypeRepository;
        this.poolRepository = poolRepository;
        this.vaultRepository = vaultRepository;
        this.uniPairRepository = uniPairRepository;
        this.tokenRepository = tokenRepository;
    }

    @PostConstruct
    private void init() {
        lastBlock = ethBlockService.getLastBlock();
        vaultType = findOrCreateContractType(Type.VAULT);
        poolType = findOrCreateContractType(Type.POOL);
        uniPairType = findOrCreateContractType(Type.UNI_PAIR);
        infrastructureType = findOrCreateContractType(Type.INFRASTRUCTURE);
        tokenType = findOrCreateContractType(Type.TOKEN);
    }

    public void load() {
        log.info("Start load contracts");
        loadPools();
        loadVaults();
        loadUniPairs();
        loadTokens();
        log.info("Contracts loading ended");
    }

    private void loadTokens() {
        for (TokenInfo tokenInfo : Tokens.tokenInfos) {
            ContractEntity tokenContract = findOrCreateContract(
                tokenInfo.getTokenAddress(),
                tokenInfo.getTokenName()
                , tokenType,
                true);
            TokenEntity tokenEntity = tokenRepository.findFirstByAddress(tokenContract);
            if (tokenEntity == null) {
                tokenEntity = new TokenEntity();
                tokenEntity.setAddress(tokenContract);
                enrichToken(tokenEntity);
                tokenRepository.save(tokenEntity);
            } else if (appProperties.isUpdateContracts()) {
                enrichToken(tokenEntity);
                tokenRepository.save(tokenEntity);
            }
            tokensCacheByAddress.put(tokenEntity.getAddress().getAddress(), tokenEntity);
            tokensCacheByName.put(tokenInfo.getTokenName(), tokenEntity);
        }
    }

    private void loadVaults() {
        for (Entry<String, String> entry : HarvestVaultAddresses.VAULTS.entrySet()) {
            String name = entry.getKey();
            String hash = entry.getValue();

            ContractEntity vaultContract =
                findOrCreateContract(hash, name, vaultType, true);
            VaultEntity vaultEntity = vaultRepository.findFirstByAddress(vaultContract);
            if (vaultEntity == null) {
                vaultEntity = new VaultEntity();
                vaultEntity.setAddress(vaultContract);
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
        for (Entry<String, String> entry : POOLS.entrySet()) {
            String name = entry.getKey();
            String hash = entry.getValue();

            ContractEntity poolContract =
                findOrCreateContract(hash, name, poolType, true);
            PoolEntity poolEntity = poolRepository.findFirstByAddress(poolContract);
            if (poolEntity == null) {
                poolEntity = new PoolEntity();
                poolEntity.setAddress(poolContract);
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
        for (Entry<String, String> entry : UniPairAddresses.UNI_PAIRS.entrySet()) {
            String name = entry.getKey();
            String hash = entry.getValue();

            ContractEntity poolContract =
                findOrCreateContract(hash, name, uniPairType, true);
            UniPairEntity uniPairEntity = uniPairRepository.findFirstByAddress(poolContract);
            if (uniPairEntity == null) {
                uniPairEntity = new UniPairEntity();
                uniPairEntity.setAddress(poolContract);
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
        String address = tokenEntity.getAddress().getAddress();
        tokenEntity.setName(
            functionsUtils.callStrByName(FunctionsNames.NAME, address, lastBlock).orElse(""));
        tokenEntity.setSymbol(
            functionsUtils.callStrByName(FunctionsNames.SYMBOL, address, lastBlock).orElse(""));
        tokenEntity.setDecimals(
            functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, lastBlock)
                .orElse(BigInteger.ZERO).longValue());
        tokenEntity.setUpdatedBlock(lastBlock);
    }

    private void enrichVault(VaultEntity vaultEntity) {
        String address = vaultEntity.getAddress().getAddress();
        vaultEntity.setController(findOrCreateContract(
            functionsUtils.callAddressByName(CONTROLLER, address, lastBlock).orElse(""),
            AddressType.CONTROLLER.name(),
            infrastructureType,
            false
        ));
        vaultEntity.setGovernance(findOrCreateContract(
            functionsUtils.callAddressByName(GOVERNANCE, address, lastBlock).orElse(""),
            AddressType.GOVERNANCE.name(),
            infrastructureType,
            false
        ));
        vaultEntity.setStrategy(findOrCreateContract(
            functionsUtils.callAddressByName(STRATEGY, address, lastBlock).orElse(""),
            AddressType.UNKNOWN_STRATEGY.name(),
            infrastructureType,
            false
        ));
        vaultEntity.setUnderlying(findOrCreateContract(
            functionsUtils.callAddressByName(UNDERLYING, address, lastBlock).orElse(""),
            AddressType.UNKNOWN_UNDERLYING.name(),
            infrastructureType,
            false
        ));
        vaultEntity.setName(
            functionsUtils.callStrByName(FunctionsNames.NAME, address, lastBlock).orElse(""));
        vaultEntity.setSymbol(
            functionsUtils.callStrByName(FunctionsNames.SYMBOL, address, lastBlock).orElse(""));
        vaultEntity.setDecimals(
            functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, lastBlock)
                .orElse(BigInteger.ZERO).longValue());
        vaultEntity.setUnderlyingUnit(
            functionsUtils.callIntByName(FunctionsNames.UNDERLYING_UNIT, address, lastBlock)
                .orElse(BigInteger.ZERO).longValue());
        vaultEntity.setUpdatedBlock(lastBlock);
    }

    private void enrichPool(PoolEntity poolEntity) {
        String address = poolEntity.getAddress().getAddress();
        poolEntity.setController(findOrCreateContract(
            functionsUtils.callAddressByName(CONTROLLER, address, lastBlock).orElse(""),
            AddressType.CONTROLLER.name(),
            infrastructureType,
            false
        ));
        poolEntity.setGovernance(findOrCreateContract(
            functionsUtils.callAddressByName(GOVERNANCE, address, lastBlock).orElse(""),
            AddressType.GOVERNANCE.name(),
            infrastructureType,
            false
        ));
        poolEntity.setOwner(findOrCreateContract(
            functionsUtils.callAddressByName(OWNER, address, lastBlock).orElse(""),
            AddressType.OWNER.name(),
            infrastructureType,
            false
        ));
        poolEntity.setLpToken(findOrCreateContract(
            functionsUtils.callAddressByName(LP_TOKEN, address, lastBlock).orElse(""),
            AddressType.UNKNOWN_VAULT.name(),
            infrastructureType,
            false
        ));
        poolEntity.setRewardToken(findOrCreateContract(
            functionsUtils.callAddressByName(REWARD_TOKEN, address, lastBlock).orElse(""),
            AddressType.UNKNOWN_REWARD_TOKEN.name(),
            infrastructureType,
            false
        ));
        poolEntity.setUpdatedBlock(lastBlock);
    }

    private void enrichUniPair(UniPairEntity uniPairEntity) {
        String address = uniPairEntity.getAddress().getAddress();
        uniPairEntity.setDecimals(
            functionsUtils.callIntByName(FunctionsNames.DECIMALS, address, lastBlock)
                .orElse(BigInteger.ZERO).longValue());
        uniPairEntity.setToken0(findOrCreateContract(
            functionsUtils.callAddressByName(TOKEN0, address, lastBlock).orElse(""),
            AddressType.UNKNOWN_TOKEN.name(),
            tokenType,
            false
        ));
        uniPairEntity.setToken1(findOrCreateContract(
            functionsUtils.callAddressByName(TOKEN1, address, lastBlock).orElse(""),
            AddressType.UNKNOWN_TOKEN.name(),
            tokenType,
            false
        ));
        uniPairEntity.setUpdatedBlock(lastBlock);
    }

    private ContractEntity findOrCreateContract(String address,
                                                String name,
                                                ContractTypeEntity type,
                                                boolean rewrite) {
        if (address == null || address.isBlank()) {
            return null;
        }
        ContractEntity entity = contractRepository.findFirstByAddress(address);
        if (entity == null) {
            entity = new ContractEntity();
            entity.setAddress(address);
            entity.setName(name);
            entity.setType(type);
            log.info("Created new contract {}", name);
            contractRepository.save(entity);
        } else if (rewrite) {
            entity.setName(name);
            entity.setType(type);
            log.info("Updated contract {}", name);
            contractRepository.save(entity);
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

    public static Optional<PoolEntity> getPoolByAddress(String address) {
        return Optional.of(poolsCacheByAddress.get(address));
    }

    public static Optional<VaultEntity> getVaultByAddress(String address) {
        return Optional.of(vaultsCacheByAddress.get(address));
    }

    public static Optional<UniPairEntity> getUniPairByAddress(String address) {
        return Optional.of(uniPairsCacheByAddress.get(address));
    }

    public static Optional<TokenEntity> getTokenByAddress(String address) {
        return Optional.of(tokensCacheByAddress.get(address));
    }

    public static Optional<PoolEntity> getPoolByName(String address) {
        return Optional.of(poolsCacheByName.get(address));
    }

    public static Optional<VaultEntity> getVaultByName(String address) {
        return Optional.of(vaultsCacheByName.get(address));
    }

    public static Optional<UniPairEntity> getUniPairByName(String address) {
        return Optional.of(uniPairsCacheByName.get(address));
    }

    public static Optional<TokenEntity> getTokenByName(String address) {
        return Optional.of(tokensCacheByName.get(address));
    }

}
