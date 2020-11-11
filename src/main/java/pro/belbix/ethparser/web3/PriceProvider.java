package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_USDC_ETH;
import static pro.belbix.ethparser.web3.uniswap.LpContracts.UNI_LP_USDC_WBTC;

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
import pro.belbix.ethparser.dto.PricesDTO;
import pro.belbix.ethparser.web3.uniswap.LpContracts;

@Service
public class PriceProvider {

    private static final Logger log = LoggerFactory.getLogger(PriceProvider.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private int updateTimeout = 60;
    private final Functions functions;

    private final Map<String, Double> lastPrices = new HashMap<>();
    private final Map<String, Instant> lastUpdates = new HashMap<>();

    public PriceProvider(Functions functions) {
        init();
        this.functions = functions;
    }

    public void setUpdateTimeout(int updateTimeout) {
        this.updateTimeout = updateTimeout;
    }

    public String getAllPrices(long block) throws JsonProcessingException {
        PricesDTO dto = new PricesDTO();
        dto.setBtc(getPriceForCoin("WBTC", block));
        dto.setEth(getPriceForCoin("WETH", block));
        return objectMapper.writeValueAsString(dto);
    }

    public Double getPriceForCoin(String name, long block) {
        String coinNameSimple = simplifyName(name);
        updateUSDCPrice(coinNameSimple, block);
        return lastPrices.get(coinNameSimple);
    }

    public Tuple2<Double, Double> getPriceForUniPair(String strategyHash, long block) {
        Tuple2<String, String> names = LpContracts.lpHashToCoinNames.get(
            LpContracts.harvestStrategyToLp.get(strategyHash));
        if (names == null) {
            throw new IllegalStateException("Not found names for " + strategyHash);
        }
        return new Tuple2<>(
            getPriceForCoin(names.component1(), block),
            getPriceForCoin(names.component2(), block)
        );
    }

    private void updateUSDCPrice(String coinName, long block) {
        if (isStableCoin(coinName)) {
            return;
        }

        Instant lastUpdate = lastUpdates.get(coinName);
        if (lastUpdate != null && updateTimeout != 0
            && Duration.between(lastUpdate, Instant.now()).getSeconds() < updateTimeout) {
            return;
        }

        String lpName = "UNI_LP_USDC_" + coinName;
        String lpHash = LpContracts.lpNameToHash.get(lpName);
        if (lpHash == null) {
            throw new IllegalStateException("Not found hash for " + lpName);
        }

        Tuple2<Double, Double> reserves = functions.callReserves(lpHash, block);
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
        log.info("Price {} updated {} on block {}", coinName, price, block);
    }

    private boolean isStableCoin(String name) {
        return "USD".equals(name)
            || "USDC".equals(name)
            || "USDT".equals(name)
            || "YCRV".equals(name)
            || "3CRV".equals(name)
            || "TUSD".equals(name)
            || "DAI".equals(name);
    }

    private String simplifyName(String name) {
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
        }
        return name;
    }

    private void init() {
        lastPrices.put("ETH", 382.0);
        lastPrices.put("WETH", 382.0);

        lastPrices.put("USDC", 1.0);
        lastPrices.put("USDT", 1.0);
        lastPrices.put("DAI", 1.0);
        lastPrices.put("YCRV", 1.0);
        lastPrices.put("3CRV", 1.0);
        lastPrices.put("TUSD", 1.0);

        lastPrices.put("WBTC", 13673.0);
        lastPrices.put("RENBTC", 13673.0);
        lastPrices.put("CRVRENWBTC", 13673.0);
        lastPrices.put("TBTC", 13673.0);
    }

}
