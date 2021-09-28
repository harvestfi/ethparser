package pro.belbix.ethparser.properties;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import org.springframework.stereotype.Component;

@Component
public class NetworkProperties {

  private final EthAppProperties ethAppProperties;
  private final BscAppProperties bscAppProperties;
  private final MaticAppProperties maticAppProperties;

  public NetworkProperties(EthAppProperties ethAppProperties,
      BscAppProperties bscAppProperties,
      MaticAppProperties maticAppProperties) {
    this.ethAppProperties = ethAppProperties;
    this.bscAppProperties = bscAppProperties;
    this.maticAppProperties = maticAppProperties;
  }

  public NetworkPropertiesI get(String network) {
    switch (network) {
      case ETH_NETWORK:
        return ethAppProperties;
      case BSC_NETWORK:
        return bscAppProperties;
      case MATIC_NETWORK:
        return maticAppProperties;
      default:
        throw new IllegalStateException("Unknown network " + network);
    }
  }

}
