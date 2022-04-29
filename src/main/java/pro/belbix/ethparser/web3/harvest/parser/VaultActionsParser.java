package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE_PER_FULL_SHARE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractType.POOL;
import static pro.belbix.ethparser.web3.contracts.ContractType.VAULT;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.model.tx.HarvestTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionService;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstantsV7;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.contracts.db.ErrorDbService;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.harvest.decoder.VaultActionsLogDecoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class VaultActionsParser extends Web3Parser<HarvestDTO, Log> {

  public static final double BURNED_FARM = 14850.0;
  private static final Set<String> allowedMethods = new HashSet<>(
      Collections.singletonList("transfer"));
  private final VaultActionsLogDecoder vaultActionsLogDecoder = new VaultActionsLogDecoder();
  private final Web3Functions web3Functions;

  private final VaultActionsDBService vaultActionsDBService;
  private final EthBlockService ethBlockService;
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final NetworkProperties networkProperties;
  private final Web3Subscriber web3Subscriber;
  private final FunctionService functionService;


  private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  private final ContractDbService contractDbService;

  public VaultActionsParser(Web3Functions web3Functions,
      VaultActionsDBService vaultActionsDBService,
      EthBlockService ethBlockService,
      PriceProvider priceProvider,
      FunctionsUtils functionsUtils,
      ParserInfo parserInfo,
      AppProperties appProperties,
      NetworkProperties networkProperties,
      Web3Subscriber web3Subscriber,
      FunctionService functionService,
      HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator,
      ContractDbService contractDbService,
      ErrorDbService errorDbService) {
    super(parserInfo, appProperties, errorDbService);
    this.web3Functions = web3Functions;
    this.vaultActionsDBService = vaultActionsDBService;
    this.ethBlockService = ethBlockService;
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.networkProperties = networkProperties;
    this.web3Subscriber = web3Subscriber;
    this.functionService = functionService;
    this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
    this.contractDbService = contractDbService;
  }

  @Override
  protected void subscribeToInput() {
    web3Subscriber.subscribeOnLogs(input, this.getClass().getSimpleName());
  }

  @Override
  protected boolean save(HarvestDTO dto) {
    enrichDto(dto, dto.getNetwork());
    harvestOwnerBalanceCalculator.fillBalance(dto, dto.getNetwork());
    boolean success = vaultActionsDBService.saveHarvestDTO(dto);

    if (dto.getMigration() != null) {
      save(dto.getMigration());
    }
    if (success) {
      log.debug("Successfully saved vault action for {}", dto.getVault());
    }
    return success;
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network)
        .isParseHarvestLog();
  }

  @Override
  public HarvestDTO parse(Log ethLog, String network) {
    if (!isValidLog(ethLog, network)) {
      return null;
    }
    HarvestTx harvestTx = vaultActionsLogDecoder.decode(ethLog);
    if (harvestTx == null) {
      return null;
    }

    if (!isAllowedMethod(harvestTx)) {
      return null;
    }

    if (ContractUtils.isPsAddress(harvestTx.getVault().getValue())) {
      if (!parsePs(harvestTx, network)) {
        return null;
      }
    } else {
      if (!parseVaults(harvestTx, ethLog, network)) {
        return null;
      }
    }
    HarvestDTO dto = createDto(harvestTx, network);
    //enrich date
    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(ethLog.getBlockNumber().longValue(), network));

    if (ContractUtils.isPsAddress(harvestTx.getVault().getValue())) {
      fillPsTvlAndUsdValue(dto, harvestTx.getVault().getValue(), network);
    } else {
      //share price
      fillSharePrice(dto, network);

      //usd values
      fillUsdPrice(dto, network);
    }
    log.info(dto.print());
    if (harvestTx.isMigration()) {
      log.info("Parse migration for {}", dto.getVault());
      parseMigration(dto, network);
    }
    return dto;
  }

  private boolean isValidLog(Log ethLog, String network) {
    return ethLog != null
        && contractDbService
        .getContractByAddressAndType(ethLog.getAddress(), VAULT, network)
        .isPresent();
  }

  private void fillPsTvlAndUsdValue(HarvestDTO dto, String vaultHash, String network) {
    String poolAddress = ContractUtils.getPsPool(vaultHash);
    Double price = priceProvider.getPriceForCoin(
        ContractUtils.getFarmAddress(network), dto.getBlock(), network);
    double vaultBalance = functionsUtils.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, poolAddress, dto.getBlock(), network)
            .orElse(BigInteger.ZERO),
        vaultHash, network);

    dto.setLastUsdTvl(price * vaultBalance);
    dto.setLastTvl(vaultBalance);
    dto.setSharePrice(farmTotalAmount(dto.getBlock(), network)); //todo remove after full reparsing
    dto.setTotalAmount(dto.getSharePrice());
    dto.setUsdAmount(Math.round(dto.getAmount() * price));
  }

  private double farmTotalAmount(long block, String network) {
    return functionsUtils.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY,
            ContractUtils.getFarmAddress(network), block, network)
            .orElse(BigInteger.ZERO),
        ContractUtils.getFarmAddress(network), network)
        - BURNED_FARM;
  }

  private boolean parsePs(HarvestTx harvestTx, String network) {
    if ("Staked".equals(harvestTx.getMethodName())) {
      harvestTx.setMethodName("Deposit");
    } else if ("Staked#V2".equals(harvestTx.getMethodName())) {
      TransactionReceipt receipt = web3Functions
          .fetchTransactionReceipt(harvestTx.getHash(), network);
      String vault = receipt.getTo();
      if (vault.equalsIgnoreCase(ContractConstantsV7.iPS_ADDRESS)) {
        return false; //not count deposit from iPS
      }
      harvestTx.setMethodName("Deposit");
      harvestTx.setAmount(harvestTx.getIntFromArgs()[0]);
    } else if ("withdrawn".equals(harvestTx.getMethodName().toLowerCase())) {
      TransactionReceipt receipt = web3Functions
          .fetchTransactionReceipt(harvestTx.getHash(), network);
      String owner = receipt.getFrom();
      if (!owner.equals(harvestTx.getOwner())) {
        return false; //withdrawn for not owner is a garbage
      }
      harvestTx.setMethodName("Withdraw");
    } else {
      return false;
    }
    return true;
  }

  private boolean parseVaults(HarvestTx harvestTx, Log ethLog, String network) {
    TransactionReceipt receipt = web3Functions
        .fetchTransactionReceipt(harvestTx.getHash(), network);
    if (receipt == null) {
      throw new IllegalStateException("Receipt is null for " + harvestTx.getHash());
    }
    if (ZERO_ADDRESS.equals(harvestTx.getAddressFromArgs1().getValue())) {
      harvestTx.setMethodName("Deposit");
      harvestTx.setOwner(receipt.getFrom());
    } else if (ZERO_ADDRESS.equals(harvestTx.getAddressFromArgs2().getValue())) {
      harvestTx.setMethodName("Withdraw");
      harvestTx.setOwner(receipt.getFrom());
    } else {
      String poolAddress = contractDbService
          .getPoolContractByVaultAddress(
              ethLog.getAddress(), ethLog.getBlockNumber().longValue(), network)
          .map(ContractEntity::getAddress)
          .orElse(""); // if we don't have a pool assume that it was migration
      if (isMigration(harvestTx, poolAddress, network)) {
        log.info("migrate tx " + harvestTx.getHash());
        harvestTx.setOwner(receipt.getFrom());
        harvestTx.setMethodName("Withdraw");
        harvestTx.setMigration(true);
      } else {
        return false;
      }
    }
    return true;
  }

  public void parseMigration(HarvestDTO dto, String network) {
    int onBlock = dto.getBlock().intValue();
    String newVault = dto.getVault().replace("_V0", "");
    String newVaultHash = contractDbService
        .getAddressByName(newVault, ContractType.VAULT, network)
        .orElseThrow(() -> new IllegalStateException("Not found address by " + newVault));
    String poolAddress = contractDbService.getPoolContractByVaultAddress(
        newVaultHash, dto.getBlock(), network)
        .orElseThrow(() -> new IllegalStateException("Not found pool for " + newVaultHash))
        .getAddress();

    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(poolAddress), onBlock, onBlock, network);
    HarvestTx migrationTx = null;
    for (LogResult ethLog : logResults) {
      HarvestTx tx = vaultActionsLogDecoder.decode((Log) ethLog.get());
      if (tx != null && "Migrated".equals(tx.getMethodName())) {
        migrationTx = tx;
        break;
      }
    }
    if (migrationTx == null) {
      log.warn("Migration not found for " + dto);
      return;
    }
    migrationTx.setVault(new Address(newVaultHash));
    createHarvestFromMigration(dto, migrationTx, network);
  }

  private void createHarvestFromMigration(HarvestDTO dto, HarvestTx migrationTx, String network) {
    HarvestDTO migrationDto = createDto(migrationTx, network);
    migrationDto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(migrationTx.getBlock().longValue(), network));
    migrationDto.setMethodName("Deposit");
    migrationDto.setVault(dto.getVault().replace("_V0", ""));
    migrationDto.setVaultAddress(
        contractDbService.getAddressByName(migrationDto.getVault(), ContractType.VAULT, network)
            .orElseThrow()
    );

    migrationDto.setAmount(
        functionsUtils.parseAmount(
            migrationTx.getIntFromArgs()[1],
            contractDbService
                .getAddressByName(migrationDto.getVault(), ContractType.VAULT, network)
                .orElseThrow(() -> new IllegalStateException(
                    "Not found address by " + migrationDto.getVault())),
            network
        )
    );

    fillSharePrice(migrationDto, network);
    fillUsdPrice(migrationDto, network);
    migrationDto.setMigrated(true);
    log.info("Migrated harvest " + migrationDto);
    dto.setMigration(migrationDto);
  }

  private boolean isMigration(HarvestTx harvestTx, String currentPool, String network) {
    String v = harvestTx.getAddressFromArgs2().getValue();
    return contractDbService
        .getContractByAddressAndType(v, POOL, network)
        .isPresent()
        //it is transfer to stacking
        && !v.equalsIgnoreCase(currentPool) // and it is not current contract
        && !"0x153C544f72329c1ba521DDf5086cf2fA98C86676"
        .equalsIgnoreCase(harvestTx.getAddressFromArgs1().getValue()) // and it is not iFARM reward
        ;
  }

  private void fillSharePrice(HarvestDTO dto, String network) {
    BigInteger sharePriceInt =
        functionsUtils.callIntByName(
            GET_PRICE_PER_FULL_SHARE, dto.getVaultAddress(), dto.getBlock(), network)
            .orElse(BigInteger.ZERO);
    double sharePrice;
    if (BigInteger.ONE.equals(sharePriceInt)) {
      sharePrice = 0.0;
    } else {
      sharePrice = functionsUtils
          .parseAmount(sharePriceInt, dto.getVaultAddress(), network);
    }
    dto.setSharePrice(sharePrice);
  }

  public void enrichDto(HarvestDTO dto, String network) {
    //set gas
    dto.setLastGas(web3Functions.fetchAverageGasPrice(network));
  }

  private void fillUsdPrice(HarvestDTO dto, String network) {
    String underlyingToken = functionsUtils
        .callAddressByName(UNDERLYING, dto.getVaultAddress(), dto.getBlock(), network)
        .orElseThrow(
            () -> new IllegalStateException(
                "Can't fetch underlying token for " + dto.getVaultAddress()));
    if (functionService.isLp(underlyingToken, dto.getBlock(), network)) {
      fillUsdValuesForLP(dto, dto.getVaultAddress(), underlyingToken, network);
    } else {
      fillUsdValues(dto, dto.getVaultAddress(), network);
    }
  }

  private void fillUsdValues(HarvestDTO dto, String vaultHash, String network) {
    String underlyingAddress = functionsUtils.callAddressByName(
        UNDERLYING, vaultHash, dto.getBlock(), network)
        .orElseThrow(
            () -> new IllegalStateException(
                "Can't fetch underlying token for " + vaultHash));

    Double priceUnderlying = priceProvider
        .getPriceForCoin(underlyingAddress, dto.getBlock(), network);
    if (priceUnderlying == null) {
      throw new IllegalStateException("Unknown coin " + dto.getVault());
    }
    dto.setUnderlyingAddress(underlyingAddress);
    dto.setUnderlyingPrice(priceUnderlying);
    double vaultBalance = functionsUtils.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, vaultHash, dto.getBlock(), network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        vaultHash, network);
    double sharePrice = dto.getSharePrice();

    double vault = (vaultBalance * sharePrice);
    dto.setLastTvl(vault);
    dto.setLastUsdTvl((double) Math.round(vault * priceUnderlying));
    dto.setUsdAmount((long) (priceUnderlying * dto.getAmount() * dto.getSharePrice()));
    if (ContractConstantsV7.iPS_ADDRESS.equalsIgnoreCase(dto.getVaultAddress())) {
      dto.setTotalAmount(farmTotalAmount(dto.getBlock(), network));
    }
  }

  public void fillUsdValuesForLP(HarvestDTO dto, String vaultHash, String lpHash, String network) {
    long dtoBlock = dto.getBlock();
    double vaultBalance = functionsUtils.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, vaultHash, dtoBlock, network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        vaultHash, network);
    double sharePrice = dto.getSharePrice();
    double lpTotalSupply = functionsUtils.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, lpHash, dtoBlock, network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        lpHash, network);

    Tuple2<Double, Double> lpUnderlyingBalances = functionsUtils.callReserves(
        lpHash, dtoBlock, network);
    if (lpUnderlyingBalances == null) {
      log.error("lpUnderlyingBalances is null. maybe wrong lp contract for " + dto);
      return;
    }

    double lpUnderlyingBalance1 = lpUnderlyingBalances.component1();
    double lpUnderlyingBalance2 = lpUnderlyingBalances.component2();

    double vaultSharedBalance = (vaultBalance * sharePrice);
    dto.setLastTvl(vaultSharedBalance);
    double vaultFraction = vaultSharedBalance / lpTotalSupply;
    String underlyingLpAddress = functionsUtils.callAddressByName(
        UNDERLYING, vaultHash, dto.getBlock(), network)
        .orElseThrow(
            () -> new IllegalStateException(
                "Can't fetch underlying token for " + vaultHash));
    Tuple2<Double, Double> uniPrices = priceProvider
        .getPairPriceForLpHash(underlyingLpAddress, dtoBlock, network);

    //suppose it's ONE_INCH ETH pair
    if (lpUnderlyingBalance1 == 0) {
      double coin2AmountUsd = lpUnderlyingBalance2 * uniPrices.component2();
      lpUnderlyingBalance1 = (coin2AmountUsd / uniPrices.component1());
    } else if (lpUnderlyingBalance2 == 0) {
      double coin1AmountUsd = lpUnderlyingBalance1 * uniPrices.component1();
      lpUnderlyingBalance2 = (coin1AmountUsd / uniPrices.component2());
    }

    double firstVault = vaultFraction * lpUnderlyingBalance1;
    double secondVault = vaultFraction * lpUnderlyingBalance2;

    Tuple2<String, String> lpTokens = contractDbService
        .tokenAddressesByUniPairAddress(lpHash, network);

    dto.setLpStat(LpStat.createJson(
        contractDbService.getNameByAddress(lpTokens.component1(), network).orElse("unknown"),
        lpTokens.component1(),
        contractDbService.getNameByAddress(lpTokens.component2(), network).orElse("unknown"),
        lpTokens.component2(),
        firstVault,
        secondVault,
        uniPrices.component1(),
        uniPrices.component2()
    ));

    Long firstVaultUsdAmount = Math.round(firstVault * uniPrices.component1());
    Long secondVaultUsdAmount = Math.round(secondVault * uniPrices.component2());
    long vaultUsdAmount = firstVaultUsdAmount + secondVaultUsdAmount;
    dto.setLastUsdTvl((double) vaultUsdAmount);

    double txFraction = (dto.getAmount() / vaultBalance);
    long txUsdAmount = Math.round(vaultUsdAmount * txFraction);
    dto.setUsdAmount(txUsdAmount);
  }

  private boolean isAllowedMethod(HarvestTx harvestTx) {
    if (ContractUtils.isPsAddress(harvestTx.getVault().getValue())) {
      return "staked".equalsIgnoreCase(harvestTx.getMethodName())
          || "Staked#V2".equalsIgnoreCase(harvestTx.getMethodName())
          || "withdrawn".equalsIgnoreCase(harvestTx.getMethodName());
    }
    return allowedMethods.contains(harvestTx.getMethodName().toLowerCase());
  }

  public HarvestDTO createDto(HarvestTx tx, String network) {
    String vaultName = contractDbService
        .getNameByAddress(tx.getVault().getValue(), network)
        .orElseThrow();
    HarvestDTO dto = new HarvestDTO();
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setHash(tx.getHash());
    dto.setBlock(tx.getBlock().longValue());
    dto.setNetwork(network);
    dto.setVault(vaultName);
    dto.setVaultAddress(tx.getVault().getValue());
    dto.setConfirmed(1);
    dto.setMethodName(tx.getMethodName());
    dto.setAmount(functionsUtils.parseAmount(tx.getAmount(), tx.getVault().getValue(), network));
    if (tx.getAmountIn() != null) {
      dto.setAmountIn(functionsUtils
          .parseAmount(tx.getAmountIn(), tx.getFToken().getValue(), network));
    }
    dto.setOwner(tx.getOwner());
    return dto;
  }


}
