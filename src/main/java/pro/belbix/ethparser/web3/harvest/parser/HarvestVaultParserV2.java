package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE_PER_FULL_SHARE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

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
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class HarvestVaultParserV2 implements Web3Parser {
  public static final double BURNED_FARM = 14850.0;
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private static final Set<String> allowedMethods = new HashSet<>(
      Collections.singletonList("transfer"));
  private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final HarvestDBService harvestDBService;
  private final EthBlockService ethBlockService;
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final ParserInfo parserInfo;
  private final AppProperties appProperties;
  private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  private Instant lastTx = Instant.now();
  private long count = 0;

  public HarvestVaultParserV2(Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, HarvestDBService harvestDBService,
      EthBlockService ethBlockService,
      PriceProvider priceProvider,
      FunctionsUtils functionsUtils,
      ParserInfo parserInfo,
      AppProperties appProperties,
      HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator) {
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.harvestDBService = harvestDBService;
    this.ethBlockService = ethBlockService;
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.parserInfo = parserInfo;
    this.appProperties = appProperties;
    this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
  }

  @Override
  public void startParse() {
    log.info("Start parse Harvest vaults logs");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnLogs(logs);
    new Thread(() -> {
      while (run.get()) {
        Log ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          HarvestDTO dto = parseVaultLog(ethLog, appProperties.getNetwork());
          handleDto(dto, appProperties.getNetwork());
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
      boolean success = harvestDBService.saveHarvestDTO(dto);

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
    HarvestTx harvestTx = harvestVaultLogDecoder.decode(ethLog);
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

    HarvestDTO dto = harvestTx.toDto(network);
    //enrich date
    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(ethLog.getBlockNumber().longValue(), network));

    if (ContractUtils.getInstance(network).isPsAddress(harvestTx.getVault().getValue())) {
      fillPsTvlAndUsdValue(dto, harvestTx.getVault().getValue(), network);
    } else {
      //share price
      fillSharedPrice(dto, network);

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
    String poolAddress = ContractUtils.getInstance(network).poolByVaultAddress(vaultHash)
        .orElseThrow(() -> new IllegalStateException("Not found pool for " + vaultHash))
        .getContract().getAddress();
    Double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock(), network);
    double vaultBalance = ContractUtils.getInstance(network).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, poolAddress, dto.getBlock(), network)
            .orElse(BigInteger.ZERO),
        vaultHash);

    dto.setLastUsdTvl(price * vaultBalance);
    dto.setLastTvl(vaultBalance);
    dto.setSharePrice(farmTotalAmount(dto.getBlock(), network)); //todo remove after full reparsing
    dto.setTotalAmount(dto.getSharePrice());
    dto.setUsdAmount(Math.round(dto.getAmount() * price));
  }

  private double farmTotalAmount(long block, String network) {
    return ContractUtils.getInstance(network).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, ContractConstants.FARM_TOKEN, block, network)
            .orElse(BigInteger.ZERO),
        ContractConstants.FARM_TOKEN)
        - BURNED_FARM;
  }

  private boolean parsePs(HarvestTx harvestTx, String network) {
    if ("Staked".equals(harvestTx.getMethodName())) {
      harvestTx.setMethodName("Deposit");
    } else if ("Staked#V2".equals(harvestTx.getMethodName())) {
      TransactionReceipt receipt = web3Functions
          .fetchTransactionReceipt(harvestTx.getHash(), network);
      String vault = receipt.getTo();
      if (vault.equalsIgnoreCase(
          ContractUtils.getInstance(network).getAddressByName("iPS", ContractType.VAULT).get())) {
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
      String poolAddress = ContractUtils.getInstance(network)
          .poolByVaultAddress(ethLog.getAddress())
          .map(PoolEntity::getContract)
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
    String newVaultHash = ContractUtils.getInstance(network)
        .getAddressByName(newVault, ContractType.VAULT)
        .orElseThrow(() -> new IllegalStateException("Not found address by " + newVault));
    String poolAddress = ContractUtils.getInstance(network).poolByVaultAddress(newVaultHash)
        .orElseThrow(() -> new IllegalStateException("Not found pool for " + newVaultHash))
        .getContract().getAddress();

    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(poolAddress), onBlock, onBlock, network);
    HarvestTx migrationTx = null;
    for (LogResult ethLog : logResults) {
      HarvestTx tx = harvestVaultLogDecoder.decode((Log) ethLog.get());
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
    HarvestDTO migrationDto = migrationTx.toDto(network);
    migrationDto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(migrationTx.getBlock().longValue(), network));
    migrationDto.setMethodName("Deposit");
    migrationDto.setVault(dto.getVault().replace("_V0", ""));

    migrationDto
        .setAmount(
            ContractUtils.getInstance(network).parseAmount(migrationTx.getIntFromArgs()[1],
                ContractUtils.getInstance(network)
                    .getAddressByName(migrationDto.getVault(), ContractType.VAULT)
                    .orElseThrow(() -> new IllegalStateException(
                        "Not found address by " + migrationDto.getVault()))
            )
        );

    fillSharedPrice(migrationDto, network);
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
        && !v.equalsIgnoreCase( // and it is not iPS
        ContractUtils.getInstance(network).getAddressByName("iPS", ContractType.VAULT)
            .orElseThrow())
        ;
  }

  private void fillSharedPrice(HarvestDTO dto, String network) {
    String vaultHash = ContractUtils.getInstance(network)
        .getAddressByName(dto.getVault(), ContractType.VAULT)
        .orElseThrow(() -> new IllegalStateException("Not found address for " + dto.getVault()));
    BigInteger sharedPriceInt =
        functionsUtils.callIntByName(GET_PRICE_PER_FULL_SHARE, vaultHash, dto.getBlock(), network)
            .orElse(BigInteger.ZERO);
    double sharedPrice;
    if (BigInteger.ONE.equals(sharedPriceInt)) {
      sharedPrice = 0.0;
    } else {
      sharedPrice = ContractUtils.getInstance(network).parseAmount(sharedPriceInt, vaultHash);
    }
    dto.setSharePrice(sharedPrice);
  }

  public void enrichDto(HarvestDTO dto, String network) {
    //set gas
    dto.setLastGas(web3Functions.fetchAverageGasPrice(network));
  }

  private void fillUsdPrice(HarvestDTO dto, String network) {
    String vaultHash = ContractUtils.getInstance(network)
        .getAddressByName(dto.getVault(), ContractType.VAULT)
        .orElseThrow(() -> new IllegalStateException("Not found address by " + dto.getVault()));
    String underlyingToken = functionsUtils
        .callAddressByName(UNDERLYING, vaultHash, dto.getBlock(), network)
        .orElseThrow(
            () -> new IllegalStateException("Can't fetch underlying token for " + vaultHash));
    if (ContractUtils.getInstance(network).isLp(underlyingToken)) {
      fillUsdValuesForLP(dto, vaultHash, underlyingToken, network);
    } else {
      fillUsdValues(dto, vaultHash, network);
    }
  }

  private void fillUsdValues(HarvestDTO dto, String vaultHash, String network) {
    String underlyingAddress;
    if (ETH_NETWORK.equals(network)) {
      underlyingAddress = dto.getVault();
    } else {
      underlyingAddress = ContractUtils.getInstance(network).getVaultUnderlying(vaultHash)
          .orElse(dto.getVault());
    }
    Double price = priceProvider.getPriceForCoin(underlyingAddress, dto.getBlock(), network);
    if (price == null) {
      throw new IllegalStateException("Unknown coin " + dto.getVault());
    }
    dto.setUnderlyingPrice(price);
    double vaultBalance = ContractUtils.getInstance(network).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, vaultHash, dto.getBlock(), network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        vaultHash);
    double sharedPrice = dto.getSharePrice();

    double vault = (vaultBalance * sharedPrice);
    dto.setLastTvl(vault);
    dto.setLastUsdTvl((double) Math.round(vault * price));
    dto.setUsdAmount((long) (price * dto.getAmount() * dto.getSharePrice()));
    if ("iPS".equals(dto.getVault())) {
      dto.setTotalAmount(farmTotalAmount(dto.getBlock(), network));
    }
  }

  public void fillUsdValuesForLP(HarvestDTO dto, String vaultHash, String lpHash, String network) {
    long dtoBlock = dto.getBlock();
    double vaultBalance = ContractUtils.getInstance(network).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, vaultHash, dtoBlock, network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        vaultHash);
    double sharedPrice = dto.getSharePrice();
    double lpTotalSupply = ContractUtils.getInstance(network).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, lpHash, dtoBlock, network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + vaultHash)),
        lpHash);

    Tuple2<Double, Double> lpUnderlyingBalances = functionsUtils.callReserves(
        lpHash, dtoBlock, network);
    if (lpUnderlyingBalances == null) {
      log.error("lpUnderlyingBalances is null. maybe wrong lp contract for " + dto);
      return;
    }

    double lpUnderlyingBalance1 = lpUnderlyingBalances.component1();
    double lpUnderlyingBalance2 = lpUnderlyingBalances.component2();

    double vaultSharedBalance = (vaultBalance * sharedPrice);
    dto.setLastTvl(vaultSharedBalance);
    double vaultFraction = vaultSharedBalance / lpTotalSupply;
    String underlyingLpAddress =
        ContractUtils.getInstance(network).getVaultUnderlying(vaultHash)
            .orElseThrow(() -> new IllegalStateException(
                "Can't fetch underlying token for " + vaultHash));
    Tuple2<Double, Double> uniPrices = priceProvider
        .getPairPriceForLpHash(underlyingLpAddress, dtoBlock, network);

    double coin1Usd = uniPrices.component1();
    double coin2Usd = uniPrices.component2();
    //suppose it's ONE_INCH ETH pair
    if (lpUnderlyingBalance1 == 0) {
      coin2Usd = lpUnderlyingBalance2 * uniPrices.component2();
      lpUnderlyingBalance1 = (coin2Usd / uniPrices.component1());
    } else if (lpUnderlyingBalance2 == 0) {
      coin1Usd = lpUnderlyingBalance1 * uniPrices.component1();
      lpUnderlyingBalance2 = (coin1Usd / uniPrices.component2());
    }

    double firstVault = vaultFraction * lpUnderlyingBalance1;
    double secondVault = vaultFraction * lpUnderlyingBalance2;

    dto.setLpStat(LpStat.createJson(
        lpHash,
        firstVault,
        secondVault,
        uniPrices.component1(),
        uniPrices.component2(),
        network
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

  @Override
  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  public BlockingQueue<Log> getLogs() {
    return logs;
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
