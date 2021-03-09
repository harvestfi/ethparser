package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;

@Entity
@Table(name = "b_contract_logs")
@Data
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(exclude = {"contractEvent"})
public class ContractLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @Column(columnDefinition = "text") // json type will be a big headache
  private String logs;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ContractEventEntity contractEvent;

  @ManyToOne
  @JoinColumn(name = "topic", referencedColumnName = "methodId")
  private LogHexEntity topic;
}
