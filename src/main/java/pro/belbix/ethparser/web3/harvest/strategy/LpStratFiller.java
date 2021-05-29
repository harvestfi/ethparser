package pro.belbix.ethparser.web3.harvest.strategy;

import static java.math.RoundingMode.HALF_UP;
import static org.web3j.abi.TypeReference.makeTypeReference;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.CLAIMED_REWARDS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.END_BLOCK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.POOL_ID;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.POOL_INFO;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARDS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_PER_BLOCK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_POOL;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_ALLOC_POINT;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNLOCKS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNLOCKS_TOTAL_QUOTATION;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNLOCK_COUNT;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.USER_POOL_INFO;
import static pro.belbix.ethparser.web3.abi.FunctionsUtils.typeReferencesList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.model.StratRewardInfo;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
@Log4j2
public class LpStratFiller implements FarmableProjectFiller {

  private final static String CLAIMED_EVENT =
      "0xd8138f8a3f377c5259ca548e70e4c2de94f129f5a11036a15b69513cba2b426a";
  private final ObjectMapper om = ObjectMapperFactory.getObjectMapper();
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;
  private final EthBlockService ethBlockService;

  public LpStratFiller(FunctionsUtils functionsUtils,
      Web3Functions web3Functions, EthBlockService ethBlockService) {
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
    this.ethBlockService = ethBlockService;
  }

  @Override
  public void fillPoolAddress(StratInfo stratInfo) {
    stratInfo.setPoolAddress(functionsUtils.callAddressByName(
        REWARD_POOL,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow());
  }

  @Override
  public void fillRewardTokenAddress(StratInfo stratInfo) {
    stratInfo.getRewardTokens().add(new StratRewardInfo(functionsUtils.callAddressByName(
        REWARD_TOKEN,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow()));
  }

  @Override
  public void fillPoolInfo(StratInfo stratInfo) {
    long poolId = functionsUtils.callIntByName(
        POOL_ID,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow().longValue();
    stratInfo.setPoolExtraInfo1(poolId + "");

    List poolInfo = functionsUtils.callViewFunction(
        new Function(
            USER_POOL_INFO,
            List.of(new Uint256(poolId), new Address(stratInfo.getStrategyAddress())),
            List.of(
                silentCall(() -> makeTypeReference("uint256")).orElseThrow(),
                silentCall(() -> makeTypeReference("uint256")).orElseThrow()
            )
        ),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).flatMap(raw -> silentCall(() -> om.readValue(raw, List.class)))
        .orElseThrow();

    StratRewardInfo rewardInfo = stratInfo.getRewardTokens().get(0);
    stratInfo.setPoolBalance(functionsUtils.parseAmount(
        new BigInteger((String) poolInfo.get(0)),
        rewardInfo.getAddress(),
        stratInfo.getNetwork()
    ));

    stratInfo.setPoolExtraInfo2((String) poolInfo.get(1));
    stratInfo.setPoolExtraInfo3((String) poolInfo.get(0));

    fillPoolTotalSupply(stratInfo);
  }

  @Override
  public void fillRewards(StratInfo stratInfo) {
    String masterChiefAddress = stratInfo.getPoolAddress();
    String network = stratInfo.getNetwork();
    long block = stratInfo.getBlock();
    StratRewardInfo rewardInfo = stratInfo.getRewardTokens().get(0);

    BigDecimal accRewardPerShare = calcAccRewardPerShare(stratInfo);

    BigDecimal userReward = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        REWARDS,
        stratInfo.getStrategyAddress(),
        masterChiefAddress,
        block,
        network
    ).orElseThrow());

    BigDecimal userPoolAmount = new BigDecimal(stratInfo.getPoolExtraInfo3());

    BigDecimal calculatedReward = userPoolAmount.multiply(accRewardPerShare)
        .divide(BigDecimal.valueOf(1e12), 999, HALF_UP);

    BigDecimal userAccruedReward = new BigDecimal(stratInfo.getPoolExtraInfo2());

    BigDecimal userRewardUpdated = userReward.add(calculatedReward.subtract(userAccruedReward));

    // *********** calc unlocked ****************

    int unlockCount = functionsUtils.callIntByName(
        UNLOCK_COUNT,
        masterChiefAddress,
        block,
        network
    ).orElseThrow().intValue();

    BigDecimal unlocksTotalQuotation = new BigDecimal(functionsUtils.callIntByName(
        UNLOCKS_TOTAL_QUOTATION,
        masterChiefAddress,
        block,
        network
    ).orElseThrow());

    BigDecimal claimable = BigDecimal.ZERO;
    for (int i = 0; i < unlockCount; i++) {
      //noinspection rawtypes
      List unlockInfo = functionsUtils.callViewFunction(
          new Function(
              UNLOCKS,
              List.of(new Uint256(i)),
              typeReferencesList(
                  "uint256", //block
                  "uint256")  //quota
          ),
          masterChiefAddress,
          block,
          network
      ).flatMap(raw -> silentCall(() -> om.readValue(raw, List.class)))
          .orElseThrow();

      if (block < Long.parseLong((String) unlockInfo.get(0))) {
        continue;
      }

      claimable = claimable.add(
          userRewardUpdated.multiply(new BigDecimal((String) unlockInfo.get(1)))
              .divide(unlocksTotalQuotation, 999, HALF_UP)
      );
    }
    // ***********************

    BigDecimal claimedRewards = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        CLAIMED_REWARDS,
        stratInfo.getStrategyAddress(),
        masterChiefAddress,
        block,
        network
    ).orElseThrow());

