package pro.belbix.ethparser.web3.harvest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Vaults {

    public final static String WETH = "0xFE09e53A81Fe2808bc493ea64319109B5bAa573e".toLowerCase();
    public final static String DAI = "0xab7fa2b2985bccfc13c6d86b1d5a17486ab1e04c".toLowerCase();
    public final static String USDC = "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE".toLowerCase();
    public final static String USDT = "0x053c80eA73Dc6941F518a68E2FC52Ac45BDE7c9C".toLowerCase();
    public final static String TUSD = "0x7674622c63bee7f46e86a4a5a18976693d54441b".toLowerCase();
    public final static String WBTC = "0x5d9d25c7C457dD82fc8668FFC6B9746b674d4EcB".toLowerCase();
    public final static String RENBTC = "0xC391d1b08c1403313B0c28D47202DFDA015633C4".toLowerCase();
    public final static String CRVRENBTC = "0x9aA8F427A17d6B0d91B6262989EdC7D45d6aEdf8".toLowerCase();
    public final static String WETH_DAI_LP = "0x307E2752e8b8a9C29005001Be66B1c012CA9CDB7".toLowerCase();
    public final static String WETH_USDC_LP = "0xA79a083FDD87F73c2f983c5551EC974685D6bb36".toLowerCase();
    public final static String WETH_USDT_LP = "0x7DDc3ffF0612E75Ea5ddC0d6Bd4e268f70362Cff".toLowerCase();
    public final static String WETH_WBTC_LP = "0x01112a60f427205dcA6E229425306923c3Cc2073".toLowerCase();
    public final static String WBTC_TBTC = "0xF553E1f826f42716cDFe02bde5ee76b2a52fc7EB".toLowerCase();
    public final static Map<String, String> vaultNames = new LinkedHashMap<>();
    public final static Map<String, Double> vaultDividers = new LinkedHashMap<>();

    static {
        vaultNames.put(WETH , "WETH");
        vaultNames.put(DAI , "DAI");
        vaultNames.put(USDC , "USDC");
        vaultNames.put(USDT , "USDT");
        vaultNames.put(TUSD , "TUSD");
        vaultNames.put(WBTC , "WBTC");
        vaultNames.put(RENBTC , "RENBTC");
        vaultNames.put(CRVRENBTC , "CRVRENBTC");
        vaultNames.put(WETH_DAI_LP , "WETH_DAI_LP");
        vaultNames.put(WETH_USDC_LP , "WETH_USDC_LP");
        vaultNames.put(WETH_USDT_LP , "WETH_USDT_LP");
        vaultNames.put(WETH_WBTC_LP , "WETH_WBTC_LP");
        vaultNames.put(WBTC_TBTC , "WBTC_TBTC");



        vaultDividers.put(WETH , 1000_000_000_000_000_000.0);
        vaultDividers.put(DAI , 1000_000_000_000_000_000.0);
        vaultDividers.put(USDC , 100_000_000.0);
        vaultDividers.put(USDT , 100_000_000.0);
        vaultDividers.put(TUSD , 1000_000_000_000_000_000.0);
        vaultDividers.put(WBTC , 100_000_000.0);
        vaultDividers.put(RENBTC , 100_000_000.0);
        vaultDividers.put(CRVRENBTC , 1000_000_000_000_000_000.0);
        vaultDividers.put(WETH_DAI_LP , 1000_000_000_000_000_000.0);
        vaultDividers.put(WETH_USDC_LP , 1000_000_000_000_000_000.0);
        vaultDividers.put(WETH_USDT_LP , 1000_000_000_000_000_000.0);
        vaultDividers.put(WETH_WBTC_LP , 1000_000_000_000_000_000.0);
        vaultDividers.put(WBTC_TBTC , 1000_000_000_000_000_000.0);
    }

}
