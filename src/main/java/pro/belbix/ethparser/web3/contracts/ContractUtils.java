package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BSC_BLOCK_NUMBER_25_MARCH_2021;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ETH_BLOCK_NUMBER_30_AUGUST_2020;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MOONISWAP_FACTORY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MOONISWAP_FACTORY_BSC;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ONE_DOLLAR_TOKENS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ORACLES;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_ADDRESSES;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.util.Map.Entry;
import java.util.TreeMap;

public class ContractUtils {

  private ContractUtils() {
  }

  public static String getPsPool(String address) {
    if ("0x59258f4e15a5fc74a7284055a8094f58108dbd4f".equalsIgnoreCase(address)) {
      return "0x59258f4e15a5fc74a7284055a8094f58108dbd4f".toLowerCase();
    }
    return "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".toLowerCase();
  }

  public static boolean isPsName(String name) {
    return name.equals("PS")
        || name.equals("PS_V0")
        || name.equals("ST_PS")
        || name.equals("FARM")
        ;
  }

  public static boolean isPsAddress(String address) {
    return PS_ADDRESSES.contains(address);
  }

  public static boolean isStableCoin(String address) {
    return ONE_DOLLAR_TOKENS.contains(address);
  }

  public static String getBaseAddressInsteadOfZero(String address, String network) {
    return ZERO_ADDRESS.equalsIgnoreCase(address) ?
        getBaseNetworkWrappedTokenAddress(network) :
        address;
  }

  public static int getStartBlock(String network) {
    if (ETH_NETWORK.equals(network)) {
      return ETH_BLOCK_NUMBER_30_AUGUST_2020.getBlockNumber().intValue();
    } else if (BSC_NETWORK.equals(network)) {
      return BSC_BLOCK_NUMBER_25_MARCH_2021.getBlockNumber().intValue();
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  public static boolean isOneInch(String factoryAdr, String network) {
    if (ETH_NETWORK.equals(network)) {
      return MOONISWAP_FACTORY.equalsIgnoreCase(factoryAdr);
    } else if (BSC_NETWORK.equals(network)) {
      return MOONISWAP_FACTORY_BSC.equalsIgnoreCase(factoryAdr);
    }
    return false;
  }

  public static String getBaseNetworkWrappedTokenAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
    } else if (BSC_NETWORK.equals(network)) {
      return "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c";
    }
    return "";
  }

  public static String getBaseNetworkWrappedTokenName(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "ETH";
    } else if (BSC_NETWORK.equals(network)) {
      return "WBNB";
    }
    return "";
  }

  public static String getSimilarAssetForPrice(String name, String network) {
    if (ETH_NETWORK.equals(network)) {
      return getSimilarAssetForPriceEth(name);
    } else if (BSC_NETWORK.equals(network)) {
      return getSimilarActiveForPriceBsc(name);
    }
    return name;
  }

  private static String getSimilarAssetForPriceEth(String name) {
    name = name.replaceFirst("_V0", "");
    switch (name) {
      case "CRV_STETH":
      case "WETH":
        return "ETH";

      case "PS":
      case "iPS":
        return "FARM";

      case "RENBTC":
      case "CRVRENWBTC":
      case "CRV_RENWBTC":
      case "CRV_RENBTC":
      case "TBTC":
      case "BTC":
      case "CRV_OBTC":
      case "CRV_TBTC":
      case "HBTC":
      case "CRV_HBTC":
        return "WBTC";

      case "CRV_EURS":
        return "EURS";

      case "CRV_LINK":
        return "LINK";

      case "SUSHI_HODL":
        return "SUSHI";

      case "YCRV":
      case "_3CRV":
      case "3CRV":
      case "CRV_CMPND":
      case "CRV_BUSD":
      case "CRV_USDN":
      case "CRV_HUSD":
      case "CRV_UST":
      case "CRV_AAVE":
      case "CRV_GUSD":
        return "USDC";
    }
    return name;
  }

  private static String getSimilarActiveForPriceBsc(String name) {
    //noinspection SwitchStatementWithTooFewBranches
    switch (name) {
      case "RENBTC":
        return "BTCB";
    }
    return name;
  }

  public static String getPriceOracle(long block, String network) {
    Entry<Long, String> entry = new TreeMap<>(ORACLES.get(network)).floorEntry(block);
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  public static String getEthAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase();
    } else if (BSC_NETWORK.equals(network)) {
      return "0x2170Ed0880ac9A755fd29B2688956BD959F933F8".toLowerCase();
    }
    return null;
  }
}
