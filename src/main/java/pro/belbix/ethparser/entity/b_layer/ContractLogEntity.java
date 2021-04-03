package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;

@Entity
@Table(name = "b_contract_logs", indexes = {
    @Index(name = "b_contract_logs_contract_tx_id_log_idx",
        columnList = "contract_tx_id, logIdx", unique = true),
    @Index(name = "b_contract_logs_address", columnList = "address"),
    @Index(name = "b_contract_logs_topic", columnList = "topic")
})
@Data
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(exclude = {"contractTx"})
@ToString(exclude = {"contractTx"})
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ContractLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private long logIdx;
  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private String logs;

  @ManyToOne
  @JoinColumn(name = "address", referencedColumnName = "idx")
  private EthAddressEntity address;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ContractTxEntity contractTx;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "topic", referencedColumnName = "methodId")
  private LogHashEntity topic;
}
