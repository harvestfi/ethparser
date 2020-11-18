package pro.belbix.ethparser.web3;

import java.util.HashMap;
import java.util.Map;

public class ContractMapper {
    public static final double D6 = 1000_000.0;
    public static final double D8 = 100_000_000.0;
    public static final double D18 = 1000_000_000_000_000_000.0;

    private final static Map<String, String> names = new HashMap<>();
    private final static Map<String, String> digits = new HashMap<>();

    static {
        names.put("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2", "WETH");
        names.put("0xa0246c9032bc3a600820415ae600c6388619a14d", "FARM");
        names.put("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48", "USDC");
    }

    static {
        digits.put("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2", "1000000000000000000");
        digits.put("0xa0246c9032bc3a600820415ae600c6388619a14d", "1000000000000000000");
        digits.put("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48", "1000000");
    }


    public static String findName(String contract) {
        return names.getOrDefault(contract, contract);
    }

    public static String findDivider(String contract) {
        return digits.getOrDefault(contract, "1");
    }
}
