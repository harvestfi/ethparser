package pro.belbix.ethparser.web3.deployer.parser;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.COINS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_CURRENT_TOKENS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_POOL_FROM_LP_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.LP_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MINTER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.SYMBOL;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN1;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.USER_REWARD_PER_TOKEN_PAID;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.VAULT_FRACTION_TO_INVEST_NUMERATOR;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BSC_FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CURVE_REGISTRY_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractType.POOL;
import static pro.belbix.ethparser.web3.contracts.ContractType.TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractType.UNI_PAIR;
import static pro.belbix.ethparser.web3.contracts.ContractType.UNKNOWN;
import static pro.belbix.ethparser.web3.contracts.ContractType.VAULT;
import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Int128;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.abi.FunctionService;
import pro.belbix.ethparser.web3.abi.FunctionsNames;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUpdater;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.PlatformType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.contracts.models.LpContract;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;
import pro.belbix.ethparser.web3.contracts.models.TokenContract;
import pro.belbix.ethparser.web3.deployer.ContractInfo;
import pro.belbix.ethparser.web3.prices.LPSeeker;

@Service
@Log4j2
public class DeployerEventToContractTransformer {

  private final FunctionsUtils functionsUtils;
  private final ContractLoader contractLoader;
  private final ContractUpdater contractUpdater;
  private final LPSeeker lpSeeker;
  private final ContractDbService contractDbService;
  private final FunctionService functionService;

