package pro.belbix.ethparser.web3.harvest.strategy;

import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF_UNDERLYING;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMPTROLLER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_ASSETS_IN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_BORROWS_CURRENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.model.StratRewardInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
@Log4j2
public class CompFiller implements FarmableProjectFiller {

  private final static String DISTRIBUTED_SUPPLIER_COMP_EVENT =
      "0x2caecd17d02f56fa897705dcc740da2d237c373f70686f4e0d9bd3bf0400ea7a";
  private final ObjectMapper om = ObjectMapperFactory.getObjectMapper();
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;

  public CompFiller(FunctionsUtils functionsUtils,
      Web3Functions web3Functions) {
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
  }

  @Override
  public void fillPoolAddress(StratInfo stratInfo) {
    stratInfo.setPoolAddress(functionsUtils.callAddressByName(
        COMPTROLLER,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow(
        () -> new IllegalStateException("Can't fetch comptroller for "
            + stratInfo.getStrategyAddress())
    ));
  }

  @Override
  public void fillRewardTokenAddress(StratInfo stratInfo) {
    stratInfo.getRewardTokens().add(new StratRewardInfo(functionsUtils.callAddressByName(
        REWARD_TOKEN,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow(
        () -> new IllegalStateException("Can't fetch reward token for "
            + stratInfo.getStrategyAddress())
    )));
  }

  // comptroller has a role pool for COMP
  @Override
  public void fillPoolInfo(StratInfo stratInfo) {
    stratInfo.setPoolSpecificUnderlying(functionsUtils.callViewFunction(
        new Function(
            GET_ASSETS_IN,
            List.of(new Address(stratInfo.getStrategyAddress())),
            List.of(silentCall(() ->
                TypeReference.makeTypeReference("address[]")).orElseThrow())
        ),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .orElseThrow(
            () -> new IllegalStateException("Can't fetch cToken for "
                + stratInfo.getStrategyAddress())
        ));

    fillPoolBalance(stratInfo);
    fillPoolTotalSupply(stratInfo);
  }

  @Override
  public void fillRewards(StratInfo stratInfo) {
    String network = stratInfo.getNetwork();

    BigDecimal claimableTokens = CompUtils.calculateRewards(
        functionsUtils,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolSpecificUnderlying(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    );

    StratRewardInfo rewardInfo = stratInfo.getRewardTokens().get(0);
    rewardInfo.setAmount(functionsUtils.parseAmount(
        claimableTokens.toBigInteger(),
        stratInfo.getRewardTokens().get(0).getAddress(),
        network
    ));
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
        DISTRIBUTED_SUPPLIER_COMP_EVENT
    ).map(Log::getBlockNumber)
        .map(BigInteger::intValue)
        .orElse(0);
  }

  private void fillPoolBalance(StratInfo stratInfo) {
    stratInfo.setPoolBalance(functionsUtils.callViewFunction(
        new Function(
            BALANCE_OF_UNDERLYING,
            List.of(new Address(stratInfo.getStrategyAddress())),
            List.of(silentCall(() -> TypeReference.makeTypeReference("uint")).orElseThrow())
        ),
        stratInfo.getPoolSpecificUnderlying(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .map(BigInteger::new)
        .map(x -> functionsUtils.parseAmount(
            x, stratInfo.getStrategyUnderlyingAddress(), stratInfo.getNetwork()))
        .orElseThrow());
  }

  private void fillPoolTotalSupply(StratInfo stratInfo) {
    stratInfo.setPoolTotalSupply(functionsUtils.callIntByName(
        TOTAL_BORROWS_CURRENT,
        stratInfo.getPoolSpecificUnderlying(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).map(x -> functionsUtils.parseAmount(
        x, stratInfo.getStrategyUnderlyingAddress(), stratInfo.getNetwork()))
        .orElseThrow());
  }

  private Predicate<? super List<Type>> lastClaimPredicate(StratInfo stratInfo) {
    return l ->
        ((Address) l.get(0)).getValue().equalsIgnoreCase(stratInfo.getPoolSpecificUnderlying())
            && ((Address) l.get(1)).getValue().equalsIgnoreCase(stratInfo.getStrategyAddress());
  }
}
