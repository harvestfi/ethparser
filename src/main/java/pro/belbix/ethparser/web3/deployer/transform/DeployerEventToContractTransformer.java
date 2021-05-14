package pro.belbix.ethparser.web3.deployer.transform;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.LP_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.SYMBOL;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.USER_REWARD_PER_TOKEN_PAID;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.VAULT_FRACTION_TO_INVEST_NUMERATOR;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BSC_FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractType.POOL;
import static pro.belbix.ethparser.web3.contracts.ContractType.UNKNOWN;
import static pro.belbix.ethparser.web3.contracts.ContractType.VAULT;
import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.abi.FunctionsNames;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.contracts.models.LpContract;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;
import pro.belbix.ethparser.web3.contracts.models.TokenContract;
import pro.belbix.ethparser.web3.deployer.ContractInfo;

@Service
@Log4j2
public class DeployerEventToContractTransformer {

  private final FunctionsUtils functionsUtils;
  private final ContractLoader contractLoader;
  private final ContractDbService contractDbService;
  private final ContractNameCreator contractNameCreator;
  private final TokenTransformer tokenTransformer;

  public DeployerEventToContractTransformer(
      FunctionsUtils functionsUtils,
      ContractLoader contractLoader,
      ContractDbService contractDbService,
      ContractNameCreator contractNameCreator,
      TokenTransformer tokenTransformer) {
    this.functionsUtils = functionsUtils;
    this.contractLoader = contractLoader;
    this.contractDbService = contractDbService;
    this.contractNameCreator = contractNameCreator;
    this.tokenTransformer = tokenTransformer;
  }

  public void handleAndSave(DeployerDTO dto) {
    var contracts = transform(dto);
    for (PureEthContractInfo contract : contracts) {
      log.info("Save {}", contract);
      if (ContractType.VAULT == contract.getContractType()) {
        contractLoader.loadVault((SimpleContract) contract,
            contract.getNetwork(), contract.getCreatedOnBlock());
      } else if (POOL == contract.getContractType()) {
        contractLoader.loadPool((SimpleContract) contract,
            contract.getNetwork(), contract.getCreatedOnBlock());
      } else if (ContractType.TOKEN == contract.getContractType()) {
        contractLoader.loadToken((TokenContract) contract,
            contract.getNetwork(), contract.getCreatedOnBlock());
      } else if (ContractType.UNI_PAIR == contract.getContractType()) {
        contractLoader.loadUniPair((LpContract) contract,
            contract.getNetwork(), contract.getCreatedOnBlock());
      } else {
        log.error("Unknown contract type! {}", contract);
      }
    }

    // need to link token to lp after loading
    for (PureEthContractInfo contract : contracts) {
      if (ContractType.TOKEN == contract.getContractType()) {
        contractLoader.linkUniPairsToToken((TokenContract) contract, contract.getNetwork());
      }
    }
  }


  public List<PureEthContractInfo> transform(DeployerDTO dto) {
    if (!isEligible(dto)) {
      return List.of();
    }

    if (ZERO_ADDRESS.equalsIgnoreCase(dto.getToAddress())) {
      throw new IllegalStateException("Zero <to> address " + dto);
    }

    ContractType type = detectContractType(dto);
    if (!isEligibleVaultOrPool(dto, type)) {
      return List.of();
    }

    String address = dto.getToAddress();
    long block = dto.getBlock();
    String network = dto.getNetwork();
    log.info("Start transform {}", address);

    ContractInfo contractInfo =
        collectVaultOrPoolContractInfo(address, block, network, type);

    if (contractInfo == null) {
      return List.of();
    }

    SimpleContract contract = new SimpleContract(
        (int) dto.getBlock(),
        contractInfo.getName(),
        dto.getToAddress());
    contract.setContractType(type);
    contract.setNetwork(dto.getNetwork());

    List<PureEthContractInfo> result = new ArrayList<>();
    result.add(contract);
    collectUnderlingContracts(contractInfo, result);
    collectRewardToken(contractInfo, result);
    return result;
  }

  private boolean isEligible(DeployerDTO dto) {
    if (dto == null) {
      return false;
    }
    if (isVaultInit(dto.getMethodName())) {
      return true;
    }

    // any interactions with existed contracts
    if (dto.getToAddress() != null
        && contractDbService.getContractByAddress(dto.getToAddress(), dto.getNetwork())
        .isPresent()) {
      return true;
    }

    return CONTRACT_CREATION.name().equals(dto.getType());
  }

  private boolean isEligibleVaultOrPool(DeployerDTO dto, ContractType type) {
    if (VAULT != type && POOL != type) {
      log.info("Not vault or pool, skip contract transform");
      return false;
    }

//    if (VAULT == type && CONTRACT_CREATION.name().equals(dto.getType())) {
//      log.info("Vault contract creation, parse only vault init");
//      return false;
//    }

    return true;
  }

  private boolean isVaultInit(String methodName) {
    return "0x8fc1708c".equalsIgnoreCase(methodName)
        || "initializeVault".equals(methodName);
  }

