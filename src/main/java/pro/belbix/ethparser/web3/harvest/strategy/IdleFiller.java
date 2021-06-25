package pro.belbix.ethparser.web3.harvest.strategy;

import static java.math.RoundingMode.HALF_UP;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.ALL_AVAILABLE_TOKENS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.FEE;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_GOV_TOKENS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GOV_TOKENS_INDEXES;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GOV_TOKENS_LAST_BALANCES;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.IDLE_CONTROLLER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.IDLE_UNDERLYING;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.USERS_GOV_TOKENS_INDEXES;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;
import static pro.belbix.ethparser.web3.contracts.ContractUtils.getControllerAddressByBlockAndNetwork;

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
import pro.belbix.ethparser.entity.StratInfo;
import pro.belbix.ethparser.model.StratRewardInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
@Log4j2
public class IdleFiller implements FarmableProjectFiller {

  private final static String TRANSFER_EVENT =
      "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef".toLowerCase();
  private final static String IDLE_TOKEN =
      "0x875773784af8135ea0ef43b5a374aad105c5d39e".toLowerCase();
  private final static String AAVE_TOKEN =
      "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9".toLowerCase();
  private final static String ST_AAVE_TOKEN =
      "0x4da27a545c0c5B758a6BA100e3a049001de870f5".toLowerCase();
  private final static String COMP_TOKEN =
      "0xc00e94cb662c3520282e6f5717214004a7f26888".toLowerCase();
  private final static double FULL_ALLOC = 100000.0;
  private final ObjectMapper om = ObjectMapperFactory.getObjectMapper();
  private final FunctionsUtils functionsUtils;
  private final Web3Functions web3Functions;
  private final CompFiller compFiller;

  public IdleFiller(FunctionsUtils functionsUtils,
      Web3Functions web3Functions,
      CompFiller compFiller) {
    this.functionsUtils = functionsUtils;
    this.web3Functions = web3Functions;
    this.compFiller = compFiller;
  }

  @Override
  public void fillPoolAddress(StratInfo stratInfo) {
    stratInfo.setPoolAddress(functionsUtils.callAddressByName(
        IDLE_UNDERLYING,
        stratInfo.getStrategyAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork()
    ).orElseThrow(
        () -> new IllegalStateException("Can't fetch IDLE_UNDERLYING for "
            + stratInfo.getStrategyAddress())
    ));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void fillRewardTokenAddress(StratInfo stratInfo) {
    List govTokens = functionsUtils.callViewFunction(
        new Function(
            GET_GOV_TOKENS,
            List.of(),
            List.of(silentCall(() -> TypeReference.makeTypeReference("address[]")).orElseThrow())
        ),
        stratInfo.getPoolAddress(),
        stratInfo.getBlock(),
        stratInfo.getNetwork())
        .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
        .flatMap(raw -> silentCall(() -> om.readValue(raw, List.class)))
        .orElseThrow();

    for (Object govToken : govTokens) {
      stratInfo.getRewardTokens()
          .add(new StratRewardInfo(swapTokenToUnderlying((String) govToken)));
    }
  }

  @Override
  public void fillPoolInfo(StratInfo stratInfo) {
    fillPoolBalance(stratInfo);
    fillPoolTotalSupply(stratInfo);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void fillRewards(StratInfo stratInfo) {
    long block = stratInfo.getBlock();
    String network = stratInfo.getNetwork();
    String idleTokenGovernance = stratInfo.getPoolAddress();

    BigDecimal usrBal = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
        BALANCE_OF,
        stratInfo.getStrategyAddress(),
        idleTokenGovernance,
        block,
        network
    ).orElseThrow(
        () -> new IllegalStateException("Can't fetch BALANCE_OF for "
            + idleTokenGovernance)));

    for (StratRewardInfo rewardInfo : stratInfo.getRewardTokens()) {
      String govToken = swapUnderlyingToToken(rewardInfo.getAddress());
      BigDecimal usrIndex = new BigDecimal(functionsUtils.callViewFunction(
          new Function(
              USERS_GOV_TOKENS_INDEXES,
              List.of(
                  new Address(govToken),
                  new Address(stratInfo.getStrategyAddress())
              ),
              List.of(silentCall(() -> TypeReference.makeTypeReference("uint256")).orElseThrow())
          ),
          idleTokenGovernance,
          block,
          network)
          .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
          .map(BigInteger::new)
          .orElseThrow());

      BigDecimal govTokensIndex = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
          GOV_TOKENS_INDEXES,
          govToken,
          idleTokenGovernance,
          block,
          network).orElseThrow(
          () -> new IllegalStateException("Can't fetch BALANCE_OF for "
              + idleTokenGovernance)));

