package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;
import static pro.belbix.ethparser.web3.contracts.Tokens.BAC_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.BAC_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.BADGER_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.BADGER_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.BAS_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.BAS_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.BSGS_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.BSGS_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.BSG_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.BSG_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.DAI_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.DPI_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.DPI_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.DSD_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.DSD_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.ESD_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.ESD_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.EURS_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.EURS_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.FARM_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.GRAIN_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.GRAIN_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.IDX_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.MAAPL_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.MAAPL_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.MAMZN_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.MAMZN_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.MGOOGL_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.MGOOGL_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.MIC_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.MIC_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.MIS_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.MIS_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.MTSLA_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.MTSLA_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.SUSHI_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.SUSHI_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.TBTC_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.USDC_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.USDT_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.UST_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.WBTC_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.WBTC_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.WETH_NAME;
import static pro.belbix.ethparser.web3.contracts.Tokens.WETH_TOKEN;
import static pro.belbix.ethparser.web3.contracts.Tokens.findNameForContract;
import static pro.belbix.ethparser.web3.contracts.Tokens.simplifyName;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.web3j.tuples.generated.Tuple2;

public class LpContracts {

    public static final String UNI_LP_ETH_DAI = "0xA478c2975Ab1Ea89e8196811F51A7B7Ade33eB11".toLowerCase();
    public static final String UNI_LP_USDC_ETH = "0xB4e16d0168e52d35CaCD2c6185b44281Ec28C9Dc".toLowerCase();
    public static final String UNI_LP_ETH_USDT = "0x0d4a11d5EEaaC28EC3F61d100daF4d40471f1852".toLowerCase();
    public static final String UNI_LP_ETH_WBTC = "0xBb2b8038a1640196FbE3e38816F3e67Cba72D940".toLowerCase();
    public static final String SUSHI_LP_WBTC_TBTC = "0x2Dbc7dD86C6cd87b525BD54Ea73EBeeBbc307F68".toLowerCase();
    public static final String UNI_LP_USDC_WBTC = "0x004375dff511095cc5a197a54140a24efef3a416".toLowerCase();
    public static final String UNI_LP_USDC_FARM = "0x514906fc121c7878424a5c928cad1852cc545892".toLowerCase();
    public static final String UNI_LP_WETH_FARM = "0x56feaccb7f750b997b36a68625c7c596f0b41a58".toLowerCase();
    public static final String UNI_LP_IDX_ETH = "0x3452a7f30a712e415a0674c0341d44ee9d9786f9".toLowerCase();
    public static final String UNI_LP_USDC_IDX = "0xc372089019614e5791b08b5036f298d002a8cbef".toLowerCase();
    public static final String UNI_LP_ETH_DPI = "0x4d5ef58aac27d99935e5b6b4a6778ff292059991".toLowerCase();
    public static final String UNI_LP_WBTC_BADGER = "0xcd7989894bc033581532d2cd88da5db0a4b12859".toLowerCase();
    public static final String SUSHI_LP_ETH_DAI = "0xc3d03e4f041fd4cd388c549ee2a29a9e5075882f".toLowerCase();
    public static final String SUSHI_LP_ETH_USDC = "0x397ff1542f962076d0bfe58ea045ffa2d347aca0".toLowerCase();
    public static final String SUSHI_LP_ETH_USDT = "0x06da0fd433c1a5d7a4faa01111c044910a184553".toLowerCase();
    public static final String SUSHI_LP_ETH_WBTC = "0xceff51756c56ceffca006cd410b03ffc46dd3a58".toLowerCase();
    public static final String UNI_LP_GRAIN_FARM = "0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49".toLowerCase();
    public static final String UNI_LP_BAC_DAI = "0xd4405f0704621dbe9d4dea60e128e0c3b26bddbd".toLowerCase();
    public static final String UNI_LP_DAI_BAS = "0x0379da7a5895d13037b6937b109fa8607a659adf".toLowerCase();
    public static final String SUSHI_LP_MIC_USDT = "0xC9cB53B48A2f3A9e75982685644c1870F1405CCb".toLowerCase();
    public static final String SUSHI_LP_MIS_USDT = "0x066F3A3B7C8Fa077c71B9184d862ed0A4D5cF3e0".toLowerCase();
    public static final String ONEINCH_LP_ETH_DAI = "0x7566126f2fD0f2Dddae01Bb8A6EA49b760383D5A".toLowerCase();
    public static final String ONEINCH_LP_ETH_USDC = "0xb4dB55a20E0624eDD82A0Cf356e3488B4669BD27".toLowerCase();
    public static final String ONEINCH_LP_ETH_USDT = "0xbBa17b81aB4193455Be10741512d0E71520F43cB".toLowerCase();
    public static final String ONEINCH_LP_ETH_WBTC = "0x6a11F3E5a01D129e566d783A7b6E8862bFD66CcA".toLowerCase();
    public static final String UNI_LP_DAI_BSG = "0x4a9596e5d2f9bef50e4de092ad7181ae3c40353e".toLowerCase();
    public static final String UNI_LP_DAI_BSGS = "0x980a07e4f64d21a0cb2ef8d4af362a79b9f5c0da".toLowerCase();
    public static final String UNI_LP_ESD_USDC = "0x88ff79eb2bc5850f27315415da8685282c7610f9".toLowerCase();
    public static final String UNI_LP_USDC_DSD = "0x66e33d2605c5fb25ebb7cd7528e7997b0afa55e8".toLowerCase();
    public static final String UNI_LP_MAAPL_UST = "0xb022e08adc8ba2de6ba4fecb59c6d502f66e953b".toLowerCase();
    public static final String UNI_LP_MAMZN_UST = "0x0Ae8cB1f57e3b1b7f4f5048743710084AA69E796".toLowerCase();
    public static final String UNI_LP_MGOOGL_UST = "0x4b70ccD1Cf9905BE1FaEd025EADbD3Ab124efe9a".toLowerCase();
    public static final String UNI_LP_MTSLA_UST = "0x5233349957586A8207c52693A959483F9aeAA50C".toLowerCase();
    public static final String UNI_LP_USDC_EURS = "0x767055e2a9f15783b1ec5ef134a89acf3165332f".toLowerCase();
    public static final String SUSHI_LP_SUSHI_ETH = "0x795065dCc9f64b5614C407a6EFDC400DA6221FB0".toLowerCase();

