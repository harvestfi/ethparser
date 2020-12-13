package pro.belbix.ethparser.web3.uniswap.contracts;

import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.keyCoinForLp;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.lpHashToCoinNames;

import java.util.HashMap;
import java.util.Map;
import org.web3j.tuples.generated.Tuple2;

public class Tokens {

    public static final String FARM_TOKEN = "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase();
    public static final String BADGER_TOKEN = "0x3472A5A71965499acd81997a54BBA8D852C6E53d".toLowerCase();
    public final static String USDC_TOKEN = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase();
    public final static String WETH_TOKEN = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase();
    public final static String WBTC_TOKEN = "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599".toLowerCase();
    public final static String DAI_TOKEN = "".toLowerCase();
    public final static String TBTC_TOKEN = "".toLowerCase();
    public final static String USDT_TOKEN = "".toLowerCase();
    public final static String IDX_TOKEN = "".toLowerCase();
    public final static String DPI_TOKEN = "".toLowerCase();
    public final static String GRAIN_TOKEN = "0x6589fe1271A0F29346796C6bAf0cdF619e25e58e".toLowerCase();

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

    private final static Map<String, String> tokenHashToName = new HashMap<>();
    private final static Map<String, String> tokenNameToHash = new HashMap<>();

    static {
        tokenHashToName.put(WETH_TOKEN, WETH_NAME);
        tokenHashToName.put(FARM_TOKEN, FARM_NAME);
        tokenHashToName.put(USDC_TOKEN, USDC_NAME);
        tokenHashToName.put(BADGER_TOKEN, BADGER_NAME);
        tokenHashToName.put(WBTC_TOKEN, WBTC_NAME);
        tokenHashToName.put(GRAIN_TOKEN, GRAIN_NAME);

        tokenNameToHash.put(WETH_NAME, WETH_TOKEN);
        tokenNameToHash.put(FARM_NAME, FARM_TOKEN);
        tokenNameToHash.put(USDC_NAME, USDC_TOKEN);
        tokenNameToHash.put(BADGER_NAME, BADGER_TOKEN);
        tokenNameToHash.put(WBTC_NAME, WBTC_TOKEN);
        tokenNameToHash.put(GRAIN_NAME, GRAIN_TOKEN);


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
        String keyCoinName = tokenHashToName.get(keyCoin);
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
        String name = tokenHashToName.get(contract);
        if(name == null) {
            throw new IllegalStateException("Name not found for " + contract);
        }
        return name;
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
        String keyCoinName = tokenHashToName.get(keyCoin);
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
        String hash = tokenNameToHash.get(pairName);
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
}
