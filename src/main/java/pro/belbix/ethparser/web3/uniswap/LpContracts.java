package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.ContractMapper.D18;
import static pro.belbix.ethparser.web3.ContractMapper.D6;
import static pro.belbix.ethparser.web3.ContractMapper.D8;
import static pro.belbix.ethparser.web3.harvest.Vaults.SUSHI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.Vaults.SUSHI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.Vaults.SUSHI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.Vaults.SUSHI_ETH_WBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.SUSHI_WBTC_TBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_DAI_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDC_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDT_V0;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_WBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_WBTC_V0;

import java.util.LinkedHashMap;
import java.util.Map;
import org.web3j.tuples.generated.Tuple2;

public class LpContracts {

    public static final String UNI_LP_ETH_DAI = "0xA478c2975Ab1Ea89e8196811F51A7B7Ade33eB11".toLowerCase();
    public static final String UNI_LP_ETH_USDC = "0xB4e16d0168e52d35CaCD2c6185b44281Ec28C9Dc".toLowerCase();
    public static final String UNI_LP_ETH_USDT = "0x0d4a11d5EEaaC28EC3F61d100daF4d40471f1852".toLowerCase();
    public static final String UNI_LP_ETH_WBTC = "0xBb2b8038a1640196FbE3e38816F3e67Cba72D940".toLowerCase();
    public static final String SUSHI_LP_WBTC_TBTC = "0x2Dbc7dD86C6cd87b525BD54Ea73EBeeBbc307F68".toLowerCase();
    public static final String UNI_LP_USDC_ETH = "0xB4e16d0168e52d35CaCD2c6185b44281Ec28C9Dc".toLowerCase();
    public static final String UNI_LP_USDC_WBTC = "0x004375dff511095cc5a197a54140a24efef3a416".toLowerCase();
    public static final String UNI_LP_USDC_FARM = "0x514906fc121c7878424a5c928cad1852cc545892".toLowerCase();
    public static final String SUSHI_LP_ETH_DAI = "0xc3d03e4f041fd4cd388c549ee2a29a9e5075882f".toLowerCase();
    public static final String SUSHI_LP_ETH_USDC = "0x397ff1542f962076d0bfe58ea045ffa2d347aca0".toLowerCase();
    public static final String SUSHI_LP_ETH_USDT = "0x06da0fd433c1a5d7a4faa01111c044910a184553".toLowerCase();
    public static final String SUSHI_LP_ETH_WBTC = "0xceff51756c56ceffca006cd410b03ffc46dd3a58".toLowerCase();

    public static final Map<String, String> harvestStrategyToLp = new LinkedHashMap<>();
    public static final Map<String, String> lpNameToHash = new LinkedHashMap<>();
    public static final Map<String, Double> lpHashToDividers = new LinkedHashMap<>();
    public static final Map<String, Tuple2<String, String>> lpHashToCoinNames = new LinkedHashMap<>();
    public final static Map<String, Tuple2<Double, Double>> lpPairsDividers = new LinkedHashMap<>();

