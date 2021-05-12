package pro.belbix.ethparser.web3.abi;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.MINIMUM_LIQUIDITY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.MOONISWAP_FACTORY_GOVERNANCE;

import org.springframework.stereotype.Service;

@Service
public class FunctionService {

  private final FunctionsUtils functionsUtils;

  public FunctionService(FunctionsUtils functionsUtils) {
    this.functionsUtils = functionsUtils;
  }

  public boolean isLp(String address, long block, String network) {
    try {
      return functionsUtils.callIntByName(MINIMUM_LIQUIDITY, address, block, network)
          .orElse(null) != null
          || functionsUtils
          .callAddressByName(MOONISWAP_FACTORY_GOVERNANCE, address, block, network)
          .orElse(null) != null;
    } catch (Exception ignored) {
    }
    return false;
  }
}
