package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;

public class ContractConstants {

  public static final double D2 = 100.0;
  public static final double D6 = 1000_000.0;
  public static final double D8 = 100_000_000.0;
  public static final double D18 = 1000_000_000_000_000_000.0;

  public static final long L18 = 1000_000_000_000_000_000L;

  public static final int PAIR_TYPE_UNISWAP = 1;
  public static final int PAIR_TYPE_SUSHI = 2;
  public static final int PAIR_TYPE_ONEINCHE = 3;

  public static final DefaultBlockParameterNumber ETH_BLOCK_NUMBER_30_AUGUST_2020 =
      (DefaultBlockParameterNumber) DefaultBlockParameter.valueOf(new BigInteger("10765094"));
  public static final DefaultBlockParameterNumber BSC_BLOCK_NUMBER_18_MARCH_2021 =
      (DefaultBlockParameterNumber) DefaultBlockParameter.valueOf(new BigInteger("5800123"));
  public static final DefaultBlockParameterNumber MATIC_BLOCK_NUMBER_06_JUL_2021 =
      (DefaultBlockParameterNumber) DefaultBlockParameter.valueOf(new BigInteger("16566542"));

  public static final String PCS_V1_FACTORY_ADDRESS = "0xbcfccbde45ce874adcb698cc183debcf17952812"
      .toLowerCase();
  public static final String PCS_V2_FACTORY_ADDRESS = "0xca143ce32fe78f1f7019d7d551a6402fc5350c73"
      .toLowerCase();
  public static final String UNISWAP_FACTORY_ADDRESS = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"
      .toLowerCase();
  public static final String SUSHISWAP_FACTORY_ADDRESS = "0xC0AEe478e3658e2610c5F7A4A2E1777cE9e4f2Ac"
      .toLowerCase();
  public static final String CURVE_REGISTRY_ADDRESS = "0x7D86446dDb609eD0F5f8684AcF30380a356b2B4c"
      .toLowerCase();
  public static final String BELT_POOL_ADDRESS = "0xF16D312d119c13dD27fD0dC814b0bCdcaAa62dfD"
      .toLowerCase();
  public static final String ONE_INCH_FACTORY_ADDRESS = "0xbAF9A5d4b0052359326A6CDAb54BABAa3a3A9643"
      .toLowerCase();
  static final String ONE_INCH_FACTORY_BSC = "0xD41B24bbA51fAc0E4827b6F94C0D6DDeB183cD64"
      .toLowerCase();
  public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
  public static final String FARM_TOKEN = "0xa0246c9032bc3a600820415ae600c6388619a14d"
      .toLowerCase();
  public static final String BSC_FARM_TOKEN = "0x4B5C23cac08a567ecf0c1fFcA8372A45a5D33743"
      .toLowerCase();
  public static final String MATIC_FARM_TOKEN = "0xab0b2ddb9c7e440fac8e140a89c0dbcbf2d7bbff"
      .toLowerCase();
  public static final String GRAIN_TOKEN = "0x6589fe1271A0F29346796C6bAf0cdF619e25e58e"
      .toLowerCase();
  public static final String PS_ADDRESS = "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50"
      .toLowerCase();
  public static final String PS_V0_ADDRESS = "0x59258F4e15A5fC74A7284055A8094F58108dbD4f"
      .toLowerCase();
  public static final String iPS_ADDRESS = "0x1571eD0bed4D987fe2b498DdBaE7DFA19519F651"
      .toLowerCase();
  public static final String ST_PS_ADDRESS = "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C"
      .toLowerCase();

  public static final String QUICK_FACTORY_ADDRESS = "0x5757371414417b8C6CAad45bAeF941aBc7d3Ab32"
      .toLowerCase();
  public static final String SUSHI_FACTORY_ADDRESS = "0xc35dadb65012ec5796536bd9864ed8773abc74c4"
      .toLowerCase();
  public static final String BANCOR_CONVERSION_ADDRESS = "0x2f9ec37d6ccfff1cab21733bdadede11c823ccb0";

  public final static Map<String, String> DEPLOYERS = Map.of(
      ETH_NETWORK, "0xf00dD244228F51547f0563e60bCa65a30FBF5f7f".toLowerCase(),
      BSC_NETWORK, "0xf00dd244228f51547f0563e60bca65a30fbf5f7f".toLowerCase(),
      MATIC_NETWORK, "0xf00dd244228f51547f0563e60bca65a30fbf5f7f".toLowerCase()
  );

