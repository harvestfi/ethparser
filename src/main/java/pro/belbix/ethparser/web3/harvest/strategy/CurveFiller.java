package pro.belbix.ethparser.web3.harvest.strategy;

import static java.math.RoundingMode.HALF_UP;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.CLAIMABLE_TOKENS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MINTER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.POOL;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.entity.StratInfo;
import pro.belbix.ethparser.model.StratRewardInfo;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.SimpleDecoder;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class CurveFiller implements FarmableProjectFiller {

  private final static String MINTED_EVENT = "0x9d228d69b5fdb8d273a2336f8fb8612d039631024ea9bf09c424a9503aa078f0";
  private final static String CRV_TOKEN = "0xD533a949740bb3306d119CC777fa900bA034cd52"
      .toLowerCase();
  private final static BigDecimal D18 = BigDecimal.valueOf(10L).pow(18);
  private final static SimpleDecoder SIMPLE_DECODER = new SimpleDecoder();
  private final ObjectMapper om = ObjectMapperFactory.getObjectMapper();
  private final FunctionsUtils functionsUtils;
  private final EthBlockService ethBlockService;
  private final PriceProvider priceProvider;
  private final Web3Functions web3Functions;
  private final AppProperties appProperties;

  public CurveFiller(FunctionsUtils functionsUtils,
      EthBlockService ethBlockService,
      PriceProvider priceProvider, Web3Functions web3Functions,
      AppProperties appProperties) {
    this.functionsUtils = functionsUtils;
    this.ethBlockService = ethBlockService;
    this.priceProvider = priceProvider;
    this.web3Functions = web3Functions;
    this.appProperties = appProperties;
  }

  @Override
  public void fillPoolAddress(StratInfo stratInfo) {
    stratInfo.setPoolAddress(
        functionsUtils.callAddressByName(
            POOL, stratInfo.getStrategyAddress(), stratInfo.getBlock(), stratInfo.getNetwork())
            .orElseThrow(
                () -> new IllegalStateException("Can't fetch underlying for "
                    + stratInfo.getStrategyAddress())
            ));
  }

  @Override
  public void fillRewardTokenAddress(StratInfo stratInfo) {
    stratInfo.getRewardTokens().add(new StratRewardInfo(CRV_TOKEN));
  }

  @Override
  public void fillPoolInfo(StratInfo stratInfo) {
    fillPoolBalance(stratInfo);
    fillPoolTotalSupply(stratInfo);
  }

  @Override
  public void fillRewards(StratInfo stratInfo) {
    double claimableTokens = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        CLAIMABLE_TOKENS,
        stratInfo.getStrategyAddress(),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow()).divide(D18, 99, HALF_UP).doubleValue();

    stratInfo.getRewardTokens().get(0).setAmount(claimableTokens);
  }

  @Override
  public int lastClaimBlock(StratInfo stratInfo) {
    String minterAddress = functionsUtils.callAddressByName(
        MINTER,
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork())
        .orElseThrow(
            () -> new IllegalStateException("Minter not found for " + stratInfo.getPoolAddress())
        );

    return web3Functions.findLastLogEvent(
        minterAddress,
        Objects.requireNonNullElse(
            stratInfo.getStrategyCreated(),
            stratInfo.getBlock() - 100000)
            .intValue(),
        (int) stratInfo.getBlock(),
        stratInfo.getNetwork(),
        lastClaimPredicate(stratInfo),
        stratInfo.getPoolSpecificUnderlying(),
        stratInfo.getStrategyAddress(),
        MINTED_EVENT
    ).map(Log::getBlockNumber)
        .map(BigInteger::intValue)
        .orElse(0);
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
  }

  private Predicate<? super List<Type>> lastClaimPredicate(StratInfo stratInfo) {
    return l ->
        ((Address) l.get(0)).getValue().equalsIgnoreCase(stratInfo.getStrategyAddress())
            && ((Address) l.get(1)).getValue().equalsIgnoreCase(stratInfo.getPoolAddress());
  }
}
