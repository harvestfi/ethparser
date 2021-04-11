package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Objects.requireNonNullElse;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BUYBACK_RATIO;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.LIQUIDATE_REWARD_TO_WETH_IN_SUSHI;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.PROFITSHARING_DENOMINATOR;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.PROFITSHARING_NUMERATOR;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING_BALANCE_IN_VAULT;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING_BALANCE_WITH_INVESTMENT;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNIVERSAL_LIQUIDATOR;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.VAULT_FRACTION_TO_INVEST_DENOMINATOR;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.VAULT_FRACTION_TO_INVEST_NUMERATOR;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLERS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.model.HardWorkTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.decoder.HardWorkLogDecoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class HardWorkParser implements Web3Parser {
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final HardWorkLogDecoder hardWorkLogDecoder = new HardWorkLogDecoder();
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final HardWorkDbService hardWorkDbService;
  private final ParserInfo parserInfo;
  private final AppProperties appProperties;
  private Instant lastTx = Instant.now();

  public HardWorkParser(PriceProvider priceProvider,
      FunctionsUtils functionsUtils,
      Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, HardWorkDbService hardWorkDbService,
      ParserInfo parserInfo,
      AppProperties appProperties) {
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.hardWorkDbService = hardWorkDbService;
    this.parserInfo = parserInfo;
    this.appProperties = appProperties;
  }

  @Override
  public void startParse() {
    log.info("Start parse Hard work logs");
    web3Subscriber.subscribeOnLogs(logs);
    parserInfo.addParser(this);
    new Thread(() -> {
      while (run.get()) {
        Log ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          HardWorkDTO dto = parseLog(ethLog, appProperties.getNetwork());
          if (dto != null) {
            lastTx = Instant.now();
            boolean saved = hardWorkDbService.save(dto);
            if (saved) {
              output.put(dto);
            }
          }
        } catch (Exception e) {
          log.error("Can't save " + ethLog, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  public HardWorkDTO parseLog(Log ethLog, String network) {
    if (ethLog == null
        || !CONTROLLERS.get(network).equalsIgnoreCase(ethLog.getAddress())) {
      return null;
    }

    HardWorkTx tx = hardWorkLogDecoder.decode(ethLog);
    if (tx == null) {
      return null;
    }

    // parse SharePriceChangeLog was wrong solution
    // some strategies doesn't change share price
    // but it is a good point for catch doHardWork
    if (!"SharePriceChangeLog".equals(tx.getMethodName())) {
      throw new IllegalStateException("Unknown method " + tx.getMethodName());
    }

    if (cu(network).getNameByAddress(tx.getVault()).isEmpty()) {
      log.warn("Unknown vault " + tx.getVault());
      return null;
    }
    String vaultName = cu(network).getNameByAddress(tx.getVault())
        .orElseThrow(() -> new IllegalStateException("Not found name by " + tx.getVault()));
    if (vaultName.endsWith("_V0")) {
      // skip old strategies
      return null;
    }
    HardWorkDTO dto = new HardWorkDTO();
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock());
    dto.setBlockDate(tx.getBlockDate());
    dto.setVault(vaultName);
    dto.setShareChange(cu(network).parseAmount(
        tx.getNewSharePrice().subtract(tx.getOldSharePrice()), tx.getVault()));

    parseRates(dto, tx.getStrategy(), network);
    parseRewards(dto, tx.getHash(), network);
    parseVaultInvestedFunds(dto, network);

    log.info(dto.print());
    return dto;
  }

  // not in the root because it can be weekly reward
  private void parseRewards(HardWorkDTO dto, String txHash, String network) {
    TransactionReceipt tr = web3Functions.fetchTransactionReceipt(txHash, network);
    double farmPrice =
        priceProvider.getPriceForCoin("FARM", dto.getBlock(), network);
    dto.setFarmPrice(farmPrice);
    boolean autoStake = isAutoStake(tr.getLogs());
    dto.setAutoStake(autoStake ? 1 : 0);
    for (Log ethLog : tr.getLogs()) {
      parseRewardAddedEvents(ethLog, dto, autoStake, network);
    }

    double ethPrice =
        priceProvider.getPriceForCoin("ETH", dto.getBlock(), network);
    dto.setEthPrice(ethPrice);
    double farmBuybackEth = dto.getFullRewardUsd() / ethPrice;
    dto.setFarmBuybackEth(farmBuybackEth);
    fillFeeInfo(dto, txHash, tr, network);
  }

  private void parseRates(HardWorkDTO dto, String strategyHash, String network) {
    double profitSharingDenominator =
        functionsUtils
            .callIntByName(
                PROFITSHARING_DENOMINATOR, strategyHash, dto.getBlock(), network)
            .orElseThrow(() -> new IllegalStateException(
                "Error get profitSharingDenominator from " + strategyHash))
            .doubleValue();
    double profitSharingRate = 0.0;
    if (profitSharingDenominator > 0) {
      double profitSharingNumerator =
          functionsUtils
              .callIntByName(PROFITSHARING_NUMERATOR, strategyHash, dto.getBlock(), network)
              .orElseThrow(() -> new IllegalStateException(
                  "Error get profitSharingNumerator from " + strategyHash))
              .doubleValue();
      profitSharingRate = profitSharingNumerator / profitSharingDenominator;
    }
    dto.setProfitSharingRate(profitSharingRate);
    dto.setBuyBackRate(fetchBuybackRatio(strategyHash, dto.getBlock(), network));
  }

  private double fetchBuybackRatio(String strategyAddress, long block, String network) {
    double buyBackRatio =
        functionsUtils.callIntByName(
            BUYBACK_RATIO, strategyAddress, block, network)
            .orElse(BigInteger.ZERO).doubleValue();
    if (buyBackRatio > 0) {
      return buyBackRatio / 10000;
    }

    if (functionsUtils.callAddressByName(
        UNIVERSAL_LIQUIDATOR, strategyAddress, block, network)
        .isPresent()) {
      return 1;
    }

    Boolean liquidateRewardToWethInSushi =
        functionsUtils.callBoolByName(
            LIQUIDATE_REWARD_TO_WETH_IN_SUSHI, strategyAddress, block, network)
            .orElse(false);
    return liquidateRewardToWethInSushi ? 1 : 0;
  }

  private boolean isAutoStake(List<Log> logs) {
    return logs.stream()
        .filter(l -> {
          try {
            return hardWorkLogDecoder.decode(l).getMethodName().equals("RewardAdded");
          } catch (Exception ignored) {
          }
          return false;
        }).count() > 1;
  }

  private void parseRewardAddedEvents(
      Log ethLog, HardWorkDTO dto, boolean autoStake, String network) {
    HardWorkTx tx;
    try {
      tx = hardWorkLogDecoder.decode(ethLog);
    } catch (Exception e) {
      return;
    }
    if (tx == null) {
      return;
    }
    if ("RewardAdded".equals(tx.getMethodName()) && isAllowedLog(ethLog, network)) {
      if (!autoStake && dto.getFarmBuyback() != 0.0) {
        throw new IllegalStateException("Duplicate RewardAdded for " + dto);
      }
      double reward = tx.getReward().doubleValue() / D18;

      // AutoStake strategies have two RewardAdded events - first for PS and second for stake contract
      if (autoStake && dto.getFarmBuyback() != 0) {
        // in this case it is second reward for strategy
        double fullReward = (reward * dto.getFarmPrice())
            / (1 - requireNonNullElse(dto.getProfitSharingRate(), 0.3)); // full reward
        dto.setFullRewardUsd(fullReward);
      } else {
        double farmBuybackMultiplier =
            (1 - requireNonNullElse(dto.getProfitSharingRate(), 0.3))
                / requireNonNullElse(dto.getProfitSharingRate(), 0.3)
                * dto.getBuyBackRate();

        // PS pool reward
        dto.setFarmBuyback(reward + (reward * farmBuybackMultiplier));

        // for non AutoStake strategy we will not have accurate data for strategy reward
        // just calculate aprox value based on PS reward
        if (!autoStake) {
          double fullReward = ((reward * dto.getFarmPrice())
              / requireNonNullElse(dto.getProfitSharingRate(), 0.3)); // full reward
          dto.setFullRewardUsd(fullReward);
        }
      }

    }
  }

  private boolean isAllowedLog(Log ethLog, String network) {
    return "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".equalsIgnoreCase(ethLog.getAddress())
        || cu(network).isPoolAddress(ethLog.getAddress().toLowerCase());
  }

  private void fillFeeInfo(
      HardWorkDTO dto, String txHash, TransactionReceipt tr, String network) {
    Transaction transaction = web3Functions.findTransaction(txHash, network);
    double gas = (tr.getGasUsed().doubleValue());
    double gasPrice = transaction.getGasPrice().doubleValue() / D18;
    double ethPrice =
        priceProvider.getPriceForCoin("ETH", dto.getBlock(), network);
    double feeUsd = gas * gasPrice * ethPrice;
    dto.setFee(feeUsd);
    double feeEth = gas * gasPrice;
    dto.setFeeEth(feeEth);
    dto.setGasUsed(gas);
  }

  private void parseVaultInvestedFunds(HardWorkDTO dto, String network) {
    String vaultHash = cu(network).getAddressByName(dto.getVault(), ContractType.VAULT)
        .orElseThrow();
    double underlyingBalanceInVault = functionsUtils.callIntByName(
        UNDERLYING_BALANCE_IN_VAULT,
        vaultHash,
        dto.getBlock(), network).orElse(BigInteger.ZERO).doubleValue();
    double underlyingBalanceWithInvestment = functionsUtils.callIntByName(
        UNDERLYING_BALANCE_WITH_INVESTMENT,
        vaultHash,
        dto.getBlock(), network).orElse(BigInteger.ZERO).doubleValue();
    double vaultFractionToInvestNumerator = functionsUtils.callIntByName(
        VAULT_FRACTION_TO_INVEST_NUMERATOR,
        vaultHash,
        dto.getBlock(), network).orElse(BigInteger.ZERO).doubleValue();
    double vaultFractionToInvestDenominator = functionsUtils.callIntByName(
        VAULT_FRACTION_TO_INVEST_DENOMINATOR,
        vaultHash,
        dto.getBlock(), network).orElse(BigInteger.ZERO).doubleValue();

    double invested =
        100.0 * (underlyingBalanceWithInvestment - underlyingBalanceInVault)
            / underlyingBalanceWithInvestment;
    dto.setInvested(invested);
    double target = 100.0 * (vaultFractionToInvestNumerator / vaultFractionToInvestDenominator);
    dto.setInvestmentTarget(target);
  }

  private static ContractUtils cu(String network) {
    return ContractUtils.getInstance(network);
  }

  @Override
  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  @Override
  public Instant getLastTx() {
    return lastTx;
  }
}
