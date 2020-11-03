package pro.belbix.ethparser;

import java.util.HashMap;
import java.util.Map;
import org.web3j.tuples.generated.Tuple2;

public class PriceProvider {

    private final Map<String, Double> lastPrices = new HashMap<>();

    public PriceProvider() {
        init();
    }

    public Double getPriceForCoin(String name) {
        return lastPrices.get(name);
    }

    public Tuple2<Double, Double> getPriceForUniPair(String name) {
        String[] names = name.split("_");
        return new Tuple2<>(
            lastPrices.get(names[1]),
            lastPrices.get(names[2])
        );
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
