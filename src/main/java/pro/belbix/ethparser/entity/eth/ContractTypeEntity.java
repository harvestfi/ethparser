package pro.belbix.ethparser.entity.eth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "eth_contract_types")
@Data
public class ContractTypeEntity {

    @Id
    private Integer type;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "type", fetch = FetchType.LAZY)
    private List<ContractEntity> contracts;

    public enum Type {

        VAULT(0),
        POOL(1),
        UNI_PAIR(2),
        INFRASTRUCTURE(3),
        TOKEN(4)
        ;
        private final int id;

        Type(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

}