      double govTokenAdditional = underlyingClaimable(stratInfo, govToken);
      if (govTokenAdditional > 0) {
        BigDecimal govBal = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
            BALANCE_OF,
            idleTokenGovernance,
            govToken,
            block,
            network
        ).orElseThrow());

        BigDecimal govTokensLastBalance = new BigDecimal(functionsUtils.callIntByNameWithAddressArg(
            GOV_TOKENS_LAST_BALANCES,
            govToken,
            idleTokenGovernance,
            block,
            network
        ).orElseThrow());

        BigDecimal totalSupply = new BigDecimal(functionsUtils.callIntByName(
            TOTAL_SUPPLY,
            idleTokenGovernance,
            stratInfo.getBlock(),
            stratInfo.getNetwork()
        ).orElseThrow());

        BigDecimal govBalUpdated = govBal.add(BigDecimal.valueOf(govTokenAdditional));

        govTokensIndex = govTokensIndex.add(
            govBalUpdated.subtract(govTokensLastBalance)
                .multiply(BigDecimal.valueOf(D18))
                .divide(totalSupply, 999, HALF_UP)
        );
      }

      BigDecimal delta = govTokensIndex.subtract(usrIndex);
      BigDecimal share = usrBal.multiply(delta).divide(BigDecimal.valueOf(D18), 999, HALF_UP);
      BigDecimal fee = new BigDecimal(functionsUtils.callIntByName(
          FEE,
          idleTokenGovernance,
          block,
          network
      ).orElseThrow());

      BigDecimal feeDue = share.multiply(fee).divide(BigDecimal.valueOf(FULL_ALLOC), 999, HALF_UP);
      BigDecimal claimable = share.subtract(feeDue);

      double claimablePretty = functionsUtils.parseAmount(
          claimable.toBigInteger(),
          govToken,
          network
      );

      //TODO fix calculation
      claimablePretty = 0.;
      rewardInfo.setAmount(claimablePretty);
    }

    // **************************************
