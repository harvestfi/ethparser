package pro.belbix.ethparser.web3.contracts.models;

import java.util.Objects;
import pro.belbix.ethparser.web3.contracts.ContractType;

public abstract class PureEthContractInfo {

  final int createdOnBlock;
  final String name;
  final String address;
  private ContractType contractType;
  private String network;

  protected PureEthContractInfo(int createdOnBlock, String name, String address) {
    this.createdOnBlock = createdOnBlock;
    this.name = name;
    this.address = address.toLowerCase();
  }

  public int getCreatedOnBlock() {
    return createdOnBlock;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public ContractType getContractType() {
    return contractType;
  }

  public void setContractType(ContractType contractType) {
    this.contractType = contractType;
  }

  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PureEthContractInfo that = (PureEthContractInfo) o;
    return createdOnBlock == that.createdOnBlock && Objects.equals(name, that.name)
        && Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdOnBlock, name, address);
  }

  @Override
  public String toString() {
    return "PureEthContractInfo{" +
        "createdOnBlock=" + createdOnBlock +
        ", name='" + name + '\'' +
        ", address='" + address + '\'' +
        ", contractType=" + contractType +
        ", network='" + network + '\'' +
        '}';
  }
}
