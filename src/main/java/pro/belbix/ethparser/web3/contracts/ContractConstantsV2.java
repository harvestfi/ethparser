package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import java.util.Map;

public interface ContractConstantsV2 {

  Map<String, List<String>> EXCLUDE_ADDRESSES_FOR_PRICE_SHARE_BY_NETWORK = Map.of(
      ETH_NETWORK, List.of(
          "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50".toLowerCase(),
          "0x59258f4e15a5fc74a7284055a8094f58108dbd4f".toLowerCase() // POOL
      ),
      BSC_NETWORK, List.of(),
      MATIC_NETWORK, List.of()
  );

  Map<String, Map<String, Integer>> FULL_PARSABLE_UNI_PAIRS = Map.of(
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
}
