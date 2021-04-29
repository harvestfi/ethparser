package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE_PER_FULL_SHARE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractType.UNI_PAIR;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.model.tx.HarvestTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.harvest.decoder.VaultActionsLogDecoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class VaultActionsParser implements Web3Parser {
  public static final double BURNED_FARM = 14850.0;
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private static final Set<String> allowedMethods = new HashSet<>(
      Collections.singletonList("transfer"));
  private final VaultActionsLogDecoder vaultActionsLogDecoder = new VaultActionsLogDecoder();
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final BlockingQueue<Web3Model<Log>> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final VaultActionsDBService vaultActionsDBService;
  private final EthBlockService ethBlockService;
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final ParserInfo parserInfo;
  private final AppProperties appProperties;
  private final NetworkProperties networkProperties;
  private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  private final ContractDbService contractDbService;
  private Instant lastTx = Instant.now();
  private long count = 0;

  public VaultActionsParser(Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, VaultActionsDBService vaultActionsDBService,
      EthBlockService ethBlockService,
      PriceProvider priceProvider,
      FunctionsUtils functionsUtils,
      ParserInfo parserInfo,
      AppProperties appProperties,
      NetworkProperties networkProperties,
      HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator,
      ContractDbService contractDbService) {
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.vaultActionsDBService = vaultActionsDBService;
    this.ethBlockService = ethBlockService;
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.parserInfo = parserInfo;
    this.appProperties = appProperties;
    this.networkProperties = networkProperties;
    this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
    this.contractDbService = contractDbService;
  }

  @Override
  public void startParse() {
    log.info("Start parse Harvest vaults logs");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnLogs(logs);
    new Thread(() -> {
      while (run.get()) {
        Web3Model<Log> ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          if (ethLog == null
              || !networkProperties.get(ethLog.getNetwork())
              .isParseHarvestLog()) {
            continue;
          }
          HarvestDTO dto = parseVaultLog(ethLog.getValue(), ethLog.getNetwork());
          handleDto(dto, ethLog.getNetwork());
        } catch (Exception e) {
          log.error("Can't save " + ethLog, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  private void handleDto(HarvestDTO dto, String network) throws InterruptedException {
    if (dto != null) {
      lastTx = Instant.now();
      enrichDto(dto, network);
      harvestOwnerBalanceCalculator.fillBalance(dto, network);
      boolean success = vaultActionsDBService.saveHarvestDTO(dto);

      if (success) {
        output.put(dto);
      }
      if (dto.getMigration() != null) {
        handleDto(dto.getMigration(), network);
      }
    }
  }

  public HarvestDTO parseVaultLog(Log ethLog, String network) {
    if (!isValidLog(ethLog, network)) {
      return null;
    }
    count++;
    if (count % 100 == 0) {
      log.info(this.getClass().getSimpleName() + " handled " + count);
    }
    HarvestTx harvestTx = vaultActionsLogDecoder.decode(ethLog);
    if (harvestTx == null) {
      return null;
    }

    if (!isAllowedMethod(harvestTx, network)) {
      return null;
    }

    if (ContractUtils.getInstance(network).isPsAddress(harvestTx.getVault().getValue())) {
      if (!parsePs(harvestTx, network)) {
        return null;
      }
    } else {
      if (!parseVaults(harvestTx, ethLog, network)) {
        return null;
      }
    }
    String vaultName = contractDbService
        .getNameByAddress(harvestTx.getVault().getValue(), network)
        .orElseThrow();
    HarvestDTO dto = createDto(harvestTx, network);
    //enrich date
    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(ethLog.getBlockNumber().longValue(), network));

    if (ContractUtils.getInstance(network).isPsAddress(harvestTx.getVault().getValue())) {
      fillPsTvlAndUsdValue(dto, harvestTx.getVault().getValue(), network);
    } else {
      //share price
      fillSharePrice(dto, network);

      //usd values
      fillUsdPrice(dto, network);
    }
    log.info(dto.print());
    if (harvestTx.isMigration()) {
      parseMigration(dto, network);
    }
    return dto;
  }

  private boolean isValidLog(Log ethLog, String network) {
    return ethLog != null
        && ContractUtils.getInstance(network).isVaultAddress(ethLog.getAddress());
  }

  private void fillPsTvlAndUsdValue(HarvestDTO dto, String vaultHash, String network) {
    String poolAddress = ContractUtils.getPsPool(vaultHash);
    Double price = priceProvider.getPriceForCoin(FARM_TOKEN, dto.getBlock(), network);
    double vaultBalance = contractDbService.parseAmount(
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
    return contractDbService.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, ContractConstants.FARM_TOKEN, block, network)
            .orElse(BigInteger.ZERO),
        ContractConstants.FARM_TOKEN, network)
        - BURNED_FARM;
  }

  private boolean parsePs(HarvestTx harvestTx, String network) {
    if ("Staked".equals(harvestTx.getMethodName())) {
      harvestTx.setMethodName("Deposit");
    } else if ("Staked#V2".equals(harvestTx.getMethodName())) {
      TransactionReceipt receipt = web3Functions
          .fetchTransactionReceipt(harvestTx.getHash(), network);
      String vault = receipt.getTo();
      if (vault.equalsIgnoreCase("0x1571eD0bed4D987fe2b498DdBaE7DFA19519F651")) {
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
          .getPoolContractByVaultAddress(ethLog.getAddress(), network)
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
    String poolAddress = contractDbService.getPoolContractByVaultAddress(newVaultHash, network)
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
            contractDbService.parseAmount(
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
    return ContractUtils.getInstance(network).isPoolAddress(v)
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
      sharePrice = contractDbService
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
    if (contractDbService
        .getContractByAddressAndType(underlyingToken, UNI_PAIR, network)
        .isPresent()) {
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

    Double priceUnderlying = priceProvider.getPriceForCoin(underlyingAddress, dto.getBlock(), network);
    if (priceUnderlying == null) {
      throw new IllegalStateException("Unknown coin " + dto.getVault());
    }
    dto.setUnderlyingPrice(priceUnderlying);
    double vaultBalance = contractDbService.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, vaultHash, dto.getBlock(), network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        vaultHash, network);
    double sharePrice = dto.getSharePrice();

    double vault = (vaultBalance * sharePrice);
    dto.setLastTvl(vault);
    dto.setLastUsdTvl((double) Math.round(vault * priceUnderlying));
    dto.setUsdAmount((long) (priceUnderlying * dto.getAmount() * dto.getSharePrice()));
    if ("iPS".equals(dto.getVault())) {
      dto.setTotalAmount(farmTotalAmount(dto.getBlock(), network));
    }
  }

  public void fillUsdValuesForLP(HarvestDTO dto, String vaultHash, String lpHash, String network) {
    long dtoBlock = dto.getBlock();
    double vaultBalance = contractDbService.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, vaultHash, dtoBlock, network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        vaultHash, network);
    double sharePrice = dto.getSharePrice();
    double lpTotalSupply = contractDbService.parseAmount(
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

    Tuple2<String, String> lpTokens = ContractUtils.getInstance(network)
        .tokenAddressesByUniPairAddress(lpHash);

    dto.setLpStat(LpStat.createJson(
        contractDbService.getNameByAddress(lpTokens.component1(), network).orElse("unknown"),
        contractDbService.getNameByAddress(lpTokens.component2(), network).orElse("unknown"),
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

  private boolean isAllowedMethod(HarvestTx harvestTx, String network) {
    if (ContractUtils.getInstance(network).isPsAddress(harvestTx.getVault().getValue())) {
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
    dto.setAmount(contractDbService.parseAmount(tx.getAmount(), tx.getVault().getValue(), network));
    if (tx.getAmountIn() != null) {
      dto.setAmountIn(contractDbService
          .parseAmount(tx.getAmountIn(), tx.getFToken().getValue(), network));
    }
    dto.setOwner(tx.getOwner());
    return dto;
  }

  @Override
  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  @PreDestroy
  public void stop() {
    run.set(false);
  }

  @Override
  public Instant getLastTx() {
    return lastTx;
  }

}
