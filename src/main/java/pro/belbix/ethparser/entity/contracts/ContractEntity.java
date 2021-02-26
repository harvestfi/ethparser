package pro.belbix.ethparser.entity.contracts;

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
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "eth_contracts", indexes = {
    @Index(name = "idx_eth_contracts_address", columnList = "address"),
    @Index(name = "idx_eth_contracts_name", columnList = "name")
})
@Data
public class ContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String address;
    private String name;
    private Long created;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type")
    @Fetch(FetchMode.JOIN)
    private ContractTypeEntity type;

//    @JsonIgnore
//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contract", fetch = FetchType.LAZY)
//    private PoolEntity pool;
//
//    @JsonIgnore
//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contract", fetch = FetchType.LAZY)
//    private VaultEntity vault;
//
//    @JsonIgnore
//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contract", fetch = FetchType.LAZY)
//    private UniPairEntity uniPair;
//
//    @JsonIgnore
//    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contract", fetch = FetchType.LAZY)
//    private TokenEntity token;

}
