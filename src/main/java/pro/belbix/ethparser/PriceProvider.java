package pro.belbix.ethparser;

import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_USDC_ETH;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_USDC_WBTC;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.uniswap.LpContracts;

@Service
public class PriceProvider {

    private static final int UPDATE_TIMEOUT = 60 * 10;
    private final Web3Service web3Service;
    private final Functions functions;

    private final Map<String, Double> lastPrices = new HashMap<>();
    private final Map<String, Instant> lastUpdates = new HashMap<>();

    public PriceProvider(Web3Service web3Service, Functions functions) {
        init();
        this.web3Service = web3Service;
        this.functions = functions;
    }

    public Double getPriceForCoin(String name) {
        updateUSDCPrice(name);
        return lastPrices.get(name);
    }

    public Tuple2<Double, Double> getPriceForUniPair(String strategyHash) {
        Tuple2<String, String> names = LpContracts.lpHashToCoinNames.get(
            LpContracts.harvestStrategyToLp.get(strategyHash));
        if (names == null) {
            throw new IllegalStateException("Not found names for " + strategyHash);
        }
        return new Tuple2<>(
            getPriceForCoin(names.component1()),
            getPriceForCoin(names.component2())
        );
    }

    private void updateUSDCPrice(String coinName) {
        if (isStableCoin(coinName)) {
            return;
        }
        coinName = simplifyName(coinName);

        Instant lastUpdate = lastUpdates.get(coinName);
        if (lastUpdate != null && Duration.between(lastUpdate, Instant.now()).getSeconds() < UPDATE_TIMEOUT) {
            return;
        }

        String lpName = "UNI_LP_USDC_" + coinName;
        String lpHash = LpContracts.lpNameToHash.get(lpName);
        if (lpHash == null) {
            throw new IllegalStateException("Not found hash for " + lpName);
        }

        Tuple2<Double, Double> reserves = functions.callReserves(lpHash);
        double price;
        if (UNI_LP_USDC_ETH.equals(lpHash)) {
            price = reserves.component1() / reserves.component2();
        } else if (UNI_LP_USDC_WBTC.equals(lpHash)) {
            price = reserves.component2() / reserves.component1();
        } else {
            throw new IllegalStateException("Unknown LP");
        }

        lastPrices.put(coinName, price);
        lastUpdates.put(coinName, Instant.now());
    }

    private boolean isStableCoin(String name) {
        return "USD".equals(name)
            || "USDC".equals(name)
            || "USDT".equals(name)
            || "DAI".equals(name);
    }

    private String simplifyName(String name) {
        if ("WETH".equals(name)) {
            return "ETH";
        } else if ("RENBTC".equals(name)) {
            return "WBTC";
        } else if ("CRVRENWBTC".equals(name)) {
            return "WBTC";
        } else if ("TBTC".equals(name)) {
            return "WBTC";
        }
        return name;
    }

    private void init() {
        lastPrices.put("ETH", 382.0);
        lastPrices.put("WETH", 382.0);

        lastPrices.put("USDC", 1.0);
        lastPrices.put("USDT", 1.0);
        lastPrices.put("DAI", 1.0);

        lastPrices.put("WBTC", 13673.0);
        lastPrices.put("RENBTC", 13673.0);
        lastPrices.put("CRVRENWBTC", 13673.0);
        lastPrices.put("TBTC", 13673.0);
    }

}