    public static final Map<String, String> lpNameToHash = new LinkedHashMap<>();
    public static final Map<String, Double> lpHashToDividers = new LinkedHashMap<>();
    public static final Map<String, String> lpHashToName = new LinkedHashMap<>();
    public static final Map<String, Tuple2<String, String>> lpHashToCoinNames = new LinkedHashMap<>();
    public static final Map<String, String> keyCoinForLp = new LinkedHashMap<>();
    public static final Set<String> parsable = new HashSet<>();
    public static final Set<String> oneInch = new HashSet<>();

    static {
        try {
            initMaps();
        } catch (Exception e) {
            e.printStackTrace();
        }

        lpHashToCoinNames.put(UNI_LP_ETH_DAI, new Tuple2<>(DAI_NAME, WETH_NAME));
        lpHashToCoinNames.put(UNI_LP_USDC_ETH, new Tuple2<>(USDC_NAME, WETH_NAME));
        lpHashToCoinNames.put(UNI_LP_ETH_USDT, new Tuple2<>(WETH_NAME, USDT_NAME));
        lpHashToCoinNames.put(UNI_LP_ETH_WBTC, new Tuple2<>(WBTC_NAME, WETH_NAME));
        lpHashToCoinNames.put(SUSHI_LP_WBTC_TBTC, new Tuple2<>(WBTC_NAME, TBTC_NAME));
        lpHashToCoinNames.put(SUSHI_LP_ETH_DAI, new Tuple2<>(DAI_NAME, WETH_NAME));
        lpHashToCoinNames.put(SUSHI_LP_ETH_USDC, new Tuple2<>(USDC_NAME, WETH_NAME));
        lpHashToCoinNames.put(SUSHI_LP_ETH_USDT, new Tuple2<>(WETH_NAME, USDT_NAME));
        lpHashToCoinNames.put(SUSHI_LP_ETH_WBTC, new Tuple2<>(WBTC_NAME, WETH_NAME));
        lpHashToCoinNames.put(UNI_LP_IDX_ETH, new Tuple2<>(IDX_NAME, WETH_NAME));
        lpHashToCoinNames.put(UNI_LP_USDC_IDX, new Tuple2<>(IDX_NAME, USDC_NAME));
        lpHashToCoinNames.put(UNI_LP_ETH_DPI, new Tuple2<>(DPI_NAME, WETH_NAME));
        lpHashToCoinNames.put(UNI_LP_WBTC_BADGER, new Tuple2<>(WBTC_NAME, BADGER_NAME));
        lpHashToCoinNames.put(UNI_LP_WETH_FARM, new Tuple2<>(FARM_NAME, WETH_NAME));
        lpHashToCoinNames.put(UNI_LP_USDC_FARM, new Tuple2<>(FARM_NAME, USDC_NAME));
        lpHashToCoinNames.put(UNI_LP_GRAIN_FARM, new Tuple2<>(GRAIN_NAME, FARM_NAME));
        lpHashToCoinNames.put(UNI_LP_BAC_DAI, new Tuple2<>(BAC_NAME, DAI_NAME));
        lpHashToCoinNames.put(UNI_LP_DAI_BAS, new Tuple2<>(DAI_NAME, BAS_NAME));
        lpHashToCoinNames.put(SUSHI_LP_MIC_USDT, new Tuple2<>(MIC_NAME, USDT_NAME));
        lpHashToCoinNames.put(SUSHI_LP_MIS_USDT, new Tuple2<>(MIS_NAME, USDT_NAME));
        lpHashToCoinNames.put(UNI_LP_USDC_WBTC, new Tuple2<>(WBTC_NAME, USDC_NAME));
        lpHashToCoinNames.put(ONEINCH_LP_ETH_DAI, new Tuple2<>(WETH_NAME, DAI_NAME));
        lpHashToCoinNames.put(ONEINCH_LP_ETH_USDC, new Tuple2<>(WETH_NAME, USDC_NAME));
        lpHashToCoinNames.put(ONEINCH_LP_ETH_USDT, new Tuple2<>(WETH_NAME, USDT_NAME));
        lpHashToCoinNames.put(ONEINCH_LP_ETH_WBTC, new Tuple2<>(WETH_NAME, WBTC_NAME));
        lpHashToCoinNames.put(UNI_LP_DAI_BSG, new Tuple2<>(DAI_NAME, BSG_NAME));
        lpHashToCoinNames.put(UNI_LP_DAI_BSGS, new Tuple2<>(DAI_NAME, BSGS_NAME));
        lpHashToCoinNames.put(UNI_LP_ESD_USDC, new Tuple2<>(ESD_NAME, USDC_NAME));
        lpHashToCoinNames.put(UNI_LP_USDC_DSD, new Tuple2<>(USDC_NAME, DSD_NAME));
        lpHashToCoinNames.put(UNI_LP_USDC_EURS, new Tuple2<>(USDC_NAME, EURS_NAME));
        lpHashToCoinNames.put(UNI_LP_MAAPL_UST, new Tuple2<>(UST_NAME, MAAPL_NAME));
        lpHashToCoinNames.put(UNI_LP_MAMZN_UST, new Tuple2<>(MAMZN_NAME, UST_NAME));
        lpHashToCoinNames.put(UNI_LP_MGOOGL_UST, new Tuple2<>(MGOOGL_NAME, UST_NAME));
        lpHashToCoinNames.put(UNI_LP_MTSLA_UST, new Tuple2<>(MTSLA_NAME, UST_NAME));
        lpHashToCoinNames.put(SUSHI_LP_SUSHI_ETH, new Tuple2<>(SUSHI_NAME, WETH_NAME));

        // this LPs use for get price for the key token
        keyCoinForLp.put(UNI_LP_USDC_FARM, FARM_TOKEN);
        keyCoinForLp.put(UNI_LP_WETH_FARM, FARM_TOKEN);
        keyCoinForLp.put(UNI_LP_WBTC_BADGER, BADGER_TOKEN);
        keyCoinForLp.put(UNI_LP_USDC_ETH, WETH_TOKEN);
        keyCoinForLp.put(UNI_LP_GRAIN_FARM, GRAIN_TOKEN);
        keyCoinForLp.put(UNI_LP_BAC_DAI, BAC_TOKEN);
        keyCoinForLp.put(UNI_LP_DAI_BAS, BAS_TOKEN);
        keyCoinForLp.put(SUSHI_LP_MIC_USDT, MIC_TOKEN);
        keyCoinForLp.put(SUSHI_LP_MIS_USDT, MIS_TOKEN);
        keyCoinForLp.put(UNI_LP_DAI_BSG, BSG_TOKEN);
        keyCoinForLp.put(UNI_LP_DAI_BSGS, BSGS_TOKEN);
        keyCoinForLp.put(UNI_LP_ESD_USDC, ESD_TOKEN);
        keyCoinForLp.put(UNI_LP_ETH_DPI, DPI_TOKEN);
        keyCoinForLp.put(UNI_LP_ETH_WBTC, WBTC_TOKEN);
        keyCoinForLp.put(UNI_LP_USDC_DSD, DSD_TOKEN);
        keyCoinForLp.put(UNI_LP_USDC_EURS, EURS_TOKEN);
        keyCoinForLp.put(UNI_LP_MAAPL_UST, MAAPL_TOKEN);
        keyCoinForLp.put(UNI_LP_MAMZN_UST, MAMZN_TOKEN);
        keyCoinForLp.put(UNI_LP_MGOOGL_UST, MGOOGL_TOKEN);
        keyCoinForLp.put(UNI_LP_MTSLA_UST, MTSLA_TOKEN);
        keyCoinForLp.put(SUSHI_LP_SUSHI_ETH, SUSHI_TOKEN);

        parsable.add(UNI_LP_USDC_FARM);
        parsable.add(UNI_LP_WETH_FARM);
        parsable.add(UNI_LP_GRAIN_FARM);

        oneInch.add(ONEINCH_LP_ETH_DAI);
        oneInch.add(ONEINCH_LP_ETH_USDC);
        oneInch.add(ONEINCH_LP_ETH_USDT);
        oneInch.add(ONEINCH_LP_ETH_WBTC);
    }

