package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BSC_BLOCK_NUMBER_18_MARCH_2021;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BSC_FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLERS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ETH_BLOCK_NUMBER_30_AUGUST_2020;
import static pro.belbix.ethparser.web3.contracts.ContractConstantsV7.FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.FULL_PARSABLE_UNI_PAIRS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MATIC_BLOCK_NUMBER_06_JUL_2021;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.MATIC_FARM_TOKEN;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ONE_INCH_FACTORY_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ONE_INCH_FACTORY_BSC;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_BANCOR_TRANSACTIONS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_V0_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ContractUtils {

  private ContractUtils() {
  }

  public static String getPsPool(String address) {
    if (PS_V0_ADDRESS.equalsIgnoreCase(address)) {
      return PS_V0_ADDRESS.toLowerCase();
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
    if (address == null) {
      return false;
    }
    return ContractConstantsV6.PS_ADDRESSES.contains(address.toLowerCase());
  }

  public static boolean isPsAddress(String address, String network) {
    if (address == null) {
      return false;
    }
    return ContractConstantsV3.PS_ADDRESSES_BY_NETWORK.get(network).contains(address.toLowerCase());
  }

  public static boolean isFarmAddress(String address) {
    return "0xa0246c9032bc3a600820415ae600c6388619a14d".equalsIgnoreCase(address)
        || "0x4B5C23cac08a567ecf0c1fFcA8372A45a5D33743".equalsIgnoreCase(address)
        || "0xab0b2ddb9c7e440fac8e140a89c0dbcbf2d7bbff".equalsIgnoreCase(address);
  }

  public static boolean isStableCoin(String address) {
    return ContractConstantsV5.ONE_DOLLAR_TOKENS.contains(address.toLowerCase());
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
      return BSC_BLOCK_NUMBER_18_MARCH_2021.getBlockNumber().intValue();
    }else if (MATIC_NETWORK.equals(network)) {
      return MATIC_BLOCK_NUMBER_06_JUL_2021.getBlockNumber().intValue();
    }
    else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  public static boolean isOneInch(String factoryAdr, String network) {
    if (ETH_NETWORK.equals(network)) {
      return ONE_INCH_FACTORY_ADDRESS.equalsIgnoreCase(factoryAdr);
    } else if (BSC_NETWORK.equals(network)) {
      return ONE_INCH_FACTORY_BSC.equalsIgnoreCase(factoryAdr);
    }
    return false;
  }

  public static String getBaseNetworkWrappedTokenAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2";
    } else if (BSC_NETWORK.equals(network)) {
      return "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c";
    } else if (MATIC_NETWORK.equals(network)) {
      return "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270";
    }
    return "";
  }

  public static String getBaseNetworkWrappedTokenName(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "ETH";
    } else if (BSC_NETWORK.equals(network)) {
      return "WBNB";
    } else if (MATIC_NETWORK.equals(network)) {
      return "WMATIC";
    }
    return "";
  }

  public static List<String> getControllerAddressByNetwork(String network) {
    Map<Long, String> entry = CONTROLLERS.get(network);
    if (entry == null) {
      return null;
    }
    return new ArrayList<>(entry.values());
  }

  public static String getControllerAddressByBlockAndNetwork(long block, String network) {
    Entry<Long, String> entry = new TreeMap<>(CONTROLLERS.get(network)).floorEntry(block);
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  public static String getPriceOracle(long block, String network) {
    Entry<Long, String> entry = new TreeMap<>(ContractConstantsV3.ORACLES.get(network)).floorEntry(block);
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  public static String getOldPriceOracle(String network) {
    Entry<Long, String> entry = new TreeMap<>(ContractConstantsV3.ORACLES.get(network)).firstEntry();
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  public static String getPriceOracleByFactory(String factory, String network) {
    String oracle = ContractConstantsV3.ORACLES_BY_FACTORY.get(network).get(factory);
    if (oracle == null) {
      throw new IllegalStateException("Factory " + factory + " not found");
    }
    return oracle;
  }

  public static String getEthAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase();
    } else if (BSC_NETWORK.equals(network)) {
      return "0x2170Ed0880ac9A755fd29B2688956BD959F933F8".toLowerCase();
    } else if (MATIC_NETWORK.equals(network)) {
      return "0x7ceb23fd6bc0add59e62ac25578270cff1b9f619".toLowerCase();
    }
    return null;
  }

  public static String getBtcAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599".toLowerCase();
    } else if (BSC_NETWORK.equals(network)) {
      return "0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c".toLowerCase();
    }else if (MATIC_NETWORK.equals(network)) {
      return "0x1bfd67037b42cf73acf2047067bd4f2c47d9bfd6".toLowerCase();
    }
    return null;
  }

  public static String getUsdAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase();
    } else if (BSC_NETWORK.equals(network)) {
      return "0xe9e7cea3dedca5984780bafc599bd69add087d56".toLowerCase();
    }
    return null;
  }

  public static String getEurAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0xdb25f211ab05b1c97d595516f45794528a807ad8".toLowerCase();
    } else if (BSC_NETWORK.equals(network)) {
      // didn't find, use busd
      return "0xe9e7cea3dedca5984780bafc599bd69add087d56".toLowerCase();
    }
    return null;
  }

  public static String getLinkAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return "0x514910771af9ca656af840dff83e8264ecf986ca".toLowerCase();
    } else if (BSC_NETWORK.equals(network)) {
      return "0xf8a0bf9cf54bb92f17374d9e9a321e6a111a51bd".toLowerCase();
    }
    return null;
  }

  public static String getFarmAddress(String network) {
    if (ETH_NETWORK.equals(network)) {
      return FARM_TOKEN;
    } else if (BSC_NETWORK.equals(network)) {
      return BSC_FARM_TOKEN;
    } else if (MATIC_NETWORK.equals(network)) {
      return MATIC_FARM_TOKEN;
    }
    return null;
  }

  public static boolean isFullParsableLp(String address, String network) {
    return ContractConstantsV2.FULL_PARSABLE_UNI_PAIRS.get(network).containsKey(address.toLowerCase());
  }

  public static boolean isFullParsableLpAddressAndDate(String address, int date,
      String network) {
    return FULL_PARSABLE_UNI_PAIRS.get(network).containsKey(address.toLowerCase())
        && date > FULL_PARSABLE_UNI_PAIRS.get(network).get(address.toLowerCase());
  }

  public static boolean isParsableBancorTransaction(String address, int date,
      String network) {
    return PARSABLE_BANCOR_TRANSACTIONS.get(network).containsKey(address.toLowerCase())
        && date > PARSABLE_BANCOR_TRANSACTIONS.get(network).get(address.toLowerCase());
  }
}
