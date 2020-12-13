package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.model.HarvestTx.parseAmount;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_ETH_DPI;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_GRAIN_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_USDC_ETH;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_USDC_WBTC;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_WBTC_BADGER;
import static pro.belbix.ethparser.web3.uniswap.contracts.Tokens.DPI_NAME;
import static pro.belbix.ethparser.web3.uniswap.contracts.Tokens.FARM_NAME;
import static pro.belbix.ethparser.web3.uniswap.contracts.Tokens.GRAIN_NAME;
import static pro.belbix.ethparser.web3.uniswap.contracts.Tokens.WETH_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.model.PricesModel;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
public class PriceProvider {

    private static final Logger log = LoggerFactory.getLogger(PriceProvider.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private int updateTimeout = 60;

    private final Map<String, Double> lastPrices = new HashMap<>();
    private final Map<String, Instant> lastUpdates = new HashMap<>();

    private final Functions functions;

    public PriceProvider(Functions functions) {
        init();
        this.functions = functions;
    }

    public void setUpdateTimeout(int updateTimeout) {
        this.updateTimeout = updateTimeout;
    }

    public String getAllPrices(long block) throws JsonProcessingException {
        PricesModel dto = new PricesModel();
        dto.setBtc(getPriceForCoin("WBTC", block));
        dto.setEth(getPriceForCoin("WETH", block));
        dto.setDpi(getPriceForCoin("DPI", block));
        dto.setGrain(getPriceForCoin("GRAIN", block));
        return objectMapper.writeValueAsString(dto);
    }

    public double getLpPositionAmountInUsd(String lpAddress, double amount, Long block) {
        Tuple2<String, String> names = LpContracts.lpHashToCoinNames.get(lpAddress);
        if (names == null) {
            throw new IllegalStateException("Not found names for " + lpAddress);
        }
        Tuple2<Double, Double> usdPrices = new Tuple2<>(
            getPriceForCoin(names.component1(), block),
            getPriceForCoin(names.component2(), block)
        );
        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpAddress, block);
        double lpBalance = parseAmount(functions.callErc20TotalSupply(lpAddress, block), lpAddress);
        double positionFraction = amount / lpBalance;

        double firstCoin = positionFraction * lpUnderlyingBalances.component1();
        double secondCoin = positionFraction * lpUnderlyingBalances.component2();

        double firstVaultUsdAmount = firstCoin * usdPrices.component1();
        double secondVaultUsdAmount = secondCoin * usdPrices.component2();
        return firstVaultUsdAmount + secondVaultUsdAmount;
    }

    public Double getPriceForCoin(String name, Long block) {
        String coinNameSimple = simplifyName(name);
        updateUSDCPrice(coinNameSimple, block);
        if (isStableCoin(coinNameSimple)) {
            return 1.0;
        }
        return lastPrices.get(coinNameSimple);
    }

    public Tuple2<Double, Double> getPairPriceForStrategyHash(String strategyHash, Long block) {
        return getPairPriceForLpHash(LpContracts.harvestStrategyToLp.get(strategyHash), block);
    }

    public Tuple2<Double, Double> getPairPriceForLpHash(String lpHash, Long block) {
        Tuple2<String, String> names = LpContracts.lpHashToCoinNames.get(lpHash);
        if (names == null) {
            throw new IllegalStateException("Not found names for " + lpHash);
        }
        return new Tuple2<>(
            getPriceForCoin(names.component1(), block),
            getPriceForCoin(names.component2(), block)
        );
    }

