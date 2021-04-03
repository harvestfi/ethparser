package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;

@Entity
@Table(name = "b_log_hashes", indexes = {
    @Index(name = "b_log_hashes_method_name", columnList = "methodName"),
    @Index(name = "b_log_hashes_topic_hash", columnList = "topic_hash")
})
@Data
@JsonInclude(Include.NON_NULL)
public class LogHashEntity {

  @Id
  private String methodId;
  private String methodName;

  @ManyToOne
  @JoinColumn(name = "topic_hash", referencedColumnName = "idx")
  private EthHashEntity topicHash;
}
