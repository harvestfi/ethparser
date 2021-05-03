package pro.belbix.ethparser.web3.contracts.models;

import java.util.ArrayList;
import java.util.List;

public class SimpleContract extends PureEthContractInfo {

  public SimpleContract(int createdOnBlock, String name, String address) {
    super(createdOnBlock, name, address);
  }

  public static List<SimpleContract> createContracts(SimpleContract... contracts) {
    List<SimpleContract> result = new ArrayList<>();
    for (SimpleContract contract : contracts) {
      if (result.stream().anyMatch(v -> v.getAddress().equals(contract.getAddress())
          || v.getName().equals(contract.getName()))) {
        System.out.println("Duplicate contract " + contract.getName());
        System.exit(-1);
      }
      result.add(contract);
    }
    return result;
  }
}
