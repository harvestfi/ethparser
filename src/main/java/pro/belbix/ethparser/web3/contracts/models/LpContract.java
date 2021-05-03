package pro.belbix.ethparser.web3.contracts.models;

import java.util.ArrayList;
import java.util.List;

public class LpContract extends SimpleContract {

  private final String keyToken;

  public LpContract(int createdOnBlock, String name, String keyToken, String address) {
    super(createdOnBlock, name, address);
    this.keyToken = keyToken;
  }

  public String getKeyToken() {
    return keyToken;
  }

  public static List<LpContract> createLpContracts(LpContract... contracts) {
    List<LpContract> result = new ArrayList<>();
    for (LpContract contract : contracts) {
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