  private ContractInfo collectVaultOrPoolContractInfo(
      String address,
      long block,
      String network,
      ContractType type
  ) {
    ContractInfo contractInfo = new ContractInfo(address, block, network, type);
    if (POOL == type) {
      address = functionsUtils.callAddressByName(
          LP_TOKEN, address, block, network)
          .orElseThrow(
              () -> new IllegalStateException("Can't fetch vault for pool " + contractInfo)
          ); // use only vault address for name creation
    }

    String underlyingAddress = functionsUtils.callAddressByName(
        UNDERLYING, address, block, network)
        .orElse(null);
    if (underlyingAddress == null) {
      // some pools (PS/LP) have not vault as underlying
      underlyingAddress = address;
    }

    if (isMigration(address, underlyingAddress, type)) {
      log.info("It is a migration contract, skip");
      return null;
    }

    String underlyingName = functionsUtils.callStrByName(
        NAME, underlyingAddress, block, network)
        .orElse("");
    PlatformType platformType = PlatformType.valueOfName(underlyingName);

    contractInfo.setUnderlyingAddress(underlyingAddress);
    contractInfo.setUnderlyingName(underlyingName);
    contractInfo.setPlatformType(platformType);

    String tokenNames = contractNameCreator.tokenNames(contractInfo);
    String prefix;
    String name;
    // single token or something new
    if (platformType.isUnknown()) {
      if (tokenNames.isBlank()) {
        String underlyingSymbol = underlyingSymbol(underlyingAddress, block, network);
        if (underlyingSymbol.isBlank()) {
          // better than nothing
          if (underlyingName.isBlank()) {
            name = functionsUtils.callAddressByName(
                SYMBOL, address, block, network)
                .orElse("UNKNOWN_NAME");
          } else {
            name = underlyingName.replaceAll(" ", "");
          }
        } else {
          // SINGLE TOKEN
          name = underlyingSymbol;
        }
      } else {
        // UNKNOWN LP UNDERLYING
        name = underlyingName.replaceAll(" ", "") + "_" + tokenNames;
      }
    } else {
      prefix = platformType.getPrettyName();
      if (tokenNames.isBlank()) {
        String underlyingSymbol = underlyingSymbol(underlyingAddress, block, network);
        name = prefix + "_" + underlyingSymbol;
      } else {
        // LP UNDERLYING
        name = prefix + "_" + tokenNames;
      }
    }

    if (name.endsWith("_")) {
      name = name.substring(0, name.length() - 1);
    }
    if (type == POOL) {
      name = "ST_" + name;
    }
    contractInfo.setName(name);
    return contractInfo;
  }

  private boolean isMigration(String address, String underlyingAddress, ContractType type) {
    return VAULT == type
        && (FARM_TOKEN.equalsIgnoreCase(underlyingAddress)
        || BSC_FARM_TOKEN.equalsIgnoreCase(underlyingAddress)
        || ZERO_ADDRESS.equalsIgnoreCase(underlyingAddress))
        && !ContractUtils.isPsAddress(address);
  }

  private String underlyingSymbol(String address, long block, String network) {
    // assume that FARM underlying only in Profit Share pool
    if (ContractUtils.isFarmAddress(address)
        || ContractUtils.isPsAddress(address)) {
      return "PS";
    }
    return functionsUtils.callStrByName(
        FunctionsNames.SYMBOL, address, block, network)
        .orElse("")
        .replaceAll("/", "_")
        .replaceAll("\\+", "_")
        .replaceAll("Crv", "")
        .replaceAll("Belt.fi", "")
        .trim()
        ;
  }

  private ContractType detectContractType(DeployerDTO dto) {
    if (dto.getToAddress() == null) {
      return UNKNOWN;
    }
    ContractType type = UniqueTypes.TYPES.get(dto.getToAddress().toLowerCase());
    if (type != null) {
      return type;
    }
    try {
      if (functionsUtils.callIntByName(
          VAULT_FRACTION_TO_INVEST_NUMERATOR,
          dto.getToAddress(),
          dto.getBlock(), dto.getNetwork()).isPresent()) {
        return VAULT;
      } else if (functionsUtils.callIntByNameWithAddressArg(
          USER_REWARD_PER_TOKEN_PAID,
          dto.getToAddress(), // any address
          dto.getToAddress(),
          dto.getBlock(), dto.getNetwork()).isPresent()) {
        return POOL;
      }
    } catch (Exception e) {
      log.info("Can't determinate contract type {}", dto);
    }
    return UNKNOWN;
  }

  private void collectUnderlingContracts(
      ContractInfo contractInfo,
      List<PureEthContractInfo> contracts
  ) {
    long block = contractInfo.getBlock();
    String network = contractInfo.getNetwork();

    tokenTransformer
        .createTokenAndLpContracts(contractInfo.getUnderlyingAddress(), block, network, contracts);
    contractInfo.getUnderlyingTokens().forEach(c ->
        tokenTransformer.createTokenAndLpContracts(c, block, network, contracts));
  }

  private void collectRewardToken(ContractInfo contractInfo, List<PureEthContractInfo> result) {
    if (contractInfo.getContractType() != POOL) {
      return;
    }

    String rewardTokenAdr = functionsUtils.callAddressByName(REWARD_TOKEN,
        contractInfo.getAddress(), contractInfo.getBlock(), contractInfo.getNetwork())
        .orElse(null);

    tokenTransformer.createTokenAndLpContracts(rewardTokenAdr,
        contractInfo.getBlock(), contractInfo.getNetwork(), result);
  }


}