  final static Map<String, Map<Long, String>> CONTROLLERS = Map.of(
      ETH_NETWORK, Map.of(
          10770087L, "0x222412af183BCeAdEFd72e4Cb1b71f1889953b1C".toLowerCase(),
          12652885L, "0x3cc47874dc50d98425ec79e647d83495637c55e3".toLowerCase()),
      BSC_NETWORK, Map.of(
          5990839L, "0x222412af183bceadefd72e4cb1b71f1889953b1c".toLowerCase()),
      MATIC_NETWORK,  Map.of(
          16612698L, "0x2ce34b1bb247f242f1d2a33811e01138968efbff".toLowerCase()
      )
  );

  public final static Map<String, String> NOTIFY_HELPER = Map.of(
      ETH_NETWORK, "0xe20c31e3d08027f5aface84a3a46b7b3b165053c".toLowerCase(),
      BSC_NETWORK, "0xf71042c88458ff1702c3870f62f4c764712cc9f0".toLowerCase(),
      MATIC_NETWORK, "0xe85c8581e60d7cd32bbfd86303d2a4fa6a951dac".toLowerCase()
  );

  final static Map<String, Map<Long, String>> ORACLES = Map.of(
      ETH_NETWORK,
      Map.of(12015724L, "0x48DC32eCA58106f06b41dE514F29780FFA59c279".toLowerCase(),
          12820106L, "0x1358c91D5b25D3eDAc2b7B26A619163d78f1717d".toLowerCase()),
      BSC_NETWORK,
      Map.of(6442627L, "0xE0e9F05054Ad3a2b6414AD13D768be91a84b47e8".toLowerCase(),
          6952687L, "0x643cF46eef91Bd878D9710ceEB6a7E6F929F2608".toLowerCase(),
          9142012L, "0x0E74303d0D18884Ce2CEb3670e72686645c4f38B".toLowerCase()),
     MATIC_NETWORK,
      Map.of(16841617L, "0x0E74303d0D18884Ce2CEb3670e72686645c4f38B".toLowerCase())
      );

  final static Map<String, Map<String, String>> ORACLES_BY_FACTORY = Map.of(
      ETH_NETWORK,
      Map.of(UNISWAP_FACTORY_ADDRESS,
          "0x48DC32eCA58106f06b41dE514F29780FFA59c279".toLowerCase()),
      BSC_NETWORK,
      Map.of(PCS_V1_FACTORY_ADDRESS,
          "0xE0e9F05054Ad3a2b6414AD13D768be91a84b47e8".toLowerCase(), // V1
          PCS_V2_FACTORY_ADDRESS,
          "0x643cF46eef91Bd878D9710ceEB6a7E6F929F2608".toLowerCase()) // V2
  );

  static final Map<String, Map<String, Integer>> FULL_PARSABLE_UNI_PAIRS = Map.of(
      ETH_NETWORK, Map.of(
          "0x514906fc121c7878424a5c928cad1852cc545892".toLowerCase(), 10777067,
          // UNI_LP_USDC_FARM - FARM
          "0x56feaccb7f750b997b36a68625c7c596f0b41a58".toLowerCase(), 11407437,
          // UNI_LP_WETH_FARM - FARM
          "0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49".toLowerCase(), 11407202
          // UNI_LP_GRAIN_FARM - GRAIN
      ),
      BSC_NETWORK, Map.of(),
      MATIC_NETWORK, Map.of()
  );

  public static final List<String> BANCOR_USDC_CONVERT_PATH = List.of(
      "0x1F573D6Fb3F13d689FF844B4cE37794d79a7FF1C".toLowerCase(), // BNT
      "0x874d8dE5b26c9D9f6aA8d7bab283F9A9c6f777f4".toLowerCase(), // Liquidity Pool (USDCBNT)
      "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48".toLowerCase()  // USDC
  );

  static final Map<String, Map<String, Integer>> PARSABLE_BANCOR_TRANSACTIONS = Map.of(
      ETH_NETWORK, Map.of(
          BANCOR_CONVERSION_ADDRESS, 10285676
      ),
      BSC_NETWORK, Map.of(),
      MATIC_NETWORK, Map.of()
  );

  public static final Set<String> PS_ADDRESSES = Set.of(
      "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".toLowerCase(), // ST_PS
      FARM_TOKEN, // FARM TOKEN
      PS_ADDRESS, // PS
      PS_V0_ADDRESS // PS_V0
  );

  public static final Set<String> ONE_DOLLAR_TOKENS = Set.of(
      "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase(), //USDC
      "0xe9e7cea3dedca5984780bafc599bd69add087d56".toLowerCase(), //BUSD
      "0xdAC17F958D2ee523a2206206994597C13D831ec7".toLowerCase(), //USDT
      "0x0000000000085d4780B73119b644AE5ecd22b376".toLowerCase(), //TUSD
      "0x2791bca1f2de4661ed88a30c99a7a9449aa84174".toLowerCase(), //matic USDC
      "0xc2132d05d31c914a87c6611c10748aeb04b58e8f".toLowerCase() //matic USDT
  );

