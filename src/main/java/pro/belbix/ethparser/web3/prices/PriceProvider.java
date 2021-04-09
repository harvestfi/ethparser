package pro.belbix.ethparser.web3.prices;

import static java.util.Objects.requireNonNullElse;
import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ORACLE_START_BLOCK;
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
    PriceDTO priceDTO = silentCall(() -> priceRepository.fetchLastPrice(lpName, block, limitOne))
        .filter(Caller::isFilledList)
        .map(l -> l.get(0))
        .orElse(null);
    if (priceDTO == null) {
      log.warn("Saved price not found for " + lpName + " at block " + block);
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

    if (block > ORACLE_START_BLOCK) {
      return amount * priceOracle.getPriceForCoinOnChain(lpAddress, block);
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
  public Double getPriceForCoin(final String coinName, long block, String network) {
    if (ZERO_ADDRESS.equalsIgnoreCase(coinName)) {
      return 0.0;
    }
    String nameOrAddress = coinName;
    if (coinName.startsWith("0x")) {
      nameOrAddress = cu(network).getNameByAddress(coinName)
          .orElseThrow(() -> new IllegalStateException("Not found name for " + coinName));
    }
    String coinNameSimple = cu(network).getSimilarActiveForPrice(nameOrAddress);
    updateUSDPrice(coinNameSimple, block, network);
    if (cu(network).isStableCoin(coinNameSimple) && !priceOracle
        .isAvailable(nameOrAddress, block)) {
      return 1.0;
    }
    return getLastPrice(coinNameSimple, block);
  }

  public Tuple2<Double, Double> getPairPriceForStrategyHash(
      String vaultAddress, Long block, String network) {
    return getPairPriceForLpHash(
        cu(network).getVaultUnderlying(vaultAddress)
            .orElseThrow(() -> new IllegalStateException(
                "Can't fetch underlying token for " + vaultAddress)),
        block, network);
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

  private void updateUSDPrice(String coinName, long block, String network) {
    if (!cu(network).isTokenCreated(coinName, block)) {
      savePrice(0.0, coinName, block);
      return;
    }
    if (cu(network).isStableCoin(coinName) && !priceOracle
        .isAvailable(coinName, block)) {
      return;
    }

    if (hasFreshPrice(coinName, block)) {
      return;
    }
    double price = getPriceForCoinWithoutCache(coinName, block, network);

    savePrice(price, coinName, block);
  }

  private double getPriceForCoinWithoutCache(String name, Long block, String network) {
    String lpName = cu(network).findUniPairNameForTokenName(name, block)
        .orElse(null);
    PriceDTO priceDTO = silentCall(() -> priceRepository.fetchLastPrice(lpName, block, limitOne))
        .filter(Caller::isFilledList)
        .map(l -> l.get(0))
        .orElse(null);
    if (priceDTO == null) {
      log.info("Saved price not found for " + name + " at block " + block);
      return getPriceForCoinFromEth(name, block, network);
    }
    if (block - priceDTO.getBlock() > 1000) {
      log.warn("Price have not updated more then {} for {}", block - priceDTO.getBlock(), name);
      return getPriceForCoinFromEth(name, block, network);
    }
    if (!priceDTO.getToken().equalsIgnoreCase(name)
        && !priceDTO.getOtherToken().equalsIgnoreCase(name)) {
      throw new IllegalStateException("Wrong source for " + name);
    }

    double otherTokenPrice = getPriceForCoin(priceDTO.getOtherToken(), block, network);
    return priceDTO.getPrice() * otherTokenPrice;
  }

  private double getPriceForCoinFromEth(String name, Long block, String network) {
    if (appProperties.isOnlyApi()) {
      return 0.0;
    }
    String tokenAdr = cu(network).getAddressByName(name, ContractType.TOKEN)
        .orElseThrow(() -> new IllegalStateException("Not found address for " + name));
    if (block > ORACLE_START_BLOCK) {
      return priceOracle.getPriceForCoinOnChain(tokenAdr, block);
    }
    String lpName = cu(network).findUniPairNameForTokenName(name, block)
        .orElse(null);
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
    if (cu(network).isDivisionSequenceSecondDividesFirst(lpHash, tokenAdr)) {
      price = reserves.component2() / reserves.component1();
    } else {
      price = reserves.component1() / reserves.component2();
    }

    Tuple2<String, String> lpTokenAdr = cu(network)
        .tokenAddressesByUniPairAddress(lpHash);
    String otherTokenName;
    if (lpTokenAdr.component1().equalsIgnoreCase(tokenAdr)) {
      otherTokenName = cu(network).getNameByAddress(lpTokenAdr.component2())
          .orElseThrow(
              () -> new IllegalStateException("Not found name for " + lpTokenAdr.component2()));
    } else if (lpTokenAdr.component2().equalsIgnoreCase(tokenAdr)) {
      otherTokenName = cu(network).getNameByAddress(lpTokenAdr.component1())
          .orElseThrow(
              () -> new IllegalStateException("Not found name for " + lpTokenAdr.component1()));
    } else {
      throw new IllegalStateException("Not found token in lp pair");
    }
    price *= getPriceForCoin(otherTokenName, block, network);
    log.info("Price {} fetched {} on block {}", name, price, block);
    return price;
  }

  private boolean hasFreshPrice(String name, long block) {
    TreeMap<Long, Double> lastPriceByBlock = lastPrices.get(name);
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

  private double getLastPrice(String name, long block) {
    TreeMap<Long, Double> lastPriceByBlocks = lastPrices.get(name);
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
