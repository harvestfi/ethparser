package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.PERIOD_FINISH;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_RATE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;

@Service
@Log4j2
public class RewardParser implements Web3Parser {
  private final ContractUtils contractUtils = ContractUtils.getInstance(ETH_NETWORK);
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final Set<String> notWaitNewBlock = Set.of("reward-download", "new-strategy-download");
  private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final RewardsDBService rewardsDBService;
  private final AppProperties appProperties;
  private final ParserInfo parserInfo;
  private Instant lastTx = Instant.now();
  private boolean waitNewBlock = true;

  public RewardParser(FunctionsUtils functionsUtils,
      Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, EthBlockService ethBlockService,
      RewardsDBService rewardsDBService, AppProperties appProperties,
      ParserInfo parserInfo) {
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.rewardsDBService = rewardsDBService;
    this.appProperties = appProperties;
    this.parserInfo = parserInfo;
  }

  @Override
  public void startParse() {
    log.info("Start parse Rewards logs");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnLogs(logs);
    new Thread(() -> {
      while (run.get()) {
        Log ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          RewardDTO dto = parseLog(ethLog);
          if (dto != null) {
            lastTx = Instant.now();
            boolean saved = rewardsDBService.saveRewardDTO(dto);
            if (saved) {
              output.put(dto);
            }
          }
        } catch (Exception e) {
          log.error("Error parse reward from " + ethLog, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  public RewardDTO parseLog(Log ethLog) throws InterruptedException {
    if (ethLog == null || !contractUtils.isPoolAddress(ethLog.getAddress())) {
      return null;
    }

    HarvestTx tx = harvestVaultLogDecoder.decode(ethLog);
    if (tx == null || !tx.getMethodName().startsWith("RewardAdded")) {
      return null;
    }

    long nextBlock = tx.getBlock().longValue();
    String poolAddress = tx.getVault().getValue();
    long periodFinish = functionsUtils.callIntByName(PERIOD_FINISH, poolAddress, nextBlock, ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Error get period from " + poolAddress))
        .longValue();
    BigInteger rewardRate = functionsUtils.callIntByName(REWARD_RATE, poolAddress, nextBlock, ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Error get rate from " + poolAddress));
    if (periodFinish == 0 || rewardRate.equals(BigInteger.ZERO)) {
      log.error("Wrong values for " + ethLog);
      return null;
    }
    long blockTime = ethBlockService.getTimestampSecForBlock(nextBlock, ETH_NETWORK);

    double farmRewardsForPeriod = 0.0;
    if (periodFinish > blockTime) {
      farmRewardsForPeriod = new BigDecimal(rewardRate)
          .divide(new BigDecimal(D18), 999, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf((double) (periodFinish - blockTime)))
          .doubleValue();
    }

    double farmBalance = ContractUtils.getInstance(ETH_NETWORK).parseAmount(
        functionsUtils
            .callIntByName(BALANCE_OF, poolAddress, ContractConstants.FARM_TOKEN, nextBlock, ETH_NETWORK)
            .orElseThrow(() -> new IllegalStateException(
                "Error get balance from " + ContractConstants.FARM_TOKEN)),
        ContractConstants.FARM_TOKEN);

    RewardDTO dto = new RewardDTO();
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock().longValue());
    dto.setBlockDate(blockTime);
    dto.setVault(contractUtils.getNameByAddress(poolAddress)
        .orElseThrow(() -> new IllegalStateException("Pool name not found for " + poolAddress))
        .replaceFirst("ST__", "")
        .replaceFirst("ST_", ""));
    dto.setReward(farmRewardsForPeriod);
    dto.setPeriodFinish(periodFinish);
    dto.setFarmBalance(farmBalance);
    log.info("Parsed " + dto);
    return dto;
  }

  public void setWaitNewBlock(boolean waitNewBlock) {
    this.waitNewBlock = waitNewBlock;
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