  //Key tokens are used to find liquidity for any given token on Uni, Sushi and Curve.
  public static final Map<String, Set<String>> KEY_TOKENS = Map.of(
      ETH_NETWORK, Set.of(
          "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48".toLowerCase(), //USDC
          "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2".toLowerCase(), //WETH
          "0x6B175474E89094C44Da98b954EedeAC495271d0F".toLowerCase(), //DAI
          "0xdAC17F958D2ee523a2206206994597C13D831ec7".toLowerCase(), //USDT
          "0xa47c8bf37f92aBed4A126BDA807A7b7498661acD".toLowerCase(), //UST
          "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599".toLowerCase(), //WBTC
          "0xdB25f211AB05b1c97D595516F45794528a807ad8".toLowerCase(), //EURS
          "0x514910771AF9Ca656af840dff83E8264EcF986CA".toLowerCase()  //LINK
      ),
      BSC_NETWORK, Set.of(
          "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d".toLowerCase(), //USDC
          "0x2170Ed0880ac9A755fd29B2688956BD959F933F8".toLowerCase(), //ETH
          "0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3".toLowerCase(), //DAI
          "0x55d398326f99059fF775485246999027B3197955".toLowerCase(), //USDT
          "0x23396cF899Ca06c4472205fC903bDB4de249D6fC".toLowerCase(), //UST
          "0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c".toLowerCase(), //BTCB
          "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56".toLowerCase(), //BUSD
          "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c".toLowerCase(), //WBNB
          "0x4BD17003473389A42DAF6a0a729f6Fdb328BbBd7".toLowerCase(), //VAI
          "0x111111111117dC0aa78b770fA6A738034120C302".toLowerCase() //1INCH
      ),
      MATIC_NETWORK, Set.of(
          "0x2791bca1f2de4661ed88a30c99a7a9449aa84174".toLowerCase(), //USDC
          "0x7ceb23fd6bc0add59e62ac25578270cff1b9f619".toLowerCase(), //ETH
          "0x8f3cf7ad23cd3cadbd9735aff958023239c6a063".toLowerCase(), //DAI
          "0xc2132d05d31c914a87c6611c10748aeb04b58e8f".toLowerCase(), //USDT
          "0x692597b009d13c4049a947cab2239b7d6517875f".toLowerCase(), //UST
          "0xdab529f40e671a1d4bf91361c21bf9f0c9712ab7".toLowerCase(), //BUSD
          "0x1bfd67037b42cf73acf2047067bd4f2c47d9bfd6".toLowerCase() //WBTC
      )
  );

  //Pricing tokens are Key tokens with good liquidity with the defined output token on Uniswap.
  public static final Map<String, Set<String>> PRISING_TOKENS = Map.of(
      ETH_NETWORK, Set.of(
          "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48".toLowerCase(), //USDC
          "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2".toLowerCase(), //WETH
          "0x6B175474E89094C44Da98b954EedeAC495271d0F".toLowerCase(), //DAI
          "0xdAC17F958D2ee523a2206206994597C13D831ec7".toLowerCase(), //USDT
          "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599".toLowerCase(), //WBTC
          "0xdB25f211AB05b1c97D595516F45794528a807ad8".toLowerCase()  //EURS
      )
  );

  public static final Map<String, Map<String, Integer>> UNI_FACTORIES = Map.of(
      ETH_NETWORK, Map.of(
          UNISWAP_FACTORY_ADDRESS, 10000835,
          SUSHISWAP_FACTORY_ADDRESS, 10794229
      ), BSC_NETWORK, Map.of(
          PCS_V1_FACTORY_ADDRESS, 586851,
          PCS_V2_FACTORY_ADDRESS, 6809737
      ), MATIC_NETWORK, Map.of(
          QUICK_FACTORY_ADDRESS, 4931780,
          SUSHI_FACTORY_ADDRESS, 11333218
      )
  );

  public static final Map<String, List<String>> PS_ADDRESSES_BY_NETWORK = Map.of(
      ETH_NETWORK, List.of(
          "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".toLowerCase(), // ST_PS
          FARM_TOKEN, // FARM TOKEN
          PS_ADDRESS, // PS
          PS_V0_ADDRESS // PS_V0
      ),
      BSC_NETWORK, List.of(
          BSC_FARM_TOKEN
      ),
      MATIC_NETWORK, List.of(
          MATIC_FARM_TOKEN
      )
  );
}
