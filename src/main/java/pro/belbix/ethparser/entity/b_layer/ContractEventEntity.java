package pro.belbix.ethparser.entity.b_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;

@Entity
@Table(name = "b_contract_events", indexes = {
    @Index(name = "b_contract_events_block_contract", columnList = "block, contract", unique = true)
})
@Data
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(exclude = {"txs", "states", "block", "id"})
public class ContractEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract", referencedColumnName = "idx")
    private EthAddressEntity contract;

    @ManyToOne
    @JoinColumn(name = "block", referencedColumnName = "number")
    @JsonIgnore
    private EthBlockEntity block;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractEvent",
        fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ContractStateEntity> states;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinTable(name = "b_contract_event_to_tx",
        joinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "tx_id", referencedColumnName = "id"))
    private Set<ContractTxEntity> txs;

}
