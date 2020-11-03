package pro.belbix.ethparser.web3.harvest;

import java.util.LinkedHashMap;
import java.util.Map;

public class Vaults {




//    public static final String WETH_V0 = "0x8e298734681adbfC41ee5d17FF8B0d6d803e7098".toLowerCase();
//    public static final String USDC_V0 = "0xc3F7ffb5d5869B3ade9448D094d81B0521e8326f".toLowerCase();
//    public static final String USDT_V0 = "0xc7EE21406BB581e741FBb8B21f213188433D9f2F".toLowerCase();
//    public static final String TUSD = "0x7674622c63Bee7F46E86a4A5A18976693D54441b".toLowerCase();
//    public static final String DAI_V0 = "0xe85C8581e60D7Cd32Bbfd86303d2A4FA6a951Dac".toLowerCase();
//    public static final String WBTC_V0 = "0xc07EB91961662D275E2D285BdC21885A4Db136B0".toLowerCase();
//    public static final String RENBTC_V0 = "0xfBe122D0ba3c75e1F7C80bd27613c9f35B81FEeC".toLowerCase();
//    public static final String CRVRENWBTC_V0 = "0x192E9d29D43db385063799BC239E772c3b6888F3".toLowerCase();
//    public static final String UNI_ETH_DAI_V0 = "0x1a9F22b4C385f78650E7874d64e442839Dc32327".toLowerCase();
//    public static final String UNI_ETH_USDC_V0 = "0x63671425ef4D25Ec2b12C7d05DE855C143f16e3B".toLowerCase();
//    public static final String UNI_ETH_USDT_V0 = "0xB19EbFB37A936cCe783142955D39Ca70Aa29D43c".toLowerCase();
//    public static final String UNI_ETH_WBTC_V0 = "0xb1FeB6ab4EF7d0f41363Da33868e85EB0f3A57EE".toLowerCase();
    public static final String UNI_ETH_DAI = "0x307E2752e8b8a9C29005001Be66B1c012CA9CDB7".toLowerCase();
    public static final String UNI_ETH_USDC = "0xA79a083FDD87F73c2f983c5551EC974685D6bb36".toLowerCase();
    public static final String UNI_ETH_USDT = "0x7DDc3ffF0612E75Ea5ddC0d6Bd4e268f70362Cff".toLowerCase();
    public static final String UNI_ETH_WBTC = "0x01112a60f427205dcA6E229425306923c3Cc2073".toLowerCase();
    public static final String WETH = "0xFE09e53A81Fe2808bc493ea64319109B5bAa573e".toLowerCase();
    public static final String USDC = "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE".toLowerCase();
    public static final String USDT = "0x053c80eA73Dc6941F518a68E2FC52Ac45BDE7c9C".toLowerCase();
    public static final String DAI = "0xab7FA2B2985BCcfC13c6D86b1D5A17486ab1e04C".toLowerCase();
    public static final String WBTC = "0x5d9d25c7C457dD82fc8668FFC6B9746b674d4EcB".toLowerCase();
    public static final String RENBTC = "0xC391d1b08c1403313B0c28D47202DFDA015633C4".toLowerCase();
    public static final String CRVRENWBTC = "0x9aA8F427A17d6B0d91B6262989EdC7D45d6aEdf8".toLowerCase();
    public static final String SUSHI_WBTC_TBTC = "0xF553E1f826f42716cDFe02bde5ee76b2a52fc7EB".toLowerCase();

    public final static Map<String, String> vaultNames = new LinkedHashMap<>();
    public final static Map<String, Double> vaultDividers = new LinkedHashMap<>();

    static {
//        vaultNames.put(WETH_V0, "WETH_V0");
//        vaultNames.put(USDC_V0, "USDC_V0");
//        vaultNames.put(USDT_V0, "USDT_V0");
//        vaultNames.put(TUSD, "TUSD");
//        vaultNames.put(DAI_V0, "DAI_V0");
//        vaultNames.put(WBTC_V0, "WBTC_V0");
//        vaultNames.put(RENBTC_V0, "RENBTC_V0");
//        vaultNames.put(CRVRENWBTC_V0, "CRVRENWBTC_V0");
//        vaultNames.put(UNI_ETH_DAI_V0, "UNI_ETH_DAI_V0");
//        vaultNames.put(UNI_ETH_USDC_V0, "UNI_ETH_USDC_V0");
//        vaultNames.put(UNI_ETH_USDT_V0, "UNI_ETH_USDT_V0");
//        vaultNames.put(UNI_ETH_WBTC_V0, "UNI_ETH_WBTC_V0");
        vaultNames.put(UNI_ETH_DAI, "UNI_ETH_DAI");
        vaultNames.put(UNI_ETH_USDC, "UNI_ETH_USDC");
        vaultNames.put(UNI_ETH_USDT, "UNI_ETH_USDT");
        vaultNames.put(UNI_ETH_WBTC, "UNI_ETH_WBTC");
        vaultNames.put(WETH, "WETH");
        vaultNames.put(USDC, "USDC");
        vaultNames.put(USDT, "USDT");
        vaultNames.put(DAI, "DAI");
        vaultNames.put(WBTC, "WBTC");
        vaultNames.put(RENBTC, "RENBTC");
        vaultNames.put(CRVRENWBTC, "CRVRENWBTC");
        vaultNames.put(SUSHI_WBTC_TBTC, "SUSHI_WBTC_TBTC");

//        vaultDividers.put(WETH_V0, 1000_000_000_000_000_000.0);
//        vaultDividers.put(USDC_V0, 1000_000.0);
//        vaultDividers.put(USDT_V0, 1000_000.0);
//        vaultDividers.put(TUSD, 1000_000_000_000_000_000.0);
//        vaultDividers.put(DAI_V0, 1000_000_000_000_000_000.0);
//        vaultDividers.put(WBTC_V0, 100_000_000.0);
//        vaultDividers.put(RENBTC_V0, 100_000_000.0);
//        vaultDividers.put(CRVRENWBTC_V0, 1000_000_000_000_000_000.0);
//        vaultDividers.put(UNI_ETH_DAI_V0, 1000_000_000_000_000_000.0);
//        vaultDividers.put(UNI_ETH_USDC_V0, 1000_000_000_000_000_000.0);
//        vaultDividers.put(UNI_ETH_USDT_V0, 1000_000_000_000_000_000.0);
//        vaultDividers.put(UNI_ETH_WBTC_V0, 1000_000_000_000_000_000.0);
        vaultDividers.put(UNI_ETH_DAI, 1000_000_000_000_000_000.0);
        vaultDividers.put(UNI_ETH_USDC, 1000_000_000_000_000_000.0);
        vaultDividers.put(UNI_ETH_USDT, 1000_000_000_000_000_000.0);
        vaultDividers.put(UNI_ETH_WBTC, 1000_000_000_000_000_000.0);
        vaultDividers.put(WETH, 1000_000_000_000_000_000.0);
        vaultDividers.put(USDC, 1000_000.0);
        vaultDividers.put(USDT, 1000_000.0);
        vaultDividers.put(DAI, 1000_000_000_000_000_000.0);
        vaultDividers.put(WBTC, 100_000_000.0);
        vaultDividers.put(RENBTC, 100_000_000.0);
        vaultDividers.put(CRVRENWBTC, 1000_000_000_000_000_000.0);
        vaultDividers.put(SUSHI_WBTC_TBTC, 1000_000_000_000_000_000.0);
    }

}
