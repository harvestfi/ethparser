package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "b_contract_logs",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contract_tx_id", "logIdx"})
    })
@Data
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(exclude = {"contractTx"})
@ToString(exclude = {"contractTx"})
public class ContractLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private long logIdx;
  @Column(columnDefinition = "text") // json type will be a big headache
  private String logs;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ContractTxEntity contractTx;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "topic", referencedColumnName = "methodId")
  private LogHashEntity topic;
}
