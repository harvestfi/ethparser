package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
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
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;

@Entity
@Table(name = "b_contract_txs",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contract_event_id", "tx_id"})
    })
@Data
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(exclude = {"contractEvent"})
@ToString(exclude = {"contractEvent"})
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ContractTxEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb")
  private String funcData;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "func_hash", referencedColumnName = "methodId")
  private FunctionHashEntity funcHash;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  private ContractEventEntity contractEvent;

  @ManyToOne(fetch = FetchType.EAGER)
  private EthTxEntity tx;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractTx",
      fetch = FetchType.EAGER, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Set<ContractLogEntity> logs;

}
