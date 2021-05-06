package pro.belbix.ethparser.web3.contracts.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TokenContract extends SimpleContract {
  private final Map<String, Integer> lps = new HashMap<>();
  private boolean isCurve = false;

  public TokenContract(int createdOnBlock, String name, String address) {
    super(createdOnBlock, name, address);
  }

  public TokenContract addLp(int useFrom, String lpName) {
    lps.put(lpName, useFrom);
    return this;
  }

  public Map<String, Integer> getLps() {
    return lps;
  }

  public static List<TokenContract> createTokenContracts(TokenContract... contracts) {
    List<TokenContract> result = new ArrayList<>();
    for (TokenContract contract : contracts) {
      if (result.stream().anyMatch(v -> v.getAddress().equals(contract.getAddress())
          || v.getName().equals(contract.getName()))) {
        System.out.println("Duplicate contract " + contract.getName());
        System.exit(-1);
      }
      result.add(contract);
    }
    return result;
  }

  public void setCurve(boolean curve) {
    isCurve = curve;
  }

  public boolean isCurve() {
    return isCurve;
  }
}