  public DeployerEventToContractTransformer(
      FunctionsUtils functionsUtils,
      ContractLoader contractLoader,
      ContractUpdater contractUpdater,
      LPSeeker lpSeeker, ContractDbService contractDbService,
      FunctionService functionService) {
    this.functionsUtils = functionsUtils;
    this.contractLoader = contractLoader;
    this.contractUpdater = contractUpdater;
    this.lpSeeker = lpSeeker;
    this.contractDbService = contractDbService;
    this.functionService = functionService;
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

    ContractInfo contractInfo = collectContractInfo(address, block, network, type);

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

  private ContractInfo collectContractInfo(
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
    PlatformType platformType = detectPlatformType(underlyingName);

    contractInfo.setUnderlyingAddress(underlyingAddress);
    contractInfo.setUnderlyingName(underlyingName);
    contractInfo.setPlatformType(platformType);

    String tokenNames = tokenNames(contractInfo);
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
    if (ContractUtils.isFarmAddress(address)) {
      return "PS";
    }
    return functionsUtils.callStrByName(
        FunctionsNames.SYMBOL, address, block, network)
        .orElse("")
        .replaceAll("/", "_")
        .replaceAll("\\+", "_")
        .replaceAll("Crv", "")
        .toUpperCase();
  }

  private ContractType detectContractType(DeployerDTO dto) {
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

  private PlatformType detectPlatformType(String name) {
    if (name.startsWith("Curve")) {
      return PlatformType.CURVE;
    } else if (name.startsWith("Uniswap")) {
      return PlatformType.UNISWAP;
    } else if (name.startsWith("SushiSwap")) {
      return PlatformType.SUSHISWAP;
    } else if (name.startsWith("1inch")) {
      return PlatformType.ONEINCH;
    } else if (name.startsWith("Balancer")) {
      return PlatformType.BALANCER;
    } else if (name.startsWith("Ellipsis")) {
      return PlatformType.ELLIPSIS;
    } else if (name.startsWith("Pancake")) {
      return PlatformType.PANCAKESWAP;
    }
    log.warn("Unknown platform for name {}", name);
    return PlatformType.UNKNOWN;
  }

  private String tokenNames(ContractInfo contractInfo) {
    String tokenNames = uniTokenNames(contractInfo);
    if (tokenNames.isBlank()) {
      try {
        tokenNames = bptTokenNames(contractInfo);
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return tokenNames;
  }

  private String bptTokenNames(ContractInfo contractInfo)
      throws IOException, ClassNotFoundException {
    String address = contractInfo.getUnderlyingAddress();
    long block = contractInfo.getBlock();
    String network = contractInfo.getNetwork();

    //noinspection unchecked
    String tokens = functionsUtils.callViewFunction(new Function(
            GET_CURRENT_TOKENS,
            List.of(),
            Collections.singletonList(TypeReference.makeTypeReference("address[]"))
        ),
        address, block, network)
        .orElse("");
    if (tokens.isBlank()) {
      return "";
    }

    //noinspection unchecked
    List<String> tokenAddresses = ObjectMapperFactory.getObjectMapper().readValue(
        (String) ObjectMapperFactory.getObjectMapper().readValue(tokens, List.class).get(0)
        , List.class);
    contractInfo.getUnderlyingTokens().addAll(tokenAddresses);
    return tokenAddresses.stream()
        .map(adr -> functionsUtils.callStrByName(
            SYMBOL, adr, block, network)
            .orElse("?"))
        .collect(Collectors.joining("_"));
  }

  private String uniTokenNames(ContractInfo contractInfo) {
    String address = contractInfo.getUnderlyingAddress();
    long block = contractInfo.getBlock();
    String network = contractInfo.getNetwork();

    String token0Adr = functionsUtils.callAddressByName(
        TOKEN0, address, block, network)
        .orElse(null);
    if (token0Adr == null) {
      return "";
    }
    String token1Adr = functionsUtils.callAddressByName(
        TOKEN1, address, block, network)
        .orElse(null);
    if (token1Adr == null) {
      return "";
    }

    contractInfo.getUnderlyingTokens().add(token0Adr);
    contractInfo.getUnderlyingTokens().add(token1Adr);

    String token0Name;
    if (ZERO_ADDRESS.equalsIgnoreCase(token0Adr)) {
      token0Name = ContractUtils.getBaseNetworkWrappedTokenName(network);
    } else {
      token0Name = functionsUtils.callStrByName(
          FunctionsNames.SYMBOL, token0Adr, block, network)
          .orElse("?");
    }

    String token1Name;
    if (ZERO_ADDRESS.equalsIgnoreCase(token1Adr)) {
      token1Name = ContractUtils.getBaseNetworkWrappedTokenName(network);
    } else {
      token1Name = functionsUtils.callStrByName(
          FunctionsNames.SYMBOL, token1Adr, block, network)
          .orElse("?");
    }
    return token0Name + "_" + token1Name;
  }

  private void collectUnderlingContracts(
      ContractInfo contractInfo,
      List<PureEthContractInfo> contracts
  ) {
    long block = contractInfo.getBlock();
    String network = contractInfo.getNetwork();

    if (contractInfo.getUnderlyingTokens().isEmpty()) {
      createTokenAndLpContracts(contractInfo.getUnderlyingAddress(), block, network, contracts);
    } else {
      // if we have multiple underlying just add underlying token
      createTokenContract(contractInfo.getUnderlyingAddress(), block, network, contracts);
      contractInfo.getUnderlyingTokens().forEach(c ->
          createTokenAndLpContracts(c, block, network, contracts));
    }
  }

  private void createTokenAndLpContracts(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    if (functionService.isLp(address, block, network)) {
      createSimpleLpContract(address, block, network, contracts);
    } else {
      TokenContract tokenContract =
          createTokenContract(address, block, network, contracts);
      if (tokenContract != null) {
        createLpContracts(address, block, network, contracts, tokenContract);
      }
    }
  }

  private TokenContract createTokenContract(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    if (ZERO_ADDRESS.equalsIgnoreCase(address)
        || contracts.stream().anyMatch(c -> c.getAddress().equalsIgnoreCase(address))
        || contractDbService.getContractByAddress(address, network).isPresent()) {
      return null;
    }

    String curveUnderlying = curveUnderlyingContracts(address, block, network, contracts);

    String symbol = functionsUtils.callStrByName(
        FunctionsNames.SYMBOL, address, block, network)
        .orElse("?");

    TokenContract tokenContract = new TokenContract((int) block, symbol, address);
    tokenContract.setNetwork(network);
    tokenContract.setContractType(TOKEN);
    tokenContract.setCurveUnderlying(curveUnderlying);
    addInContracts(contracts, tokenContract);
    return tokenContract;
  }

  private void createSimpleLpContract(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    if (ZERO_ADDRESS.equalsIgnoreCase(address)
        || contracts.stream().anyMatch(c -> c.getAddress().equalsIgnoreCase(address))
        || contractDbService.getContractByAddress(address, network).isPresent()) {
      return;
    }

    String symbol = lpName(address, block, network);

    LpContract lpContract = new LpContract((int) block, symbol, null, address);
    lpContract.setNetwork(network);
    lpContract.setContractType(UNI_PAIR);
    addInContracts(contracts, lpContract);
  }

  private void createLpContracts(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts,
      TokenContract tokenContract
  ) {
    if (tokenContract.getContractType() == UNI_PAIR // lp for lp is forbidden
        || tokenContract.getCurveUnderlying() != null // curve token doesn't have UNI LP
        || ContractUtils.isStableCoin(address) // lp for stablecoin is forbidden
    ) {
      return;
    }

    String lpAddress = lpSeeker.findLargestLP(address, block, network, contracts);
    if (lpAddress == null
        || contracts.stream()
        .anyMatch(c -> c.getAddress().equalsIgnoreCase(lpAddress))) {
      return;
    }

    tokenContract.addLp((int) block, lpAddress);
    ContractInfo lpContractInfo = new ContractInfo(lpAddress, block, network, UNI_PAIR);
    lpContractInfo.setUnderlyingAddress(lpAddress);
    String lpFullName = lpName(lpAddress, block, network);

    LpContract lpContract = new LpContract((int) block, lpFullName, address, lpAddress);
    lpContract.setContractType(UNI_PAIR);
    lpContract.setNetwork(network);
    addInContracts(contracts, lpContract);

    tokenNames(lpContractInfo); // filling underlying tokens (can be optimized)
    lpContractInfo.getUnderlyingTokens().forEach(t -> {
      if (t.equalsIgnoreCase(address)) {
        return;
      }
      createTokenAndLpContracts(t, block, network, contracts);
    });

  }

  private String curveUnderlyingContracts(
      String address,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    try {
      String minterAddress = functionsUtils.callAddressByName(MINTER, address, block, network)
          .orElse(null);
      String underlyingToken;
      if (minterAddress == null) {
        String name = functionsUtils.callStrByName(NAME, address, block, network).orElse("");
        if (!name.startsWith("Curve.fi")) {
          return null;
        }
        // use future block because it maybe not created yet
        minterAddress = functionsUtils.callAddressByNameWithArg(
            GET_POOL_FROM_LP_TOKEN, address, CURVE_REGISTRY_ADDRESS, 12000000L, network)
            .orElse(null);
        if (minterAddress == null) {
          // TODO I don't know how to find pool address, use USDC as underlying :(
          String USDC = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48";
          createTokenAndLpContracts(USDC, block, network, contracts);
          return USDC;
        }
        //noinspection unchecked
        String coinRaw = functionsUtils.callViewFunction(new Function(
                COINS,
                List.of(new Int128(0)),
                List.of(TypeReference.makeTypeReference("address"))
            ),
            minterAddress, block, network).orElse(null);
        if (coinRaw == null) {
          return null;
        }

        underlyingToken = (String) ObjectMapperFactory.getObjectMapper()
            .readValue(coinRaw, List.class)
            .get(0);
      } else {
        //noinspection unchecked
        String coinRaw = functionsUtils.callViewFunction(new Function(
                COINS,
                List.of(new Uint256(0)),
                List.of(TypeReference.makeTypeReference("address"))
            ),
            minterAddress, block, network).orElse(null);
        if (coinRaw == null) {
          return null;
        }

        underlyingToken = (String) ObjectMapperFactory.getObjectMapper()
            .readValue(coinRaw, List.class)
            .get(0);
      }

      //try to determinate underlying
      String deeperUnderlyingAddress = getPotentiallyUnderlying(underlyingToken, block, network);
      if (!Strings.isBlank(deeperUnderlyingAddress)) {
        underlyingToken = deeperUnderlyingAddress;
      }
      createTokenAndLpContracts(underlyingToken, block, network, contracts);
      return underlyingToken;
    } catch (Exception e) {
      return null;
    }
  }

  private String getPotentiallyUnderlying(String address, long block, String network) {
    return functionsUtils.callAddressByName(FunctionsNames.TOKEN,
        address, block, network).orElse("");
  }

  private String lpName(String lpAddress, long block, String network) {
    String lpName = functionsUtils.callStrByName(
        NAME, lpAddress, block, network)
        .orElse("");

    PlatformType lpPlatformType = detectPlatformType(lpName);
    ContractInfo lpContractInfo =
        new ContractInfo(lpAddress, block, network, UNI_PAIR);
    lpContractInfo.setUnderlyingAddress(lpAddress);
    String tokenNames = uniTokenNames(lpContractInfo);
    return lpPlatformType.getPrettyName() + "_LP_" + tokenNames;
  }

  private boolean addInContracts(List<PureEthContractInfo> contracts, PureEthContractInfo c) {
    if (contracts.stream()
        .anyMatch(exC -> exC.getAddress().equalsIgnoreCase(c.getAddress()))
    ) {
      return false;
    }
    contracts.add(c);
    return true;
  }


}