    private void updateUSDCPrice(String coinName, Long block) {
        if (isStableCoin(coinName)) {
            return;
        }

        Instant lastUpdate = lastUpdates.get(coinName);
        if (lastUpdate != null && updateTimeout != 0
            && Duration.between(lastUpdate, Instant.now()).getSeconds() < updateTimeout) {
            return;
        }

        String lpName;
        String nonUSD = null;
        if (DPI_NAME.equals(coinName)) {
            lpName = "UNI_LP_ETH_DPI";
            nonUSD = WETH_NAME;
        } else if (GRAIN_NAME.equals(coinName)) {
            lpName = "UNI_LP_GRAIN_FARM";
            nonUSD = FARM_NAME;
        } else if ("BADGER".equals(coinName)) {
            lpName = "UNI_LP_WBTC_BADGER";
            nonUSD = "WBTC";
        } else {
            lpName = "UNI_LP_USDC_" + coinName;
        }

        String lpHash = LpContracts.lpNameToHash.get(lpName);
        if (lpHash == null) {
            throw new IllegalStateException("Not found hash for " + lpName);
        }

        Tuple2<Double, Double> reserves = functions.callReserves(lpHash, block);
        double price;
        if (
            UNI_LP_USDC_ETH.equals(lpHash)
                || UNI_LP_WBTC_BADGER.equals(lpHash)
        ) { //todo create method based on key token
            price = reserves.component1() / reserves.component2();
        } else if (UNI_LP_USDC_WBTC.equals(lpHash)
            || UNI_LP_USDC_FARM.equals(lpHash)
            || UNI_LP_ETH_DPI.equals(lpHash)
            || UNI_LP_GRAIN_FARM.equals(lpHash)
        ) {
            price = reserves.component2() / reserves.component1();
        } else {
            throw new IllegalStateException("Unknown LP " + lpHash);
        }

        if (nonUSD != null) {
            price *= getPriceForCoin(nonUSD, block);
        }

        lastPrices.put(coinName, price);
        lastUpdates.put(coinName, Instant.now());
        log.info("Price {} updated {} on block {}", coinName, price, block);
    }

    private static boolean isStableCoin(String name) {
        return "USD".equals(name)
            || "USDC".equals(name)
            || "USDT".equals(name)
            || "YCRV".equals(name)
            || "3CRV".equals(name)
            || "_3CRV".equals(name)
            || "TUSD".equals(name)
            || "DAI".equals(name)
            || "CRV_CMPND".equals(name)
            || "CRV_BUSD".equals(name)
            || "CRV_USDN".equals(name)
            || "HUSD".equals(name)
            || "CRV_HUSD".equals(name)
            ;
    }

    private static String simplifyName(String name) {
        name = name.replaceFirst("_V0", "");
        if ("WETH".equals(name)) {
            return "ETH";
        } else if ("RENBTC".equals(name)) {
            return "WBTC";
        } else if ("CRVRENWBTC".equals(name)) {
            return "WBTC";
        } else if ("TBTC".equals(name)) {
            return "WBTC";
        } else if ("BTC".equals(name)) {
            return "WBTC";
        } else if ("CRV_TBTC".equals(name)) {
            return "WBTC";
        } else if ("HBTC".equals(name)) {
            return "WBTC";
        } else if ("CRV_HBTC".equals(name)) {
            return "WBTC";
        } else if ("PS".equals(name)) {
            return "FARM";
        }
        return name;
    }

    private void init() {
        lastPrices.put("ETH", 382.0);
        lastPrices.put("WETH", 382.0);

        lastPrices.put("WBTC", 13673.0);
        lastPrices.put("RENBTC", 13673.0);
        lastPrices.put("CRVRENWBTC", 13673.0);
        lastPrices.put("TBTC", 13673.0);
    }

    public static double readPrice(PricesModel pricesModel, String coinName) {
        coinName = simplifyName(coinName);
        if (isStableCoin(coinName)) {
            return 1.0;
        }
        if ("WBTC".equals(coinName)) {
            return pricesModel.getBtc();
        } else if ("ETH".equals(coinName)) {
            return pricesModel.getEth();
        } else if ("DPI".equals(coinName)) {
            return pricesModel.getDpi();
        } else if ("FARM".equals(coinName)) {
            return 0;
        } else {
            log.warn("Not found price for {}", coinName);
            return 0;
        }
    }

}