    BigDecimal unlocked = claimable.subtract(claimedRewards);
    // 544408636626269347746
    rewardInfo.setAmount(functionsUtils.parseAmount(
        unlocked.toBigInteger(),
        rewardInfo.getAddress(),
        network
    ));
  }

  private BigDecimal calcAccRewardPerShare(StratInfo stratInfo) {
    String masterChiefAddress = stratInfo.getPoolAddress();
    String network = stratInfo.getNetwork();
    long block = stratInfo.getBlock();
    StratRewardInfo rewardInfo = stratInfo.getRewardTokens().get(0);

    //noinspection rawtypes
    List pool = functionsUtils.callViewFunction(
        new Function(
            POOL_INFO,
            List.of(new Uint256(Long.parseLong(stratInfo.getPoolExtraInfo1()))),
            typeReferencesList(
                "address", //token
                "uint256", //accRewardPerShare
                "uint256", //allocPoint
                "uint256")  //lastRewardBlock
        ),
        masterChiefAddress,
        block,
        network
    ).flatMap(raw -> silentCall(() -> om.readValue(raw, List.class)))
        .orElseThrow();

    String poolToken = (String) pool.get(0);
    BigDecimal accRewardPerShare = new BigDecimal((String) pool.get(1));
    long currentBlock = block;
    long lastRewardBlock = Long.parseLong((String) pool.get(3));

    long endBlock = functionsUtils.callIntByName(
        END_BLOCK,
        masterChiefAddress,
        block,
        network
    ).orElseThrow().longValue();

    // it is logic for avoid double calculation in the same block, but the contract can have a calculated state
//    if (endBlock > block) {
//      currentBlock = lastRewardBlock;
//    }
//
//    if (currentBlock <= lastRewardBlock) {
//      return accRewardPerShare;
//    }

    BigDecimal lpSupply = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        BALANCE_OF,
        masterChiefAddress,
        poolToken,
        block,
        network
    ).orElseThrow());

    if (lpSupply.doubleValue() == 0) {
      return accRewardPerShare;
    }

    BigDecimal rewardPerBlock = new BigDecimal(functionsUtils.callIntByName(
        REWARD_PER_BLOCK,
        masterChiefAddress,
        block,
        network
    ).orElseThrow());

    BigDecimal totalAllocPoint = new BigDecimal(functionsUtils.callIntByName(
        TOTAL_ALLOC_POINT,
        masterChiefAddress,
        block,
        network
    ).orElseThrow());

    BigDecimal blockLasted = BigDecimal.valueOf(currentBlock - lastRewardBlock);

    BigDecimal reward = blockLasted.multiply(rewardPerBlock)
        .multiply(new BigDecimal((String) pool.get(2)))
        .divide(totalAllocPoint, 999, HALF_UP);

    return accRewardPerShare.add(
        reward.multiply(BigDecimal.valueOf(1e12))
            .divide(lpSupply, 999, HALF_UP)
    );
  }

  @Override
  public int lastClaimBlock(StratInfo stratInfo) {
    return web3Functions.findLastLogEvent(
        stratInfo.getPoolAddress(),
        Objects.requireNonNullElse(
            stratInfo.getStrategyCreated(),
            stratInfo.getBlock() - 100000)
            .intValue(),
        (int) stratInfo.getBlock(),
        stratInfo.getNetwork(),
        lastClaimPredicate(stratInfo),
        CLAIMED_EVENT
    ).map(Log::getBlockNumber)
        .map(BigInteger::intValue)
        .orElse(0);
  }

  private void fillPoolTotalSupply(StratInfo stratInfo) {
    stratInfo.setPoolTotalSupply(functionsUtils.parseAmount(
        functionsUtils.callIntByNameWithAddressArg(
            BALANCE_OF,
            stratInfo.getPoolAddress(),
            stratInfo.getStrategyUnderlyingAddress(),
            stratInfo.getBlock(),
            stratInfo.getNetwork()
        ).orElseThrow(),
        stratInfo.getStrategyUnderlyingAddress(), stratInfo.getNetwork()));
  }

  private Predicate<? super List<Type>> lastClaimPredicate(StratInfo stratInfo) {
    return l -> !l.isEmpty() &&
        ((Address) l.get(0)).getValue().equalsIgnoreCase(stratInfo.getStrategyAddress());
  }
}
