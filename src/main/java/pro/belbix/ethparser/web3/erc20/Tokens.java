package pro.belbix.ethparser.web3.erc20;

import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.keyCoinForLp;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.lpHashToCoinNames;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.web3j.tuples.generated.Tuple2;

public class Tokens {

    public static final String FARM_TOKEN = "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase();
    public static final String BADGER_TOKEN = "0x3472A5A71965499acd81997a54BBA8D852C6E53d".toLowerCase();
    public final static String USDC_TOKEN = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase();
    public final static String WETH_TOKEN = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase();
    public final static String WBTC_TOKEN = "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599".toLowerCase();
    public final static String DAI_TOKEN = "0x6b175474e89094c44da98b954eedeac495271d0f".toLowerCase();
    public final static String TBTC_TOKEN = "0x8daebade922df735c38c80c7ebd708af50815faa".toLowerCase();
    public final static String USDT_TOKEN = "0xdac17f958d2ee523a2206206994597c13d831ec7".toLowerCase();
    public final static String IDX_TOKEN = "0x95b3497bbcccc46a8f45f5cf54b0878b39f8d96c".toLowerCase();
    public final static String DPI_TOKEN = "0x1494ca1f11d487c2bbe4543e90080aeba4ba3c2b".toLowerCase();
    public final static String GRAIN_TOKEN = "0x6589fe1271A0F29346796C6bAf0cdF619e25e58e".toLowerCase();
    public final static String TUSD_TOKEN = "0x0000000000085d4780b73119b644ae5ecd22b376".toLowerCase();
    public final static String BAC_TOKEN = "0x3449fc1cd036255ba1eb19d65ff4ba2b8903a69a".toLowerCase();
    public final static String BAS_TOKEN = "0xa7ED29B253D8B4E3109ce07c80fc570f81B63696".toLowerCase();
    public final static String MIC_TOKEN = "0x368B3a58B5f49392e5C9E4C998cb0bB966752E51".toLowerCase();
    public final static String MIS_TOKEN = "0x4b4D2e899658FB59b1D518b68fe836B100ee8958".toLowerCase();

    public static final String FARM_NAME = "FARM";
    public static final String BADGER_NAME = "BADGER";
    public static final String USDC_NAME = "USDC";
    public static final String WETH_NAME = "ETH";
    public static final String WBTC_NAME = "WBTC";
    public static final String DAI_NAME = "DAI";
    public static final String TBTC_NAME = "TBTC";
    public static final String USDT_NAME = "USDT";
    public static final String IDX_NAME = "IDX";
    public static final String DPI_NAME = "DPI";
    public static final String GRAIN_NAME = "GRAIN";
    public static final String TUSD_NAME = "TUSD";
    public static final String BAC_NAME = "BAC";
    public static final String BAS_NAME = "BAS";
    public static final String MIC_NAME = "MIC";
    public static final String MIS_NAME = "MIS";

    private final static Set<TokenInfo> tokenInfos = new HashSet<>();

    static {
        tokenInfos.add(new TokenInfo(FARM_NAME, FARM_TOKEN, 10777201)
            .addLp("UNI_LP_USDC_FARM", 0, USDC_NAME)
            .addLp("UNI_LP_WETH_FARM", 11609000, WETH_NAME));

        tokenInfos.add(new TokenInfo(DPI_NAME, DPI_TOKEN, 10868422)
            .addLp("UNI_LP_ETH_DPI", 0, WETH_NAME));

        tokenInfos.add(new TokenInfo(GRAIN_NAME, GRAIN_TOKEN, 11408083)
            .addLp("UNI_LP_GRAIN_FARM", 0, FARM_NAME));

        tokenInfos.add(new TokenInfo(BADGER_NAME, BADGER_TOKEN, 10868422)
            .addLp("UNI_LP_WBTC_BADGER", 0, WBTC_NAME));

        tokenInfos.add(new TokenInfo(WETH_NAME, WETH_TOKEN, 0)
            .addLp("UNI_LP_ETH_USDC", 0, USDC_NAME));

        tokenInfos.add(new TokenInfo(WBTC_NAME, WBTC_TOKEN, 0)
            .addLp("UNI_LP_ETH_WBTC", 0, WETH_NAME));

        tokenInfos.add(new TokenInfo(BAC_NAME, BAC_TOKEN, 11356492)
            .addLp("UNI_LP_BAC_DAI", 0, DAI_NAME));

        tokenInfos.add(new TokenInfo(BAS_NAME, BAS_TOKEN, 11356501)
            .addLp("UNI_LP_DAI_BAS", 0, DAI_NAME));

        tokenInfos.add(new TokenInfo(MIC_NAME, MIC_TOKEN, 11551514)
            .addLp("SUSHI_LP_MIC_USDT", 0, USDT_NAME));

        tokenInfos.add(new TokenInfo(MIS_NAME, MIS_TOKEN, 11551671)
            .addLp("SUSHI_LP_MIS_USDT", 0, USDT_NAME));

        //todo stablecoins
        tokenInfos.add(new TokenInfo(USDC_NAME, USDC_TOKEN, 0));
        tokenInfos.add(new TokenInfo(DAI_NAME, DAI_TOKEN, 0));
        tokenInfos.add(new TokenInfo(USDT_NAME, USDT_TOKEN, 0));
        tokenInfos.add(new TokenInfo(USDT_NAME, USDT_TOKEN, 0));
        tokenInfos.add(new TokenInfo(TUSD_NAME, TUSD_TOKEN, 0));
    }

