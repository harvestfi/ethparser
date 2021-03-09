package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;

@Entity
@Table(name = "b_log_hexes")
@Data
@JsonInclude(Include.NON_NULL)
public class LogHexEntity {

  @Id
  private String methodId;
  private String methodName;

  @ManyToOne
  @JoinColumn(name = "hash", referencedColumnName = "idx")
  private EthHashEntity topic;
}