    public static Map<String, Double> getLpDividers() {
        Map<String, Double> dividers = new HashMap<>();
        for (String lpHash : lpHashToCoinNames.keySet()) {
            dividers.put(lpHash, D18);
        }
        dividers.putAll(lpHashToDividers);
        return dividers;
    }

    public static boolean isDivisionSequenceSecondDividesFirst(String lpHash) {
        Tuple2<String, String> names = lpHashToCoinNames.get(lpHash);
        if (names == null) {
            throw new IllegalStateException("Names not found for " + lpHash);
        }
        String keyTokenHash = keyCoinForLp.get(lpHash);
        if (keyTokenHash == null) {
            throw new IllegalStateException("Key token not found for " + lpHash);
        }
        String keyTokenName = Tokens.findNameForContract(keyTokenHash);
        if (keyTokenName.equalsIgnoreCase(names.component1())) {
            return true;
        } else if (keyTokenName.equalsIgnoreCase(names.component2())) {
            return false;
        } else {
            throw new IllegalStateException("Key token doesn't equal to lp tokens " + lpHash);
        }
    }

    public static String findNameForLpHash(String lpHash) {
        String lpName = lpHashToName.get(lpHash);
        if (lpName == null) {
            throw new IllegalStateException("Not found LP name for " + lpHash);
        }
        return lpName;
    }

