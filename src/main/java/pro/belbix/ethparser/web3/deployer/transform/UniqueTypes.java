package pro.belbix.ethparser.web3.deployer.transform;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_V0_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.iPS_ADDRESS;

import java.util.Map;
import pro.belbix.ethparser.web3.contracts.ContractType;

public class UniqueTypes {

  public static final Map<String, ContractType> TYPES = Map.of(
      PS_ADDRESS, ContractType.VAULT,
      PS_V0_ADDRESS, ContractType.VAULT,
      iPS_ADDRESS, ContractType.VAULT
  );

}
