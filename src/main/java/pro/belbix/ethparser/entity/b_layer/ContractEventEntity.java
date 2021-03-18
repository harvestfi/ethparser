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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;

@Entity
@Table(name = "b_contract_events",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"block", "contract"})
    })
@Data
@JsonInclude(Include.NON_NULL)
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractEvent",
        fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ContractTxEntity> txs;

}