    public static String findLpForCoins(String coin1, String coin2) {
        coin1 = simplifyName(coin1);
        coin2 = simplifyName(coin2);
        for (Entry<String, Tuple2<String, String>> entry : lpHashToCoinNames.entrySet()) {
            Tuple2<String, String> coinNames = entry.getValue();
            if (
                (coinNames.component1().equalsIgnoreCase(coin1)
                    || coinNames.component2().equalsIgnoreCase(coin1))
                    && (
                    coinNames.component1().equalsIgnoreCase(coin2)
                        || coinNames.component2().equalsIgnoreCase(coin2)
                )
            ) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Not found LP for " + coin1 + " " + coin2);
    }

    public static double amountToDouble(BigInteger amount, String lpAddress, String coinAddress) {
        Tuple2<String, String> names = lpHashToCoinNames.get(lpAddress);
        if (names == null) {
            throw new IllegalStateException("Not found names for " + lpAddress);
        }
        String coinName = findNameForContract(coinAddress);
        Tuple2<Double, Double> dividers = findLpTokensDividers(lpAddress);
        Double divider;
        if (names.component1().equals(coinName)) {
            divider = dividers.component1();
        } else if (names.component2().equals(coinName)) {
            divider = dividers.component2();
        } else {
            throw new IllegalStateException(String.format("Coin %s not compare with LP %s", coinName, lpAddress));
        }

        return amount.doubleValue() / divider;
    }

    public static Tuple2<Double, Double> findLpTokensDividers(String lpHash) {
        Tuple2<String, String> coinNames = lpHashToCoinNames.get(lpHash);
        if (coinNames == null) {
            throw new IllegalStateException("Not found coin names for " + lpHash);
        }
        double token1Divider = Tokens.getTokenInfo(coinNames.component1()).getDivider();
        double token2Divider = Tokens.getTokenInfo(coinNames.component2()).getDivider();
        return new Tuple2<>(token1Divider, token2Divider);
    }

    //dangerous, but useful
    private static void initMaps() throws IllegalAccessException, NoSuchFieldException {
        for (Field field : LpContracts.class.getDeclaredFields()) {
            if (!(field.get(null) instanceof String)) {
                continue;
            }
            String lpName = field.getName();
            String lpHash = (String) field.get(null);
            lpHashToName.put(lpHash, lpName);
            lpNameToHash.put(lpName, lpHash);
        }
    }

}
