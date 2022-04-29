package pro.belbix.ethparser.web3.contracts;

import java.math.BigInteger;
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

  public static final String CURVE_REGISTRY_ADDRESS = "0x7D86446dDb609eD0F5f8684AcF30380a356b2B4c"
      .toLowerCase();
  public static final String BELT_POOL_ADDRESS = "0xF16D312d119c13dD27fD0dC814b0bCdcaAa62dfD"
      .toLowerCase();
  public static final String ONE_INCH_FACTORY_ADDRESS = "0xbAF9A5d4b0052359326A6CDAb54BABAa3a3A9643"
      .toLowerCase();
  static final String ONE_INCH_FACTORY_BSC = "0xD41B24bbA51fAc0E4827b6F94C0D6DDeB183cD64"
      .toLowerCase();
  public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
  public static final String CURVE_ZERO_ADDRESS = "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
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
  public static final String ST_PS_ADDRESS = "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C"
      .toLowerCase();

  public static final String BANCOR_CONVERSION_ADDRESS = "0x2f9ec37d6ccfff1cab21733bdadede11c823ccb0";
}
