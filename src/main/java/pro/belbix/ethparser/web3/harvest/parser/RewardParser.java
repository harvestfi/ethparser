package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.PERIOD_FINISH;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_RATE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.NOTIFY_HELPER;
import static pro.belbix.ethparser.web3.contracts.ContractType.POOL;

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
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.RewardDTO;
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
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;
import pro.belbix.ethparser.web3.harvest.decoder.VaultActionsLogDecoder;

@Service
@Log4j2
public class RewardParser implements Web3Parser {
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final Set<String> notWaitNewBlock = Set.of("reward-download", "new-strategy-download");
  private final BlockingQueue<Web3Model<Log>> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final VaultActionsLogDecoder vaultActionsLogDecoder = new VaultActionsLogDecoder();
  private final FunctionsUtils functionsUtils;
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final RewardsDBService rewardsDBService;
  private final AppProperties appProperties;
  private final ParserInfo parserInfo;
  private final Web3Functions web3Functions;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;
  private Instant lastTx = Instant.now();
  private boolean waitNewBlock = true;

  public RewardParser(
      FunctionsUtils functionsUtils,
      Web3Subscriber web3Subscriber,
      EthBlockService ethBlockService,
      RewardsDBService rewardsDBService,
      AppProperties appProperties,
      ParserInfo parserInfo, Web3Functions web3Functions,
      NetworkProperties networkProperties,
      ContractDbService contractDbService) {
    this.functionsUtils = functionsUtils;
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.rewardsDBService = rewardsDBService;
    this.appProperties = appProperties;
    this.parserInfo = parserInfo;
    this.web3Functions = web3Functions;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
  }

  @Override
  public void startParse() {
    log.info("Start parse Rewards logs");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnLogs(logs);
    new Thread(() -> {
      while (run.get()) {
        Web3Model<Log> ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          if (ethLog == null
              || !networkProperties.get(ethLog.getNetwork())
              .isParseRewardsLog()) {
            continue;
          }
          RewardDTO dto = parseLog(ethLog.getValue(), ethLog.getNetwork());
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

  public RewardDTO parseLog(Log ethLog, String network) throws InterruptedException {
    if (ethLog == null
        || contractDbService
        .getContractByAddressAndType(ethLog.getAddress(), POOL, network)
        .isEmpty()) {
      return null;
    }

    HarvestTx tx = vaultActionsLogDecoder.decode(ethLog);
    if (tx == null || !tx.getMethodName().startsWith("RewardAdded")) {
      return null;
    }

    Transaction transaction = web3Functions.findTransaction(tx.getHash(), network);
    int isWeeklyReward = 1;
    if (transaction == null ||
        !NOTIFY_HELPER.get(network).equalsIgnoreCase(transaction.getTo())) {
      isWeeklyReward = 0;
    }

    // TODO remove when we will have our own node
    if (!notWaitNewBlock.contains(appProperties.getStartUtil())
        && waitNewBlock
        && tx.getBlock().longValue() > ethBlockService.getLastBlock(network)
        && isWeeklyReward == 1
    ) {
      log.info("Wait new block for correct parsing rewards");
      Thread.sleep(60 * 1000 * 5); //wait until new block created
    }

    long block = tx.getBlock().longValue();
    String poolAddress = tx.getVault().getValue();
    long periodFinish = functionsUtils
        .callIntByName(PERIOD_FINISH, poolAddress, block, network)
        .orElseThrow(() -> new IllegalStateException("Error get period from " + poolAddress))
        .longValue();
    BigInteger rewardRate = functionsUtils
        .callIntByName(REWARD_RATE, poolAddress, block, network)
        .orElseThrow(() -> new IllegalStateException("Error get rate from " + poolAddress));
    if (periodFinish == 0 || rewardRate.equals(BigInteger.ZERO)) {
      log.error("Wrong values for " + ethLog);
      return null;
    }
    long blockTime = ethBlockService.getTimestampSecForBlock(block, network);

    double rewardsForPeriod = 0.0;
    if (periodFinish > blockTime) {
      rewardsForPeriod = new BigDecimal(rewardRate)
          .divide(new BigDecimal(D18), 999, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf((double) (periodFinish - blockTime)))
          .doubleValue();
    }

    String rewardTokenAdr = contractDbService
        .getPoolByAddress(poolAddress, network)
        .map(p -> p.getRewardToken().getAddress())
        .orElseThrow(() -> new IllegalStateException("Reward token not found for " + poolAddress));

    double rewardBalance = contractDbService.parseAmount(
        functionsUtils.callIntByNameWithAddressArg(
            BALANCE_OF,
            poolAddress,
            rewardTokenAdr,
            block,
            network)
            .orElseThrow(() -> new IllegalStateException(
                "Error get balance from " + rewardTokenAdr)),
        rewardTokenAdr, network);

    RewardDTO dto = new RewardDTO();
    dto.setNetwork(network);
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock().longValue());
    dto.setBlockDate(blockTime);
    dto.setVault(contractDbService.getNameByAddress(poolAddress, network)
        .orElseThrow(() -> new IllegalStateException("Pool name not found for " + poolAddress))
        .replaceFirst("ST__", "")
        .replaceFirst("ST_", ""));
    dto.setReward(rewardsForPeriod);
    dto.setPeriodFinish(periodFinish);
    dto.setFarmBalance(rewardBalance);
    dto.setIsWeeklyReward(isWeeklyReward);
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
