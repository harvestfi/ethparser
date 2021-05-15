package pro.belbix.ethparser.web3.prices;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.DECIMALS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PAIR;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_RESERVES;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOKEN0;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.UNI_FACTORIES;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;

@Service
@Log4j2
public class LPSeeker {

  private static final boolean CHECK_LIQUIDITY = false;
  private static final double MIN_LIQUIDITY = 100; //it is not USD, some token amount
  private final FunctionsUtils functionsUtils;
  private final PriceOracle priceOracle;
  private final ContractDbService contractDbService;

  public LPSeeker(FunctionsUtils functionsUtils,
      PriceOracle priceOracle,
      ContractDbService contractDbService) {
    this.functionsUtils = functionsUtils;
    this.priceOracle = priceOracle;
    this.contractDbService = contractDbService;
  }


  public String findLargestLP(
      String tokenAddress,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
//    if (PriceOracle.isAvailable(block, network)) {
//      String largestKeyToken = priceOracle.getLargestKeyToken(tokenAddress, block, network);
//      if (largestKeyToken != null) {
//        return largestKeyToken;
//      }
//      pairForTokens()
//    }
    return getUniLargestPool(tokenAddress, block, network, contracts);
  }

  private String getUniLargestPool(
      String tokenAddress,
      long block,
      String network,
      List<PureEthContractInfo> contracts
  ) {
    Set<String> tokenList = ContractConstants.KEY_TOKENS.get(network);
    TreeMap<BigInteger, String> pairsLiquidity = new TreeMap<>();
    for (String keyToken : tokenList) {
      for (String factory : UNI_FACTORIES.get(network).keySet()) {
        int factoryStart = UNI_FACTORIES.get(network).get(factory);
        if (factoryStart > block
            || !isEligibleKeyToken(tokenAddress, keyToken, network)) {
          continue;
        }

        String lpAddress = lpForTokens(factory, tokenAddress, keyToken, block, network);
        if (ZERO_ADDRESS.equalsIgnoreCase(lpAddress)) {
//          log.info("Zero pair for {} {} from {}", tokenAddress, keyToken, factoryStart);
          continue;
        }

        pairsLiquidity.put(getLiquidity(lpAddress, tokenAddress, block, network), lpAddress);
      }
    }
    if (pairsLiquidity.isEmpty()
        || isNotEnoughLiquidity(pairsLiquidity.lastEntry().getKey(), tokenAddress, block, network)
    ) {
      return null;
    }

    return pairsLiquidity.lastEntry().getValue();
  }

  private boolean isEligibleKeyToken(String tokenAddress, String keyToken, String network) {
    if (tokenAddress.equalsIgnoreCase(keyToken)) {
      return false;
    }
    boolean tokenIsKeyToken =
        ContractConstants.KEY_TOKENS.get(network).contains(tokenAddress.toLowerCase());
    boolean keyTokenIsStablecoin = ContractUtils.isStableCoin(keyToken);
    // for avoid recursion we should have keyToken -> Stablecoin pairs only
    return !tokenIsKeyToken || keyTokenIsStablecoin;
  }

  private boolean isNotEnoughLiquidity(BigInteger liq, String token, long block, String network) {
    if (!CHECK_LIQUIDITY) {
      return false;
    }
    long decimals = functionsUtils.callIntByName(DECIMALS, token, block, network)
        .orElseThrow().longValue();
    double liqD = new BigDecimal(liq)
        .divide(new BigDecimal(10L).pow((int) decimals), 99, RoundingMode.HALF_UP)
        .doubleValue();
    if (liqD < MIN_LIQUIDITY) {
      log.info("Liquidity for {} is {} and lower than threshold {}",
          token, liqD, MIN_LIQUIDITY);
      return false;
    }
    return true;
  }

  private String lpForTokens(
      String factory,
      String token0,
      String token1,
      long block,
      String network) {
    try {
      //noinspection unchecked
      String result = functionsUtils.callViewFunction(new Function(
          GET_PAIR,
          List.of(new Address(token0), new Address(token1)),
          List.of(TypeReference.makeTypeReference("address"))
      ), factory, block, network)
          .orElseThrow(() -> new IllegalStateException(
              "Error fetching pair for " + token0 + " " + token1 + " from " + factory
          ));
      return (String) ObjectMapperFactory.getObjectMapper().readValue(result, List.class)
          .get(0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private BigInteger getLiquidity(String pair, String token, long block, String network) {
    String token0 = functionsUtils.callStrByName(TOKEN0, pair, block, network)
        .orElseThrow(() -> new IllegalStateException(
            "Token0 not found for " + pair + " network: " + network
        ));
    List<BigInteger> reserves = getReserves(pair, block, network);
    if (token.equalsIgnoreCase(token0)) {
      return reserves.get(0);
    } else {
      return reserves.get(1);
    }
  }

  private List<BigInteger> getReserves(
      String pair,
      long block,
      String network) {
    try {
      //noinspection unchecked
      String result = functionsUtils.callViewFunction(new Function(
          GET_RESERVES,
          List.of(),
          List.of(TypeReference.makeTypeReference("uint112"),
              TypeReference.makeTypeReference("uint112"),
              TypeReference.makeTypeReference("uint32"))
      ), pair, block, network)
          .orElseThrow(() -> new IllegalStateException(
              "Can't fetch reserves for " + pair
          ));
      //noinspection unchecked
      List<String> resultList = ObjectMapperFactory.getObjectMapper()
          .readValue(result, List.class);
      if (resultList == null || resultList.size() != 3) {
        log.error("Wrong result for reserves: {}", resultList);
        return List.of();
      }
      return List.of(
          new BigInteger(resultList.get(0)),
          new BigInteger(resultList.get(1))
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


}
