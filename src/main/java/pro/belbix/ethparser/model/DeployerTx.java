package pro.belbix.ethparser.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeployerTx extends EthTransaction {

  private DeployerActivityEnum type;
  private String methodName;

  public DeployerDTO toDto() {
    DeployerDTO deployerDTO = new DeployerDTO();
    deployerDTO.setId(this.getHash());
    deployerDTO.setIdx(this.getIdx());
    deployerDTO.setBlock(this.getBlock());
    deployerDTO.setToAddress(this.getToAddress());
    deployerDTO.setFromAddress(this.getFromAddress());
    deployerDTO.setValue(this.getValue());
    deployerDTO.setGasLimit(this.getGasLimit());
    deployerDTO.setGasPrice(this.getGasPrice());
    deployerDTO.setGasUsed(this.getGasUsed());
    deployerDTO.setMethodName(this.getMethodName());
    deployerDTO.setType(this.getType().name());
    deployerDTO.setConfirmed(this.isSuccess() ? 1 : 0);
    return deployerDTO;
  }
}