//
//    List govTokensAmounts = functionsUtils.callViewFunction(
//        new Function(
//            GET_GOV_TOKENS_AMOUNTS,
//            List.of(new Address(stratInfo.getStrategyAddress())),
//            List.of(silentCall(() -> TypeReference.makeTypeReference("uint256[]")).orElseThrow())
//        ),
//        stratInfo.getPoolAddress(),
//        block,
//        network)
//        .flatMap(raw -> silentCall(() -> (String) om.readValue(raw, List.class).get(0)))
//        .flatMap(raw -> silentCall(() -> om.readValue(raw, List.class)))
//        .orElseThrow();
//
//    for (int i = 0; i < govTokensAmounts.size(); i++) {
//      StratRewardInfo rewardInfo = stratInfo.getRewardTokens().get(i);
//      BigInteger claimable = new BigInteger((String) govTokensAmounts.get(i));
//      double claimablePretty = functionsUtils.parseAmount(
//          claimable, rewardInfo.getAddress(), network);
//      rewardInfo.setAmount(claimablePretty);
//    }
  }

  @Override
  public int lastClaimBlock(StratInfo stratInfo) {
    // idle claim doesn't have their own events, so just parse transfer
    return web3Functions.findLastLogEvent(
        IDLE_TOKEN,
        Objects.requireNonNullElse(
            stratInfo.getStrategyCreated(),
            stratInfo.getBlock() - 100000)
            .intValue(),
        (int) stratInfo.getBlock(),
        stratInfo.getNetwork(),
        lastClaimPredicate(stratInfo),
        TRANSFER_EVENT
    ).map(Log::getBlockNumber)
        .map(BigInteger::intValue)
        .orElse(0);
  }

  private double underlyingClaimable(StratInfo stratInfo, String address) {
    if (COMP_TOKEN.equalsIgnoreCase(address)) {
      //TODO compound doesn't show stats for idle contract, need to figuring out why
//      compClaimable(underlyingInfo);
    } else if (IDLE_TOKEN.equalsIgnoreCase(address)) {
      return idleClaimable(stratInfo.getPoolAddress(),
          stratInfo.getBlock(), stratInfo.getNetwork());
    } // todo add AAVE
    return 0;
  }

  private double idleClaimable(
      String idlePool,
      long block,
      String network
  ) {
    String comptroller = functionsUtils.callAddressByName(
        IDLE_CONTROLLER,
        idlePool,
        block,
        network
    ).orElseThrow();
    return IdleUtils.calculateRewards(
        functionsUtils,
        idlePool,
        comptroller,
        idlePool,
        block,
        network
    ).doubleValue();
  }

  private void compClaimable(StratInfo underlyingInfo) {
    underlyingInfo.setPoolAddress(functionsUtils.callAddressByNameWithArg(
        ALL_AVAILABLE_TOKENS,
        "0",
        underlyingInfo.getStrategyAddress(),
        underlyingInfo.getBlock(),
        underlyingInfo.getNetwork()
    ).orElseThrow(
        () -> new IllegalStateException("Can't fetch comptroller for "
            + underlyingInfo.getStrategyAddress())
    ));
    compFiller.fillPoolInfo(underlyingInfo);
    compFiller.fillRewardTokenAddress(underlyingInfo);
    compFiller.fillRewards(underlyingInfo);
  }

  private void fillPoolTotalSupply(StratInfo stratInfo) {
    stratInfo.setPoolTotalSupply(functionsUtils.parseAmount(
        functionsUtils.callIntByName(
            TOTAL_SUPPLY,
            stratInfo.getPoolAddress(),
            stratInfo.getBlock(),
            stratInfo.getNetwork()
        ).orElseThrow(
            () -> new IllegalStateException("Can't fetch TOTAL_SUPPLY for "
                + stratInfo.getPoolAddress())
        ), IDLE_TOKEN, stratInfo.getNetwork()));
  }

  private void fillPoolBalance(StratInfo stratInfo) {
    stratInfo.setPoolBalance(functionsUtils.parseAmount(
        functionsUtils.callIntByNameWithAddressArg(
            BALANCE_OF,
            stratInfo.getStrategyAddress(),
            stratInfo.getPoolAddress(),
            stratInfo.getBlock(),
            stratInfo.getNetwork()
        ).orElseThrow(
            () -> new IllegalStateException("Can't fetch BALANCE_OF for "
                + stratInfo.getPoolAddress())
        ), IDLE_TOKEN, stratInfo.getNetwork()));
  }

  private String swapTokenToUnderlying(String govToken) {
    if (govToken.equalsIgnoreCase(ST_AAVE_TOKEN)) {
      return AAVE_TOKEN;
    }
    return govToken;
  }

  private String swapUnderlyingToToken(String token) {
    if (token.equalsIgnoreCase(AAVE_TOKEN)) {
      return ST_AAVE_TOKEN;
    }
    return token;
  }

  private Predicate<? super List<Type>> lastClaimPredicate(StratInfo stratInfo) {
    return l ->
        ((Address) l.get(0)).getValue().equalsIgnoreCase(stratInfo.getStrategyAddress())
            && ((Address) l.get(1)).getValue()
            .equalsIgnoreCase(getControllerAddressByBlockAndNetwork(stratInfo.getBlock(),ETH_NETWORK));
  }
}
