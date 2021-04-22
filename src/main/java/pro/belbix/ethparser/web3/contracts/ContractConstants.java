package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.tuples.generated.Tuple2;

public class ContractConstants {

  public static final DefaultBlockParameter ETH_BLOCK_NUMBER_30_AUGUST_2020 =
      DefaultBlockParameter.valueOf(new BigInteger("10765094"));
  public static final DefaultBlockParameter BSC_BLOCK_NUMBER_25_MARCH_2021 =
      DefaultBlockParameter.valueOf(new BigInteger("5993570"));

  public final static Map<String, String> DEPLOYERS = Map.of(
      ETH_NETWORK, "0xf00dD244228F51547f0563e60bCa65a30FBF5f7f".toLowerCase(),
      BSC_NETWORK, "0xf00dd244228f51547f0563e60bca65a30fbf5f7f".toLowerCase()
  );

  public final static Map<String, String> CONTROLLERS = Map.of(
      ETH_NETWORK, "0x222412af183BCeAdEFd72e4Cb1b71f1889953b1C".toLowerCase(),
      BSC_NETWORK, "0x222412af183bceadefd72e4cb1b71f1889953b1c".toLowerCase()
  );

  public final static Map<String, String> NOTIFY_HELPER = Map.of(
      ETH_NETWORK, "0xe20c31e3d08027f5aface84a3a46b7b3b165053c".toLowerCase(),
      BSC_NETWORK, "0xf71042c88458ff1702c3870f62f4c764712cc9f0".toLowerCase()
  );

  public final static Map<String, Tuple2<Long, String>> ORACLES = Map.of(
      ETH_NETWORK,
      new Tuple2<>(12015724L, "0x48DC32eCA58106f06b41dE514F29780FFA59c279".toLowerCase()),
      BSC_NETWORK,
      new Tuple2<>(6442627L, "0xE0e9F05054Ad3a2b6414AD13D768be91a84b47e8".toLowerCase())
  );

  static final String UNISWAP_FACTORY = "0x5c69bee701ef814a2b6a3edd4b1652cb9cc5aa6f"
      .toLowerCase();
  static final String SUSHI_FACTORY = "0xc0aee478e3658e2610c5f7a4a2e1777ce9e4f2ac"
      .toLowerCase();
  static final String MOONISWAP_FACTORY = "0xbaf9a5d4b0052359326a6cdab54babaa3a3a9643"
      .toLowerCase();
  static final String MOONISWAP_FACTORY_BSC = "0xD41B24bbA51fAc0E4827b6F94C0D6DDeB183cD64"
      .toLowerCase();
  public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
  public static final String FARM_TOKEN = "0xa0246c9032bc3a600820415ae600c6388619a14d"
      .toLowerCase();

  public static final double D2 = 100.0;
  public static final double D6 = 1000_000.0;
  public static final double D8 = 100_000_000.0;
  public static final double D18 = 1000_000_000_000_000_000.0;

  public static final int PAIR_TYPE_UNISWAP = 1;
  public static final int PAIR_TYPE_SUSHI = 2;
  public static final int PAIR_TYPE_ONEINCHE = 3;

  public static final Map<String, Set<String>> PARSABLE_UNI_PAIRS = Map.of(
      ETH_NETWORK, Set.of(
          "0x514906fc121c7878424a5c928cad1852cc545892".toLowerCase(), // UNI_LP_USDC_FARM - FARM
          "0x56feaccb7f750b997b36a68625c7c596f0b41a58".toLowerCase(), // UNI_LP_WETH_FARM - FARM
          "0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49".toLowerCase() // UNI_LP_GRAIN_FARM - GRAIN
      ),
      BSC_NETWORK, Set.of()
  );

  public static final Set<String> PS_ADDRESSES = Set.of(
      "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".toLowerCase(), // ST_PS
      "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase(), // FARM TOKEN
      "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50".toLowerCase(), // PS
      "0x59258F4e15A5fC74A7284055A8094F58108dbD4f".toLowerCase() // PS_V0
  );

  public static final Set<String> ONE_DOLLAR_TOKENS = Set.of(
      "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48", //USDC
      "0xe9e7cea3dedca5984780bafc599bd69add087d56" //BUSD
  );
}
