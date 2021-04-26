package pro.belbix.ethparser.web3.prices;

import static java.util.Objects.requireNonNullElse;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ORACLES;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.utils.Caller;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class PriceProvider {

  private final Map<String, TreeMap<Long, Double>> lastPrices = new HashMap<>();
  private final Pageable limitOne = PageRequest.of(0, 1);

  private final FunctionsUtils functionsUtils;
  private final PriceRepository priceRepository;
  private final AppProperties appProperties;
  private final PriceOracle priceOracle;

  public PriceProvider(FunctionsUtils functionsUtils, PriceRepository priceRepository,
      AppProperties appProperties, PriceOracle priceOracle) {
    this.functionsUtils = functionsUtils;
    this.priceRepository = priceRepository;
    this.appProperties = appProperties;
    this.priceOracle = priceOracle;
  }

  public double getLpTokenUsdPrice(String lpAddress, double amount, long block, String network) {
    String lpName = cu(network).getNameByAddress(lpAddress)
        .orElseThrow(() -> new IllegalStateException("Not found lp name for " + lpAddress));
    PriceDTO priceDTO = silentCall(() -> priceRepository
        .fetchLastPrice(lpName, block, network, limitOne))
        .filter(Caller::isNotEmptyList)
        .map(l -> l.get(0))
        .orElse(null);
    if (priceDTO == null) {
      log.debug("Saved price not found for " + lpName + " at block " + block);
      return getLpTokenUsdPriceFromEth(lpAddress, amount, block, network);
    }
    if (priceDTO.getLpTotalSupply() == null
        || priceDTO.getLpToken0Pooled() == null
        || priceDTO.getLpToken1Pooled() == null) {
      log.warn("Saved price has wrong data for " + lpName + " at block " + block);
      return getLpTokenUsdPriceFromEth(lpAddress, amount, block, network);
    }
    Tuple2<Double, Double> lpPooled = new Tuple2<>(
        priceDTO.getLpToken0Pooled(),
        priceDTO.getLpToken1Pooled()
    );
    double lpBalance = priceDTO.getLpTotalSupply();
    return calculateLpTokenPrice(lpAddress, lpPooled, lpBalance, amount, block, network);
  }

  public double getLpTokenUsdPriceFromEth(String lpAddress, double amount, long block,
      String network) {
    if (appProperties.isOnlyApi()) {
      return 0.0;
    }

    if (block > ORACLES.get(network).component1()) {
      return amount * priceOracle.getPriceForCoinOnChain(lpAddress, block, network);
    }

    Tuple2<Double, Double> lpPooled = functionsUtils.callReserves(
        lpAddress, block, network);
    if (lpPooled == null) {
      throw new IllegalStateException("Can't reach reserves for " + lpAddress);
    }
    double lpBalance = cu(network).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, lpAddress, block, network)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + lpAddress)),
        lpAddress);
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
    Tuple2<String, String> tokensAdr = cu(network)
        .tokenAddressesByUniPairAddress(lpAddress);

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
      coinAddress = cu(network).getAddressByName(coinNameOrAddress, ContractType.TOKEN)
          .orElseThrow(
              () -> new IllegalStateException("Not found address for " + coinNameOrAddress));
    }
