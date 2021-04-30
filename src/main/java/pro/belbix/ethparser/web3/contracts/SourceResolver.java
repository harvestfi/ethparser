package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;
import pro.belbix.ethparser.web3.contracts.models.LpContract;
import pro.belbix.ethparser.web3.contracts.models.TokenContract;

@Service
@Log4j2
public class SourceResolver {

  // it is a spring service for further improvements
  // need to load contracts from deployer data

  List<SimpleContract> getVaults(String network) {
    if (ETH_NETWORK.equals(network)) {
      return EthVaultAddresses.VAULTS;
    } else if (BSC_NETWORK.equals(network)) {
      return BscVaultAddresses.VAULTS;
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  List<SimpleContract> getPools(String network) {
    if (ETH_NETWORK.equals(network)) {
      return EthPoolAddresses.POOLS;
    } else if (BSC_NETWORK.equals(network)) {
      return BscPoolAddresses.POOLS;
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  List<TokenContract> getTokens(String network) {
    if (ETH_NETWORK.equals(network)) {
      return EthTokenAddresses.TOKENS;
    } else if (BSC_NETWORK.equals(network)) {
      return BscTokenAddresses.TOKENS;
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

  List<LpContract> getLps(String network) {
    if (ETH_NETWORK.equals(network)) {
      return EthLpAddresses.LPS;
    } else if (BSC_NETWORK.equals(network)) {
      return BscLpAddresses.LPS;
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
  }

}
