package pro.belbix.ethparser.web3.harvest.strategy;

import static pro.belbix.ethparser.utils.CommonUtils.aprToApy;
import static pro.belbix.ethparser.utils.CommonUtils.calculateApr;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.INVESTED_UNDERLYING_BALANCE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.deployer.transform.PlatformType;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class StratInfoCollector {

  private final FunctionsUtils functionsUtils;
  private final PriceProvider priceProvider;
  private final CurveUtils curveUtils;
  private final ContractDbService contractDbService;

  public StratInfoCollector(Web3Functions web3Functions,
      FunctionsUtils functionsUtils, PriceProvider priceProvider,
      CurveUtils curveUtils,
      ContractDbService contractDbService) {
    this.curveUtils = curveUtils;
    this.functionsUtils = functionsUtils;
    this.priceProvider = priceProvider;
    this.contractDbService = contractDbService;
  }

  public StratInfo collect(String strategyAddress, long block, String network) {
    StratInfo stratInfo = new StratInfo(strategyAddress, block, network);

    fillContractStats(stratInfo);

    fillStrategyUnderlyingInfo(stratInfo);

    // farmable pool
    fillFarmablePool(stratInfo);
    fillPoolBalance(stratInfo);
    fillPoolTotalSupply(stratInfo);

    fillRewardsInPool(stratInfo);
    return stratInfo;
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

  private void fillRewardsInPool(StratInfo stratInfo) {
    if (stratInfo.isPlatform(PlatformType.CURVE)) {
      curveUtils.fillRewards(stratInfo);
    }

    double apr = calculateApr(stratInfo.getRewardPeriod(),
        stratInfo.getClaimableTokensUsd(),
        stratInfo.getStrategyBalanceUsd());
    double apy = aprToApy(apr, 365);

    stratInfo.setApr(apr);
    stratInfo.setApy(apy);

    log.info("Pool has rewards {} (${}) for period {} hours, apr {}(apy {})",
        stratInfo.getClaimableTokens(), stratInfo.getClaimableTokensUsd(),
        Duration.of(stratInfo.getRewardPeriod(), ChronoUnit.SECONDS).toHours(),
        apr, apy);
  }

  private void fillPoolTotalSupply(StratInfo stratInfo) {
    String poolAddress = stratInfo.getPoolAddress();
    String underlyingAddress = stratInfo.getStrategyUnderlyingAddress();
    long block = stratInfo.getBlock();
    String network = stratInfo.getNetwork();
    stratInfo.setPoolTotalSupply(functionsUtils.fetchUint256Field(
        TOTAL_SUPPLY,
        poolAddress,
        underlyingAddress,
        block,
        network));

    stratInfo.setPercentOfPool(
        (stratInfo.getPoolBalance() / stratInfo.getPoolTotalSupply()) * 100);

    stratInfo.setPercentOfInvested(
        100 - (((stratInfo.getStrategyBalance() - stratInfo.getPoolBalance())
            / stratInfo.getStrategyBalance()) * 100));
  }

  private void fillPoolBalance(StratInfo stratInfo) {
    String poolAddress = stratInfo.getPoolAddress();
    String underlyingAddress = stratInfo.getStrategyUnderlyingAddress();
    String strategyAddress = stratInfo.getStrategyAddress();
    long block = stratInfo.getBlock();
    String network = stratInfo.getNetwork();
    stratInfo.setPoolBalance(functionsUtils.fetchUint256Field(
        BALANCE_OF,
        poolAddress,
        underlyingAddress,
        block,
        network,
        strategyAddress));
  }

  private void fillFarmablePool(StratInfo stratInfo) {
    if (stratInfo.isPlatform(PlatformType.CURVE)) {
      curveUtils.fillPoolAddress(stratInfo);
    }
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

    fillStrategyBalance(stratInfo);
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
