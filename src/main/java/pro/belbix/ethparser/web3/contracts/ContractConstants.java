package pro.belbix.ethparser.web3.contracts;

import java.util.List;

public class ContractConstants {

    public static final String DEPLOYER = "0xf00dD244228F51547f0563e60bCa65a30FBF5f7f".toLowerCase();
    public static final String CONTROLLER = "0x222412af183BCeAdEFd72e4Cb1b71f1889953b1C".toLowerCase();
    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    public static final double D2 = 100.0;
    public static final double D6 = 1000_000.0;
    public static final double D8 = 100_000_000.0;
    public static final double D18 = 1000_000_000_000_000_000.0;

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