    public static TokenInfo getTokenInfo(String tokenName) {
        for (TokenInfo info : tokenInfos) {
            if (tokenName.equals(info.getTokenName())) {
                return info;
            }
        }
        throw new IllegalStateException("Not found token info for " + tokenName);
    }

    public static Map<String, Double> getTokensDividers() {
        Map<String, Double> d = new HashMap<>();
        for (TokenInfo info : tokenInfos) {
            d.put(info.getTokenAddress(), info.getDivider());
        }
        return d;
    }

    public static boolean isCreated(String tokenName, int block) {
        if (isStableCoin(tokenName)) {
            return true;
        }
        return getTokenInfo(tokenName).getCreatedOnBlock() < block;
    }

    public static boolean firstCoinIsKey(String lpAddress) {
        Tuple2<String, String> names = lpHashToCoinNames.get(lpAddress);
        if (names == null) {
            throw new IllegalStateException("Names not found for " + lpAddress);
        }
        String keyCoin = keyCoinForLp.get(lpAddress);
        if (keyCoin == null) {
            throw new IllegalStateException("Key coin not found for " + lpAddress);
        }
        String keyCoinName = findNameForContract(keyCoin);
        if (keyCoinName == null) {
            throw new IllegalStateException("Key coin name not found for " + keyCoin);
        }
        if (names.component1().equals(keyCoinName)) {
            return true;
        } else if (names.component2().equals(keyCoinName)) {
            return false;
        } else {
            throw new IllegalStateException("Not found key name in lp " + lpAddress);
        }
    }

    public static String findNameForContract(String contract) {
        for (TokenInfo info : tokenInfos) {
            if (info.getTokenAddress().equals(contract)) {
                return info.getTokenName();
            }
        }
        throw new IllegalStateException("Not found name for " + contract);
    }

    public static String findContractForName(String name) {
        return getTokenInfo(name).getTokenAddress();
    }

    public static String mapLpAddressToCoin(String address) {
        return mapLpAddress(address, true);
    }

    public static String mapLpAddressToOtherCoin(String address) {
        return mapLpAddress(address, false);
    }

    private static String mapLpAddress(String address, boolean isKeyCoin) {
        String keyCoin = keyCoinForLp.get(address);
        if (keyCoin == null) {
            throw new IllegalStateException("Not found key coin for " + address);
        }
        String keyCoinName = findNameForContract(keyCoin);
        if (keyCoinName == null) {
            throw new IllegalStateException("Not found key coin name for " + keyCoin);
        }
        Tuple2<String, String> pairNames = lpHashToCoinNames.get(address);
        if (pairNames == null) {
            throw new IllegalStateException("Unknown contract " + address);
        }
        int i;
        if (pairNames.component1().equals(keyCoinName)) {
            i = 1;
        } else if (pairNames.component2().equals(keyCoinName)) {
            i = 2;
        } else {
            throw new IllegalStateException("Key coin not found in " + pairNames);
        }
        String pairName;
        if (isKeyCoin) {
            pairName = getStringFromPair(pairNames, i, false);
        } else {
            pairName = getStringFromPair(pairNames, i, true);
        }
        String hash = findContractForName(pairName);
        if (hash == null) {
            throw new IllegalStateException("Hash not found for " + pairNames.component2());
        }
        return hash;
    }

    private static String getStringFromPair(Tuple2<String, String> pair, int i, boolean inverse) {
        if (i == 1) {
            if (inverse) {
                return pair.component2();
            } else {
                return pair.component1();
            }
        } else if (i == 2) {
            if (inverse) {
                return pair.component1();
            } else {
                return pair.component2();
            }
        } else {
            throw new IllegalStateException("Wrong index for pair " + i);
        }
    }

    public static boolean isStableCoin(String name) {
        return "USD".equals(name)
            || USDC_NAME.equals(name)
            || USDT_NAME.equals(name)
            || "YCRV".equals(name)
            || "3CRV".equals(name)
            || "_3CRV".equals(name)
            || TUSD_NAME.equals(name)
            || DAI_NAME.equals(name)
            || "CRV_CMPND".equals(name)
            || "CRV_BUSD".equals(name)
            || "CRV_USDN".equals(name)
            || "HUSD".equals(name)
            || "CRV_HUSD".equals(name)
            ;
    }

    public static String simplifyName(String name) {
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

}
