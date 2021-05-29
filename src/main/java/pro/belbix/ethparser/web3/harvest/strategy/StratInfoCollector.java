package pro.belbix.ethparser.web3.harvest.strategy;

import static pro.belbix.ethparser.utils.CommonUtils.aprToApy;
import static pro.belbix.ethparser.utils.CommonUtils.calculateApr;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.INVESTED_UNDERLYING_BALANCE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.model.StratRewardInfo;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.deployer.transform.PlatformType;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class StratInfoCollector {

  private final FunctionsUtils functionsUtils;
  private final PriceProvider priceProvider;
  private final CurveFiller curveFiller;
  private final ContractDbService contractDbService;
  private final CompFiller compFiller;
  private final IdleFiller idleFiller;
  private final EthBlockService ethBlockService;

  public StratInfoCollector(
      FunctionsUtils functionsUtils, PriceProvider priceProvider,
      CurveFiller curveFiller,
      ContractDbService contractDbService,
      CompFiller compFiller, IdleFiller idleFiller,
      EthBlockService ethBlockService) {
    this.curveFiller = curveFiller;
    this.functionsUtils = functionsUtils;
    this.priceProvider = priceProvider;
    this.contractDbService = contractDbService;
    this.compFiller = compFiller;
    this.idleFiller = idleFiller;
    this.ethBlockService = ethBlockService;
  }

  public StratInfo collect(String strategyAddress, long block, String network) {
    StratInfo stratInfo = new StratInfo(strategyAddress, block, network);

    fillContractStats(stratInfo);

    fillStrategyUnderlyingInfo(stratInfo);

    getFiller(stratInfo).fillPoolAddress(stratInfo);

    fillRewardToken(stratInfo);

    fillFarmablePoolInfo(stratInfo);

    fillRewards(stratInfo);
    return stratInfo;
  }

  private FarmableProjectFiller getFiller(StratInfo stratInfo) {
    if (stratInfo.isPlatform(PlatformType.CURVE)) {
      return curveFiller;
    } else if (stratInfo.isPlatform(PlatformType.COMPOUND)) {
      return compFiller;
    } else if (stratInfo.isPlatform(PlatformType.IDLE)) {
      return idleFiller;
    }
    throw new IllegalStateException("Unknown platform for " + stratInfo);
  }

  private void fillContractStats(StratInfo stratInfo) {
    ContractEntity strategyContract = contractDbService
        .getContractByAddress(stratInfo.getStrategyAddress(), stratInfo.getNetwork())
        .orElse(null);
    if (strategyContract == null) {
      return;
    }
    stratInfo.setStrategyCreated(strategyContract.getCreated());
    stratInfo.setStrategyName(strategyContract.getName());
  }

  private void fillRewards(StratInfo stratInfo) {
    getFiller(stratInfo).fillRewards(stratInfo);

    int lastClaimBlock = getFiller(stratInfo).lastClaimBlock(stratInfo);

    long currentTs = ethBlockService
        .getTimestampSecForBlock(stratInfo.getBlock(), stratInfo.getNetwork());
    long lastClaimTs = ethBlockService
        .getTimestampSecForBlock(lastClaimBlock, stratInfo.getNetwork());
    stratInfo.setRewardPeriod(currentTs - lastClaimTs);

    double rewardAmountUsdSum = 0;
    for (StratRewardInfo rewardInfo : stratInfo.getRewardTokens()) {
      double rewardAmountUsd = rewardInfo.getAmount() * rewardInfo.getPrice();
      rewardAmountUsdSum += rewardAmountUsd;
      rewardInfo.setAmountUsd(rewardAmountUsd);
      log.info("Pool has reward {} {} (${} for price {})",
          rewardInfo.getAmount(), rewardInfo.getName(),
          rewardInfo.getAmountUsd(), rewardInfo.getPrice());
    }

    double apr = calculateApr(stratInfo.getRewardPeriod(),
        rewardAmountUsdSum,
        stratInfo.getStrategyBalanceUsd());
    double apy = aprToApy(apr, 365);

    stratInfo.setApr(apr);
    stratInfo.setApy(apy);

    log.info("Pool has  total rewards ${} for period {} hours, apr {}(apy {})",
        rewardAmountUsdSum,
        Duration.of(stratInfo.getRewardPeriod(), ChronoUnit.SECONDS).toHours(),
        apr, apy);
  }


  private void fillFarmablePoolInfo(StratInfo stratInfo) {
    getFiller(stratInfo).fillPoolInfo(stratInfo);

    stratInfo.setPercentOfPool(
        (stratInfo.getPoolBalance() / stratInfo.getPoolTotalSupply()) * 100);

    stratInfo.setPercentOfInvested(
        100 - (((stratInfo.getStrategyBalance() - stratInfo.getPoolBalance())
            / stratInfo.getStrategyBalance()) * 100));
  }

  private void fillStrategyUnderlyingInfo(StratInfo stratInfo) {
    String address = stratInfo.getStrategyAddress();
    long block = stratInfo.getBlock();
    String network = stratInfo.getNetwork();
    stratInfo.setStrategyUnderlyingAddress(
        functionsUtils.callAddressByName(UNDERLYING, address, block, network)
            .orElseThrow(
                () -> new IllegalStateException("Can't fetch underlying for " + address)
            ));
    stratInfo.setStrategyUnderlyingPrice(priceProvider.getPriceForCoin(
        stratInfo.getStrategyUnderlyingAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()));
    stratInfo.setStrategyUnderlyingName(
        functionsUtils.callStrByName(
            NAME, stratInfo.getStrategyUnderlyingAddress(), block, network)
            .orElseThrow(
                () -> new IllegalStateException("Can't fetch name for "
                    + stratInfo.getStrategyUnderlyingAddress())
            )
    );

    stratInfo.setPlatform(
        PlatformType.valueOfName(stratInfo.getStrategyUnderlyingName()).toString());

    if (stratInfo.isPlatform(PlatformType.UNKNOWN)) {
      //try to detect platform by other metrics
      String strategyRewardToken = functionsUtils.callAddressByName(
          REWARD_TOKEN,
          stratInfo.getStrategyAddress(),
          stratInfo.getBlock(),
          stratInfo.getNetwork()
      ).orElseThrow(
          () -> new IllegalStateException("Can't fetch reward token for "
              + stratInfo.getStrategyAddress())
      );
      String strategyRewardTokenName = functionsUtils.callStrByName(
          NAME,
          strategyRewardToken,
          stratInfo.getBlock(),
          stratInfo.getNetwork()
      ).orElseThrow();
      stratInfo.setPlatform(
          PlatformType.valueOfName(strategyRewardTokenName).toString());
    }

    fillStrategyBalance(stratInfo);
  }

  private void fillRewardToken(StratInfo stratInfo) {
    getFiller(stratInfo).fillRewardTokenAddress(stratInfo);

    for (StratRewardInfo rewardInfo : stratInfo.getRewardTokens()) {
      rewardInfo.setPrice(
          priceProvider.getPriceForCoin(rewardInfo.getAddress(),
              stratInfo.getBlock(), stratInfo.getNetwork()));

      rewardInfo.setName(functionsUtils.callStrByName(
          NAME,
          rewardInfo.getAddress(),
          stratInfo.getBlock(),
          stratInfo.getNetwork()
      ).orElseThrow(
          () -> new IllegalStateException("Can't fetch reward token for "
              + rewardInfo.getAddress())
      ));
    }
  }

  private void fillStrategyBalance(StratInfo stratInfo) {
    String address = stratInfo.getStrategyAddress();
    long block = stratInfo.getBlock();
    String network = stratInfo.getNetwork();
    String underlying = stratInfo.getStrategyUnderlyingAddress();
    stratInfo.setStrategyBalance(functionsUtils.fetchUint256Field(
        INVESTED_UNDERLYING_BALANCE,
        address,
        underlying,
        block,
        network));
    stratInfo.setStrategyBalanceUsd(
        stratInfo.getStrategyBalance() * stratInfo.getStrategyUnderlyingPrice());
  }

}
