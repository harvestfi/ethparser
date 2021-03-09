package pro.belbix.ethparser.entity.b_layer;

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
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.entity.contracts.ContractEntity;

@Entity
@Table(name = "b_contract_events")
@Data
@JsonInclude(Include.NON_NULL)
public class ContractEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "contract", referencedColumnName = "address")
    private ContractEntity contract;

    @ManyToOne
    private EthTxEntity tx;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractEvent",
        fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ContractStateEntity> states;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractEvent",
        fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ContractLogEntity> logs;


}
