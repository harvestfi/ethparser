package pro.belbix.ethparser.web3.harvest.strategy;

import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF_UNDERLYING;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMPTROLLER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMP_ACCRUED;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMP_SUPPLIER_INDEX;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COMP_SUPPLY_STATE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_ASSETS_IN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.REWARD_TOKEN;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_BORROWS_CURRENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.SimpleDecoder;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class CompFiller implements FarmableProjectFiller {

  private final static String DISTRIBUTED_SUPPLIER_COMP_EVENT =
      "0x2caecd17d02f56fa897705dcc740da2d237c373f70686f4e0d9bd3bf0400ea7a";
  private final static SimpleDecoder SIMPLE_DECODER = new SimpleDecoder();
  private final ObjectMapper om = ObjectMapperFactory.getObjectMapper();
  private final FunctionsUtils functionsUtils;
  private final EthBlockService ethBlockService;
  private final PriceProvider priceProvider;
  private final Web3Functions web3Functions;
  private final AppProperties appProperties;

  public CompFiller(FunctionsUtils functionsUtils,
      EthBlockService ethBlockService, PriceProvider priceProvider,
      Web3Functions web3Functions, AppProperties appProperties) {
    this.functionsUtils = functionsUtils;
    this.ethBlockService = ethBlockService;
    this.priceProvider = priceProvider;
    this.web3Functions = web3Functions;
    this.appProperties = appProperties;
  }

  // comptroller has a role pool for COMP
  public void fillPoolInfo(StratInfo stratInfo) {
    stratInfo.setPoolAddress(functionsUtils.callAddressByName(
        COMPTROLLER,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow(
        () -> new IllegalStateException("Can't fetch comptroller for "
            + stratInfo.getStrategyAddress())
    ));

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

  @SuppressWarnings("unchecked")
  public void fillRewards(StratInfo stratInfo) {
    long block = stratInfo.getBlock();
    String network = stratInfo.getNetwork();
    double supplyIndex = functionsUtils.callViewFunction(
        new Function(
            COMP_SUPPLY_STATE,
            List.of(new Address(stratInfo.getPoolSpecificUnderlying())),
            List.of(
                silentCall(() -> TypeReference.makeTypeReference("uint224")).orElseThrow(),
                silentCall(() -> TypeReference.makeTypeReference("uint32")).orElseThrow()
            )
        ),
        stratInfo.getPoolAddress(),
        block,
        network)
        .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .map(BigInteger::new)
        .orElseThrow().doubleValue();

    double supplierIndex = functionsUtils.callViewFunction(
        new Function(
            COMP_SUPPLIER_INDEX,
            List.of(
                new Address(stratInfo.getPoolSpecificUnderlying()),
                new Address(stratInfo.getStrategyAddress())
            ),
            List.of(silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow())
        ),
        stratInfo.getPoolAddress(),
        block,
        network)
        .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .map(BigInteger::new)
        .orElseThrow().doubleValue();

    if (supplierIndex == 0.9 && supplyIndex > 0) {
      supplierIndex = 1e36;
    }
    double deltaIndex = supplyIndex - supplierIndex;
    double supplierTokens = functionsUtils.callIntByNameWithAddressArg(
        BALANCE_OF,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolSpecificUnderlying(),
        block,
        network
    ).orElseThrow().doubleValue();

    double supplierDelta = supplierTokens * deltaIndex / 1e36;

    double compAccrued = functionsUtils.callIntByNameWithAddressArg(
        COMP_ACCRUED,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        block,
        network
    ).orElseThrow().doubleValue();

    double claimableTokens = compAccrued + supplierDelta;
    stratInfo.setClaimableTokens(functionsUtils.parseAmount(
        BigInteger.valueOf((long) claimableTokens),
        stratInfo.getRewardTokenAddress(),
        network
    ));

    stratInfo.setClaimableTokensUsd(stratInfo.getClaimableTokens()
        * stratInfo.getRewardTokenPrice());

    int lastClaimBlock = findLastClaimBlockNumber(
        stratInfo.getPoolAddress(),
        stratInfo.getPoolSpecificUnderlying(),
        stratInfo.getStrategyAddress(),
        Objects.requireNonNullElse(
            stratInfo.getStrategyCreated(),
            block - 100000)
            .intValue(),
        (int) block,
        network
    );

    long currentTs = ethBlockService.getTimestampSecForBlock(stratInfo.getBlock(), network);
    long lastClaimTs = ethBlockService.getTimestampSecForBlock(lastClaimBlock, network);
    stratInfo.setRewardPeriod(currentTs - lastClaimTs);
  }

  @SuppressWarnings("rawtypes")
  private int findLastClaimBlockNumber(
      String comptroller,
      String cToken,
      String strategyAddress,
      int strategyCreated,
      int block,
      String network) {
    int step = appProperties.getHandleLoopStep();
    int start = Math.max(strategyCreated, block - step);
    int end = block;
    while (true) {
      List<LogResult> results =
          web3Functions.fetchContractLogsBatch(List.of(comptroller), start, end, network,
              DISTRIBUTED_SUPPLIER_COMP_EVENT);
      int minted = findLastClaimLogBlockNumber(results, strategyAddress, cToken);
      if (minted != 0) {
        return minted;
      }
      if (start <= strategyCreated) {
        break;
      }
      end = start;
      start = Math.max(start - step, strategyCreated);
    }
    return strategyCreated;
  }

  @SuppressWarnings("rawtypes")
  private Integer findLastClaimLogBlockNumber(List<LogResult> results, String strategyAddress,
      String cToken) {
    return SIMPLE_DECODER.findLogByPredicate(results, l ->
        ((Address) l.get(0)).getValue().equalsIgnoreCase(cToken)
            && ((Address) l.get(1)).getValue().equalsIgnoreCase(strategyAddress)
    ).map(Log::getBlockNumber)
        .map(BigInteger::intValue)
        .orElse(0);
  }

  public void fillRewardTokenAddress(StratInfo stratInfo) {
    stratInfo.setRewardTokenAddress(functionsUtils.callAddressByName(
        REWARD_TOKEN,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow(
        () -> new IllegalStateException("Can't fetch reward token for "
            + stratInfo.getStrategyAddress())
    ));
  }
}
