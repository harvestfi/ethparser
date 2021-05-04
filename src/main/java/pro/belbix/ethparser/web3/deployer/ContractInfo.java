package pro.belbix.ethparser.web3.deployer;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.PlatformType;

@Data
public class ContractInfo {

  private final String address;
  private final long block;
  private final String network;
  private final ContractType contractType;

  private String name;
  private String underlyingAddress;
  private String underlyingName;
  private List<String> underlyingTokens = new ArrayList<>();
  private PlatformType platformType;

  public ContractInfo(String address, long block, String network,
      ContractType contractType) {
    this.address = address;
    this.block = block;
    this.network = network;
    this.contractType = contractType;
  }
}
