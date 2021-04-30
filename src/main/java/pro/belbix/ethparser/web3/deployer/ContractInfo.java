package pro.belbix.ethparser.web3.deployer;

import lombok.Data;
import pro.belbix.ethparser.web3.contracts.ContractType;

@Data
public class ContractInfo {

  private final String address;
  private final long block;
  private final String network;
  private final ContractType type;

  private String name;
  private String underlyingAddress;
  private String underlyingName;
  private String token0Adr;
  private String token1Adr;

  public ContractInfo(String address, long block, String network,
      ContractType type) {
    this.address = address;
    this.block = block;
    this.network = network;
    this.type = type;
  }
}
