package pro.belbix.ethparser.dto.v0;

import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "deployer_tx",
    indexes = {
        @Index(name = "idx_deployer_tx", columnList = "block"),
        @Index(name = "idx_deployer_tx_network", columnList = "network")
    }
)
@Cacheable(false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeployerDTO implements DtoI {

  @Id
  private String id;
  private long idx;
  private long block;
  private long blockDate;
  private String network;
  private String toAddress;
  private String fromAddress;
  private double value;
  private long gasLimit;
  private long gasUsed;
  private long gasPrice;
  private String methodName;
  private String type;
  private int confirmed;
  private String name;

  public String print() {
    return "Deployer tx "
        + Instant.ofEpochSecond(blockDate)
        + " " + network
        + " " + methodName
        + " " + type
        + " " + name
        ;
  }
}
