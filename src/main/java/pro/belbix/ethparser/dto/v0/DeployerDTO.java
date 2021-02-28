package pro.belbix.ethparser.dto.v0;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(
    name = "deployer_tx",
    indexes = {@Index(name = "idx_deployer_tx", columnList = "block")})
@Cacheable(false)
@Data
public class DeployerDTO implements DtoI {
  @Id private String id;
  private long idx;
  private long block;
  private long blockDate;
  private String toAddress;
  private String fromAddress;
  private BigDecimal value;
  private BigInteger gasLimit;
  private BigInteger gasUsed;
  private BigInteger gasPrice;
  private String methodName;
  private String type;
  private int confirmed;

  public String print() {
    return Instant.ofEpochSecond(blockDate) + " " + id;
  }
}
