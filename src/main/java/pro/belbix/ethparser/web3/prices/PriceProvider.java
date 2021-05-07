package pro.belbix.ethparser.web3.prices;

import static java.util.Objects.requireNonNullElse;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.COINS;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MINTER;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.NAME;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractUtils.getBaseNetworkWrappedTokenAddress;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@Service
@Log4j2
public class PriceProvider {

  private final Map<String, TreeMap<Long, Double>> lastPrices = new HashMap<>();
  private final Pageable limitOne = PageRequest.of(0, 1);

  private final FunctionsUtils functionsUtils;
  private final AppProperties appProperties;
  private final PriceOracle priceOracle;
  private final ContractDbService contractDbService;

  public PriceProvider(FunctionsUtils functionsUtils, PriceRepository priceRepository,
      AppProperties appProperties, PriceOracle priceOracle,
      ContractDbService contractDbService) {
    this.functionsUtils = functionsUtils;
    this.appProperties = appProperties;
    this.priceOracle = priceOracle;
    this.contractDbService = contractDbService;
  }

  public double getLpTokenUsdPrice(String lpAddress, double amount, long block, String network) {
      return getLpTokenUsdPriceFromEth(lpAddress, amount, block, network);
  }

  public double getLpTokenUsdPriceFromEth(String lpAddress, double amount, long block,
      String network) {
    if (appProperties.isOnlyApi()) {
      return 0.0;
    }

    if (PriceOracle.isAvailable(block, network)) {
      return amount * priceOracle.getPriceForCoinOnChain(lpAddress, block, network);
    }
    log.info("Oracle not deployed yet, use direct calculation for prices");
    Tuple2<Double, Double> lpPooled = functionsUtils.callReserves(
        lpAddress, block, network);
    if (lpPooled == null) {
      throw new IllegalStateException("Can't reach reserves for " + lpAddress);
    }
    double lpBalance = contractDbService.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, lpAddress, block, network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + lpAddress)),
        lpAddress, network);
    double usdValue = calculateLpTokenPrice(lpAddress, lpPooled, lpBalance, amount, block, network);
    log.info("{} USD value fetched {} for {} at block {}",
        lpAddress, amount, usdValue, block);
    return usdValue;
  }

  private double calculateLpTokenPrice(String lpAddress,
      Tuple2<Double, Double> lpPooled,
      double lpBalance,
      double amount,
      long block,
      String network
  ) {
    Tuple2<String, String> tokensAdr = contractDbService
        .tokenAddressesByUniPairAddress(lpAddress, network);

    double positionFraction = amount / lpBalance;

    double firstCoin = positionFraction * lpPooled.component1();
    double secondCoin = positionFraction * lpPooled.component2();

    double firstVaultUsdAmount =
        firstCoin * getPriceForCoin(tokensAdr.component1(), block, network);
    double secondVaultUsdAmount =
        secondCoin * getPriceForCoin(tokensAdr.component2(), block, network);
    return firstVaultUsdAmount + secondVaultUsdAmount;
  }

  // you can use Vault name instead of coinName if it is not a LP
  public Double getPriceForCoin(final String coinNameOrAddress, long block, String network) {
    if (ZERO_ADDRESS.equalsIgnoreCase(coinNameOrAddress)) {
      return 0.0;
    }
    String coinAddress = coinNameOrAddress;
    if (!coinNameOrAddress.startsWith("0x")) {
      coinAddress = contractDbService
          .getAddressByName(coinNameOrAddress, ContractType.TOKEN, network)
          .orElseThrow(
              () -> new IllegalStateException("Not found address for " + coinNameOrAddress));
    }
//    String coinNameSimple = cu(network).getSimilarAssetForPrice(coinAddress);
    updateUSDPrice(coinAddress, block, network);
    return getLastPrice(coinAddress, block);
  }

  public Tuple2<Double, Double> getPairPriceForLpHash(
      String lpHash, Long block, String network) {
    Tuple2<String, String> tokensAdr = contractDbService
        .tokenAddressesByUniPairAddress(lpHash, network);
    String token0 = tokensAdr.component1();
    String token1 = tokensAdr.component2();
    // zero address in 1inch should be replaced with ETH
    if (ZERO_ADDRESS.equalsIgnoreCase(token0)) {
      token0 = getBaseNetworkWrappedTokenAddress(network);
    }
    if (ZERO_ADDRESS.equalsIgnoreCase(token1)) {
      token1 = getBaseNetworkWrappedTokenAddress(network);
    }
    return new Tuple2<>(
        getPriceForCoin(token0, block, network),
        getPriceForCoin(token1, block, network)
    );
  }

  private void updateUSDPrice(String address, long block, String network) {
    if (contractDbService.getContractByAddress(address, network)
        .filter(c -> c.getCreated() < block)
        .isEmpty()) {
      savePrice(0.0, address, block);
      return;
    }
    if (ContractUtils.isStableCoin(address)) {
      savePrice(1.0, address, block);
      return;
    }

    if (hasFreshPrice(address, block)) {
      return;
    }
    double price = getPriceForCoinFromEth(address, block, network);
    if (Double.isInfinite(price) || Double.isNaN(price)) {
      price = 0.0;
    }
    savePrice(price, address, block);
  }

  private double getPriceForCoinFromEth(String address, Long block, String network) {
    if (appProperties.isOnlyApi()) {
      return 0.0;
    }
    if (PriceOracle.isAvailable(block, network)) {
      return priceOracle.getPriceForCoinOnChain(address, block, network);
    }
    log.debug("Oracle not deployed yet, use direct calculation for prices");
    return getPriceForCoinFromEthLegacy(address, block, network, 0);
  }

  //for compatibility with CRV prices without oracle
  private double getPriceForCoinFromEthLegacy(String address, Long block, String network,
      int deep) {
    if (ContractUtils.isStableCoin(address)) {
      return 1.0;
    }
    // curve stab ETH as eee...ee
    if ("0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee".equalsIgnoreCase(address)) {
      address = ContractUtils.getBaseNetworkWrappedTokenAddress(network);
    }

    if (deep > 5) {
      log.error("Recursive price detected! {} {}", address, network);
      return 0;
    }

    // if we don't have contract in DB use it anyway
    ContractEntity c = contractDbService.getContractByAddress(address, network)
        .orElse(null);
    if (c != null && c.getCreated() > block) {
      return 0.0;
    }
    String lpHash = contractDbService
        .findPairByToken(address, block, network)
        .map(p -> p.getUniPair().getContract().getAddress())
        .orElse(null);

    if (lpHash == null) {
      try {
        String curveUnderlying = curveUnderlying(address, block, network);
        if (curveUnderlying != null) {
          return getPriceForCoinFromEthLegacy(curveUnderlying, block, network, deep + 1);
        }
      } catch (Exception ignore) {
      }

      String similarToken = tryToFindSimilarToken(address, block, network);
      if (Strings.isBlank(similarToken)) {
        log.error("Not found similar token for {}, use 1$ price", address);
        return 1;
      }
      return getPriceForCoinFromEthLegacy(similarToken, block, network, deep + 1);
    }

    Tuple2<Double, Double> reserves = functionsUtils
        .callReserves(lpHash, block, network);
    if (reserves == null) {
      log.error("Can't reach reserves for " + lpHash);
      return 0.0;
    }
    double price;
    if (isDivisionSequenceSecondDividesFirst(lpHash, address, network)) {
      price = reserves.component2() / reserves.component1();
    } else {
      price = reserves.component1() / reserves.component2();
    }

    Tuple2<String, String> lpTokenAdr = contractDbService
        .tokenAddressesByUniPairAddress(lpHash, network);
    String otherTokenAddress;
    if (lpTokenAdr.component1().equalsIgnoreCase(address)) {
      otherTokenAddress = lpTokenAdr.component2();
    } else if (lpTokenAdr.component2().equalsIgnoreCase(address)) {
      otherTokenAddress = lpTokenAdr.component1();
    } else {
      throw new IllegalStateException("Not found token in lp pair");
    }
    price *= getPriceForCoinFromEthLegacy(otherTokenAddress, block, network, deep + 1);
    if (Double.isNaN(price) || Double.isInfinite(price)) {
      price = 0.0;
    }
    log.info("Price {} fetched {} on block {}", address, price, block);
    return price;
  }

  //todo replace to Curve/Ellipses token determination
  private String tryToFindSimilarToken(String address, Long block, String network) {
    String tokenName = functionsUtils.callStrByName(NAME, address, block, network)
        .orElse("").toUpperCase();
    if (tokenName.contains("CURVE")) {
      try {
        String curveUnderlyingToken = curveUnderlying(address, block, network);
        if (curveUnderlyingToken != null) {
          return curveUnderlyingToken;
        }
      } catch (ClassNotFoundException | IOException ignored) {
      }
    }
    if (tokenName.contains("BTC")) {
      return ContractUtils.getBtcAddress(network);
    } else if (tokenName.contains("ETH")) {
      return ContractUtils.getEthAddress(network);
    } else if (
        tokenName.contains("USD")
            || tokenName.contains("UST")
            || tokenName.contains("DAI")
    ) {
      return ContractUtils.getUsdAddress(network);
    } else if (tokenName.contains("EUR")) {
      return ContractUtils.getEurAddress(network);
    } else if (tokenName.contains("LINK")) {
      return ContractUtils.getLinkAddress(network);
    }
    return "";
  }

  private String curveUnderlying(String address, Long block, String network)
      throws ClassNotFoundException, IOException {
    String minterAddress = functionsUtils.callAddressByName(MINTER, address, block, network)
        .orElse(null);
    if (minterAddress == null) {
      return null;
    }
    //noinspection unchecked
    String coinRaw = functionsUtils.callViewFunction(new Function(
            COINS,
            List.of(new Uint256(0)),
            List.of(TypeReference.makeTypeReference("address"))
        ),
        minterAddress, block, network).orElse(null);
    if (coinRaw == null) {
      return null;
    }
    return (String) ObjectMapperFactory.getObjectMapper().readValue(coinRaw, List.class)
        .get(0);
  }

  private boolean hasFreshPrice(String address, long block) {
    TreeMap<Long, Double> lastPriceByBlock = lastPrices.get(address);
    if (lastPriceByBlock == null) {
      return false;
    }

    Entry<Long, Double> entry = lastPriceByBlock.floorEntry(block);
    if (entry == null || Math.abs(entry.getKey() - block) >= 0) {
      return false;
    }
    return entry.getValue() != null && entry.getValue() != 0;
  }

  private void savePrice(double price, String name, long block) {
    TreeMap<Long, Double> lastPriceByBlock = lastPrices.computeIfAbsent(name, k -> new TreeMap<>());
    lastPriceByBlock.put(block, price);
  }

  private double getLastPrice(String address, long block) {
    TreeMap<Long, Double> lastPriceByBlocks = lastPrices.get(address);
    if (lastPriceByBlocks == null) {
      return 0.0;
    }
    Entry<Long, Double> entry = lastPriceByBlocks.floorEntry(requireNonNullElse(block, 0L));
    if (entry != null && entry.getValue() != null) {
      return entry.getValue();
    }
    return 0.0;
  }

  public boolean isDivisionSequenceSecondDividesFirst(
      String uniPairAddress,
      String tokenAddress,
      String network
  ) {
    Tuple2<String, String> tokens = contractDbService
        .tokenAddressesByUniPairAddress(uniPairAddress, network);
    if (tokens.component1().equalsIgnoreCase(tokenAddress)) {
      return true;
    } else if (tokens.component2().equalsIgnoreCase(tokenAddress)) {
      return false;
    } else {
      throw new IllegalStateException(
          "UniPair " + uniPairAddress + "doesn't contain " + tokenAddress);
    }
  }

}
