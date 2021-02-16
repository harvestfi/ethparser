package pro.belbix.ethparser.web3.contracts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContractConstants {

    public static final String DEPLOYER = "0xf00dD244228F51547f0563e60bCa65a30FBF5f7f".toLowerCase();
    public static final String CONTROLLER = "0x222412af183BCeAdEFd72e4Cb1b71f1889953b1C".toLowerCase();
    public static final String UNISWAP_FACTORY = "0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f".toLowerCase();
    public static final String SUSHI_FACTORY = "0xc0aee478e3658e2610c5f7a4a2e1777ce9e4f2ac".toLowerCase();
    public static final String MOONISWAP_FACTORY = "0xbaf9a5d4b0052359326a6cdab54babaa3a3a9643".toLowerCase();
    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    public static final double D2 = 100.0;
    public static final double D6 = 1000_000.0;
    public static final double D8 = 100_000_000.0;
    public static final double D18 = 1000_000_000_000_000_000.0;

    public static final int PAIR_TYPE_UNISWAP = 1;
    public static final int PAIR_TYPE_SUSHI = 2;
    public static final int PAIR_TYPE_ONEINCHE= 3;

    public static final Set<String> PARSABLE_UNI_PAIRS = new HashSet<>(List.of(
        "0x514906fc121c7878424a5c928cad1852cc545892", // UNI_LP_USDC_FARM - FARM
        "0x56feaccb7f750b997b36a68625c7c596f0b41a58", // UNI_LP_WETH_FARM - FARM
        "0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49" // UNI_LP_GRAIN_FARM - GRAIN
    ));

    public static final List<Integer> KEY_BLOCKS_FOR_LOADING = List.of(
        10777209, // Sep-01-2020
        10800000, // Sep-05-2020
        10850000, // Sep-12-2020
        10900000, // Sep-20-2020
        10950000, // Sep-28-2020
        11000000, // Oct-06-2020
        11050000, // Oct-13-2020
        11100000, // Oct-21-2020
        11150000, // Oct-29-2020
        11200000, // Nov-05-2020
        11250000, // Nov-13-2020
        11300000, // Nov-21-2020
        11350000, // Nov-28-2020
        11400000, // Dec-06-2020
        11450000, // Dec-14-2020
        11500000, // Dec-22-2020
        11550000, // Dec-29-2020
        11600000, // Jan-06-2020
        11650000, // Jan-14-2020
        11700000, // Jan-21-2020
        11750000 // Jan-29-2020
    );
}
