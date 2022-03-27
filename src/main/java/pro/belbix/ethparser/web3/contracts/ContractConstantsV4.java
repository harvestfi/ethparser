package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import java.util.Map;

public interface ContractConstantsV4 {
  Map<String, List<String>> EXCLUDE_JARVIS_STABLECOIN = Map.of(
      ETH_NETWORK, List.of(),
      BSC_NETWORK, List.of(),
      MATIC_NETWORK, List.of(
          "0x8ca194A3b22077359b5732DE53373D4afC11DeE3".toLowerCase(),  // jCAD
          "0x8343091F2499FD4b6174A46D067A920a3b851FF9".toLowerCase()  // jJPY
      )
  );
}