    static {
        harvestStrategyToLp.put(UNI_ETH_DAI, UNI_LP_ETH_DAI);
        harvestStrategyToLp.put(UNI_ETH_USDC, UNI_LP_ETH_USDC);
        harvestStrategyToLp.put(UNI_ETH_USDT, UNI_LP_ETH_USDT);
        harvestStrategyToLp.put(UNI_ETH_WBTC, UNI_LP_ETH_WBTC);
        harvestStrategyToLp.put(SUSHI_WBTC_TBTC, SUSHI_LP_WBTC_TBTC);
        harvestStrategyToLp.put(UNI_ETH_DAI_V0, UNI_LP_ETH_DAI);
        harvestStrategyToLp.put(UNI_ETH_USDC_V0, UNI_LP_ETH_USDC);
        harvestStrategyToLp.put(UNI_ETH_USDT_V0, UNI_LP_ETH_USDT);
        harvestStrategyToLp.put(UNI_ETH_WBTC_V0, UNI_LP_ETH_WBTC);
        harvestStrategyToLp.put(SUSHI_ETH_DAI, SUSHI_LP_ETH_DAI);
        harvestStrategyToLp.put(SUSHI_ETH_USDC, SUSHI_LP_ETH_USDC);
        harvestStrategyToLp.put(SUSHI_ETH_USDT, SUSHI_LP_ETH_USDT);
        harvestStrategyToLp.put(SUSHI_ETH_WBTC, SUSHI_LP_ETH_WBTC);

        lpHashToDividers.put(UNI_LP_ETH_DAI, D18);
        lpHashToDividers.put(UNI_LP_ETH_USDC, D18);
        lpHashToDividers.put(UNI_LP_ETH_USDT, D18);
        lpHashToDividers.put(UNI_LP_ETH_WBTC, D18);
        lpHashToDividers.put(SUSHI_LP_WBTC_TBTC, D18);
        lpHashToDividers.put(SUSHI_LP_ETH_DAI, D18);
        lpHashToDividers.put(SUSHI_LP_ETH_USDC, D18);
        lpHashToDividers.put(SUSHI_LP_ETH_USDT, D18);
        lpHashToDividers.put(SUSHI_LP_ETH_WBTC, D18);

        lpHashToCoinNames.put(UNI_LP_ETH_DAI, new Tuple2<>("DAI", "ETH"));
        lpHashToCoinNames.put(UNI_LP_ETH_USDC, new Tuple2<>("USDC", "ETH"));
        lpHashToCoinNames.put(UNI_LP_ETH_USDT, new Tuple2<>("ETH", "USDT"));
        lpHashToCoinNames.put(UNI_LP_ETH_WBTC, new Tuple2<>("WBTC", "ETH"));
        lpHashToCoinNames.put(SUSHI_LP_WBTC_TBTC, new Tuple2<>("WBTC", "TBTC"));
        lpHashToCoinNames.put(SUSHI_LP_ETH_DAI, new Tuple2<>("DAI", "ETH"));
        lpHashToCoinNames.put(SUSHI_LP_ETH_USDC, new Tuple2<>("USDC", "ETH"));
        lpHashToCoinNames.put(SUSHI_LP_ETH_USDT, new Tuple2<>("ETH", "USDT"));
        lpHashToCoinNames.put(SUSHI_LP_ETH_WBTC, new Tuple2<>("WBTC", "ETH"));

        lpNameToHash.put("UNI_LP_ETH_DAI", UNI_LP_ETH_DAI);
        lpNameToHash.put("UNI_LP_ETH_USDC", UNI_LP_ETH_USDC);
        lpNameToHash.put("UNI_LP_ETH_USDT", UNI_LP_ETH_USDT);
        lpNameToHash.put("UNI_LP_ETH_WBTC", UNI_LP_ETH_WBTC);
        lpNameToHash.put("SUSHI_LP_WBTC_TBTC", SUSHI_LP_WBTC_TBTC);
        lpNameToHash.put("UNI_LP_USDC_ETH", UNI_LP_USDC_ETH);
        lpNameToHash.put("UNI_LP_USDC_WBTC", UNI_LP_USDC_WBTC);
        lpNameToHash.put("UNI_ETH_DAI_V0", UNI_ETH_DAI_V0);
        lpNameToHash.put("UNI_ETH_USDC_V0", UNI_ETH_USDC_V0);
        lpNameToHash.put("UNI_ETH_USDT_V0", UNI_ETH_USDT_V0);
        lpNameToHash.put("UNI_ETH_WBTC_V0", UNI_ETH_WBTC_V0);
        lpNameToHash.put("UNI_LP_USDC_FARM", UNI_LP_USDC_FARM);

        lpPairsDividers.put(UNI_LP_ETH_DAI, new Tuple2<>(D18, D18));
        lpPairsDividers.put(UNI_LP_ETH_USDC, new Tuple2<>(D6, D18));
        lpPairsDividers.put(UNI_LP_ETH_USDT, new Tuple2<>(D18, D6));
        lpPairsDividers.put(UNI_LP_ETH_WBTC, new Tuple2<>(D8, D18));
        lpPairsDividers.put(SUSHI_LP_WBTC_TBTC, new Tuple2<>(D8, D18));
        lpPairsDividers.put(UNI_LP_USDC_ETH, new Tuple2<>(D6, D18));
        lpPairsDividers.put(UNI_LP_USDC_WBTC, new Tuple2<>(D8, D6));
        lpPairsDividers.put(UNI_LP_USDC_FARM, new Tuple2<>(D18, D6));
        lpPairsDividers.put(SUSHI_LP_ETH_DAI, new Tuple2<>(D18, D18));
        lpPairsDividers.put(SUSHI_LP_ETH_USDC, new Tuple2<>(D6, D18));
        lpPairsDividers.put(SUSHI_LP_ETH_USDT, new Tuple2<>(D18, D6));
        lpPairsDividers.put(SUSHI_LP_ETH_WBTC, new Tuple2<>(D8, D18));
    }

}
