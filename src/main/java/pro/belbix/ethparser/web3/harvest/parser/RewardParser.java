package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.LP_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.PERIOD_FINISH;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_RATE;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.NOTIFY_HELPER;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_V0_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ST_PS_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractType.POOL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.model.tx.HarvestTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.repositories.ErrorsRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.contracts.db.ErrorDbService;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;
import pro.belbix.ethparser.web3.harvest.decoder.VaultActionsLogDecoder;

@Service
@Log4j2
public class RewardParser extends Web3Parser<RewardDTO, Log> {

  private final Set<String> notWaitNewBlock = Set.of("reward-download", "new-strategy-download");
  private final VaultActionsLogDecoder vaultActionsLogDecoder = new VaultActionsLogDecoder();
  private final FunctionsUtils functionsUtils;
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final RewardsDBService rewardsDBService;
  private final Web3Functions web3Functions;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;
  private boolean waitNewBlock = true;

  public RewardParser(
      FunctionsUtils functionsUtils,
      Web3Subscriber web3Subscriber,
      EthBlockService ethBlockService,
      RewardsDBService rewardsDBService,
      AppProperties appProperties,
      ParserInfo parserInfo, Web3Functions web3Functions,
      NetworkProperties networkProperties,
      ContractDbService contractDbService,
      ErrorDbService errorDbService) {
    super(parserInfo, appProperties, errorDbService);
    this.functionsUtils = functionsUtils;
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.rewardsDBService = rewardsDBService;
    this.web3Functions = web3Functions;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
  }

  @Override
  protected void subscribeToInput() {
    web3Subscriber.subscribeOnLogs(input, this.getClass().getSimpleName());
  }

  @Override
  protected boolean save(RewardDTO dto) {
    return rewardsDBService.saveRewardDTO(dto);
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network).isParseRewardsLog();
  }

  public RewardDTO parse(Log ethLog, String network) {
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
      try {
        Thread.sleep(60 * 1000 * 5); //wait until new block created
      } catch (InterruptedException ignored) {
      }
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

    double rewardBalance = functionsUtils.parseAmount(
        functionsUtils.callIntByNameWithAddressArg(
            BALANCE_OF,
            poolAddress,
            rewardTokenAdr,
            block,
            network)
            .orElseThrow(() -> new IllegalStateException(
                "Error get balance from " + rewardTokenAdr)),
        rewardTokenAdr, network);

    String vaultAddress = getVaultAddressByPoolAddress(poolAddress, block, network);
    String vaultName = contractDbService.getNameByAddress(vaultAddress, network)
        .orElse("UNKNOWN_VAULT");

    RewardDTO dto = new RewardDTO();
    dto.setNetwork(network);
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock().longValue());
    dto.setBlockDate(blockTime);
    dto.setVault(vaultName);
    dto.setVaultAddress(vaultAddress);
    dto.setPoolAddress(poolAddress);
    dto.setReward(rewardsForPeriod);
    dto.setPeriodFinish(periodFinish);
    dto.setFarmBalance(rewardBalance);
    dto.setIsWeeklyReward(isWeeklyReward);
    log.info("Parsed " + dto);
    return dto;
  }

  private String getVaultAddressByPoolAddress(String poolAddress, long block, String network) {
    if (ST_PS_ADDRESS.equalsIgnoreCase(poolAddress)) {
      return PS_ADDRESS;
    } else if (PS_V0_ADDRESS.equalsIgnoreCase(poolAddress)) {
      return PS_V0_ADDRESS;
    }

    return functionsUtils.callAddressByName(LP_TOKEN, poolAddress, block, network)
        .orElseThrow(
            () -> new IllegalStateException("Can't fetch Vault for Pool " + poolAddress)
        );
  }

  public void setWaitNewBlock(boolean waitNewBlock) {
    this.waitNewBlock = waitNewBlock;
  }
}
