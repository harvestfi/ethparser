package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.harvest.Vaults.SUSHI_WBTC_TBTC;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.Vaults.UNI_ETH_WBTC;

import java.util.LinkedHashMap;
import java.util.Map;

public class LpContracts {

    public static final String UNI_LP_ETH_DAI = "0xA478c2975Ab1Ea89e8196811F51A7B7Ade33eB11".toLowerCase();
    public static final String UNI_LP_ETH_USDC = "0xB4e16d0168e52d35CaCD2c6185b44281Ec28C9Dc".toLowerCase();
    public static final String UNI_LP_ETH_USDT = "0x0d4a11d5EEaaC28EC3F61d100daF4d40471f1852".toLowerCase();
    public static final String UNI_LP_ETH_WBTC = "0xBb2b8038a1640196FbE3e38816F3e67Cba72D940".toLowerCase();
    public static final String SUSHI_LP_WBTC_TBTC = "0x2Dbc7dD86C6cd87b525BD54Ea73EBeeBbc307F68".toLowerCase();
    public static final Map<String, String> harvestStrategyToLp = new LinkedHashMap<>();

    static {
        harvestStrategyToLp.put(UNI_ETH_DAI, UNI_LP_ETH_DAI);
        harvestStrategyToLp.put(UNI_ETH_USDC, UNI_LP_ETH_USDC);
        harvestStrategyToLp.put(UNI_ETH_USDT, UNI_LP_ETH_USDT);
        harvestStrategyToLp.put(UNI_ETH_WBTC, UNI_LP_ETH_WBTC);
        harvestStrategyToLp.put(SUSHI_WBTC_TBTC, SUSHI_LP_WBTC_TBTC);
    }

}
