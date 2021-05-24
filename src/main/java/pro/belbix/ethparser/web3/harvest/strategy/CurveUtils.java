package pro.belbix.ethparser.web3.harvest.strategy;

import static java.math.RoundingMode.HALF_UP;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.CLAIMABLE_TOKENS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.CONTROLLER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GAUGE_RELATIVE_WEIGHT;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.INFLATION_RATE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.INTEGRATE_CHECKPOINT_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.INTEGRATE_FRACTION;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.INTEGRATE_INV_SUPPLY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.INTEGRATE_INV_SUPPLY_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MINTED;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MINTER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.PERIOD;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.POOL;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.WORKING_BALANCES;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.WORKING_SUPPLY;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
@Log4j2
public class CurveUtils {

  private final static BigDecimal D18 = BigDecimal.valueOf(10L).pow(18);
  private final ObjectMapper om = ObjectMapperFactory.getObjectMapper();
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;
  private final EthBlockService ethBlockService;

  public CurveUtils(FunctionsUtils functionsUtils,
      Web3Functions web3Functions, EthBlockService ethBlockService) {
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
    this.ethBlockService = ethBlockService;
  }

  //it should be fixed and honestly don't need anymore
  public void calculateToMint(StratInfo stratInfo) {
    String minterAddress = functionsUtils.callAddressByName(
        MINTER,
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork())
        .orElseThrow(
            () -> new IllegalStateException("Minter not found for " + stratInfo.getPoolAddress())
        );

    String controller = functionsUtils.callAddressByName(
        CONTROLLER,
        minterAddress,
        stratInfo.getBlock(),
        stratInfo.getNetwork())
        .orElseThrow(
            () -> new IllegalStateException("Controller not found for " + minterAddress)
        );

    @SuppressWarnings("unchecked")
    BigDecimal gaugeRelativeWeight = new BigDecimal(functionsUtils.callViewFunction(
        new Function(
            GAUGE_RELATIVE_WEIGHT,
            List.of(new Address(stratInfo.getPoolAddress())),
            List.of(silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow())
        ),
        controller,
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .orElseThrow());

    BigDecimal rate = new BigDecimal(functionsUtils.callIntByName(
        INFLATION_RATE,
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()).orElseThrow());

    long lastClaimTs = functionsUtils.callIntByNameWithAddressArg(
        INTEGRATE_CHECKPOINT_OF,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow().longValue();

    BigDecimal workingSupply = new BigDecimal(functionsUtils.callIntByName(
        WORKING_SUPPLY,
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow());

    BigDecimal integrateInvSupplyOf = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        INTEGRATE_INV_SUPPLY_OF,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow());

    BigDecimal workingBalance = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        WORKING_BALANCES,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow());

    BigDecimal integrateFraction = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        INTEGRATE_FRACTION,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow());

    @SuppressWarnings("unchecked")
    BigDecimal minted = new BigDecimal(functionsUtils.callViewFunction(
        new Function(
            MINTED,
            List.of(new Address(stratInfo.getStrategyAddress()),
                new Address(stratInfo.getPoolAddress())),
            List.of(silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow())
        ),
        minterAddress,
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .orElseThrow());

    long period = functionsUtils.callIntByName(
        PERIOD,
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow().longValue();

    @SuppressWarnings("unchecked")
    BigDecimal integrateInvSupplyToPeriod = new BigDecimal(functionsUtils.callViewFunction(
        new Function(
            INTEGRATE_INV_SUPPLY,
            List.of(new Uint256(period)),
            List.of(silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow())
        ),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .orElseThrow());

    long currentTs = ethBlockService.getTimestampSecForBlock(
        stratInfo.getBlock(), stratInfo.getNetwork());
    BigDecimal dt = BigDecimal.valueOf(currentTs - lastClaimTs);

    BigDecimal integrateInvSupply = rate.multiply(gaugeRelativeWeight).multiply(dt)
        .divide(workingSupply, 99, HALF_UP).add(integrateInvSupplyToPeriod);

    BigDecimal integrateFractionDif =
        workingBalance.multiply(integrateInvSupply.subtract(integrateInvSupplyOf))
            .divide(D18, 99, HALF_UP);

    BigDecimal integrateFractionFinal = integrateFraction.add(integrateFractionDif);

    double toMint = integrateFractionFinal.subtract(minted)
        .divide(D18, 99, HALF_UP).doubleValue();

    double claimableTokens = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        CLAIMABLE_TOKENS,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow()).divide(D18, 99, HALF_UP).doubleValue();

    log.info("Curve pool to mint {} for period {} hours",
        claimableTokens, Duration.of(dt.longValue(), ChronoUnit.SECONDS).toHours());
  }

  public void fillRewards(StratInfo stratInfo) {
    long lastClaimTs = functionsUtils.callIntByNameWithAddressArg(
        INTEGRATE_CHECKPOINT_OF,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow().longValue();

    double claimableTokens = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        CLAIMABLE_TOKENS,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow()).divide(D18, 99, HALF_UP).doubleValue();

    long currentTs = ethBlockService.getTimestampSecForBlock(
        stratInfo.getBlock(), stratInfo.getNetwork());
    long dt = currentTs - lastClaimTs;

    stratInfo.setClaimableTokens(claimableTokens);
    stratInfo.setClaimableTokensUsd(claimableTokens * stratInfo.getRewardTokenPrice());
    stratInfo.setRewardPeriod(dt);
  }

  public void fillPoolAddress(StratInfo stratInfo) {
    String address = stratInfo.getStrategyAddress();
    long block = stratInfo.getBlock();
    String network = stratInfo.getNetwork();

    //crv strategies has pool field
    stratInfo.setPoolAddress(
        functionsUtils.callAddressByName(POOL, address, block, network)
            .orElseThrow(
                () -> new IllegalStateException("Can't fetch underlying for " + address)
            ));
  }

}