//    String coinNameSimple = cu(network).getSimilarAssetForPrice(coinAddress);
    updateUSDPrice(coinAddress, block, network);
    return getLastPrice(coinAddress, block);
  }

  public Tuple2<Double, Double> getPairPriceForLpHash(
      String lpHash, Long block, String network) {
    Tuple2<String, String> tokensAdr = cu(network)
        .tokenAddressesByUniPairAddress(lpHash);
    String token0 = tokensAdr.component1();
    String token1 = tokensAdr.component2();
    // zero address in 1inch should be replaced with ETH
    if (ZERO_ADDRESS.equalsIgnoreCase(token0)) {
      token0 = cu(network).getBaseNetworkWrappedTokenAddress();
    }
    if (ZERO_ADDRESS.equalsIgnoreCase(token1)) {
      token1 = cu(network).getBaseNetworkWrappedTokenAddress();
    }
    return new Tuple2<>(
        getPriceForCoin(token0, block, network),
        getPriceForCoin(token1, block, network)
    );
  }

  private void updateUSDPrice(String address, long block, String network) {
    if (!cu(network).isTokenCreated(address, block)) {
      savePrice(0.0, address, block);
      return;
    }
    if (cu(network).isStableCoin(address)) {
      savePrice(1.0, address, block);
      return;
    }

    if (hasFreshPrice(address, block)) {
      return;
    }
    double price = getPriceForCoinWithoutCache(address, block, network);

    savePrice(price, address, block);
  }

  private double getPriceForCoinWithoutCache(String address, Long block, String network) {
    String lpName = cu(network).findUniPairNameForTokenAddress(address, block)
        .orElse(null);
    PriceDTO priceDTO = silentCall(() -> priceRepository
        .fetchLastPrice(lpName, block, network, limitOne))
        .filter(Caller::isNotEmptyList)
        .map(l -> l.get(0))
        .orElse(null);
    if (priceDTO == null) {
      log.debug("Saved price not found for " + address + " at block " + block);
      return getPriceForCoinFromEth(address, block, network);
    }
    if (block - priceDTO.getBlock() > 1000) {
      log.warn("Price have not updated more then {} for {}", block - priceDTO.getBlock(), address);
      return getPriceForCoinFromEth(address, block, network);
    }

    String tokenName = cu(network).getNameByAddress(address).orElse("");
    if (!priceDTO.getToken().equalsIgnoreCase(tokenName)
        && !priceDTO.getOtherToken().equalsIgnoreCase(tokenName)) {
      throw new IllegalStateException("Wrong source for " + tokenName);
    }
    String otherTokenAddress =
        cu(network).getAddressByName(priceDTO.getOtherToken(), ContractType.TOKEN)
            .orElse("");
    double otherTokenPrice = getPriceForCoin(otherTokenAddress, block, network);
    return priceDTO.getPrice() * otherTokenPrice;
  }

  private double getPriceForCoinFromEth(String address, Long block, String network) {
    if (appProperties.isOnlyApi()) {
      return 0.0;
    }
    if (block > ORACLES.get(network).component1()) {
      return priceOracle.getPriceForCoinOnChain(address, block, network);
    }
    //LEGACY PART
    //for compatibility with CRV prices without oracle
    String tokenName = cu(network).getNameByAddress(address)
        .map(n -> cu(network).getSimilarAssetForPrice(n))
        .orElseThrow(() -> new IllegalStateException("Not found name for " + address));
    String similarAdr = cu(network).getAddressByName(tokenName, ContractType.TOKEN)
        .orElseThrow();
    if (cu(network).isStableCoin(similarAdr)) {
      return 1.0;
    }
    String lpName = cu(network).findUniPairNameForTokenName(tokenName, block)
        .orElseThrow(() -> new IllegalStateException("Not found LP for " + tokenName));
    if (!cu(network).isUniPairCreated(lpName, block)) {
      return 0.0;
    }
    String lpHash = cu(network)
        .getAddressByName(lpName, ContractType.UNI_PAIR)
        .orElseThrow(() -> new IllegalStateException("Not found hash for " + lpName));

    Tuple2<Double, Double> reserves = functionsUtils.callReserves(
        lpHash, block, network);
    if (reserves == null) {
      throw new IllegalStateException("Can't reach reserves for " + lpName);
    }
    double price;
    if (cu(network).isDivisionSequenceSecondDividesFirst(lpHash, similarAdr)) {
      price = reserves.component2() / reserves.component1();
    } else {
      price = reserves.component1() / reserves.component2();
    }

    Tuple2<String, String> lpTokenAdr = cu(network)
        .tokenAddressesByUniPairAddress(lpHash);
    String otherTokenAddress;
    if (lpTokenAdr.component1().equalsIgnoreCase(similarAdr)) {
      otherTokenAddress = lpTokenAdr.component2();
    } else if (lpTokenAdr.component2().equalsIgnoreCase(similarAdr)) {
      otherTokenAddress = lpTokenAdr.component1();
    } else {
      throw new IllegalStateException("Not found token in lp pair");
    }
    price *= getPriceForCoin(otherTokenAddress, block, network);
    log.info("Price {} fetched {} on block {}", tokenName, price, block);
    return price;
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
  
  private ContractUtils cu(String _network) {
    return ContractUtils.getInstance(_network);
  }

}
