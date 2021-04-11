package pro.belbix.ethparser.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum;

@Data
@EqualsAndHashCode
public class DeployerTx implements EthTransactionI {

  private String hash;
  private long idx;
  private long block;
  private String toAddress;
  private String fromAddress;
  private BigDecimal value;
  private BigInteger gasLimit;
  private BigInteger gasUsed;
  private BigInteger gasPrice;
  private String inputData;
  private boolean success;
  private DeployerActivityEnum type;
  private String methodName;

  public DeployerDTO toDto() {
    DeployerDTO deployerDTO = new DeployerDTO();
    deployerDTO.setId(this.getHash());
    deployerDTO.setIdx(this.getIdx());
    deployerDTO.setBlock(this.getBlock());
    if (this.getToAddress() != null) {
      deployerDTO.setToAddress(this.getToAddress().toLowerCase());
    }
    if (this.getFromAddress() != null) {
      deployerDTO.setFromAddress(this.getFromAddress().toLowerCase());
    }
    deployerDTO.setValue(this.getValue().doubleValue());
    deployerDTO.setGasLimit(this.getGasLimit().longValue());
    deployerDTO.setGasPrice(this.getGasPrice().longValue());
    deployerDTO.setGasUsed(this.getGasUsed().longValue());
    deployerDTO.setMethodName(this.getMethodName());
    deployerDTO.setType(this.getType().name());
    deployerDTO.setConfirmed(this.isSuccess() ? 1 : 0);
    return deployerDTO;
  }
}
