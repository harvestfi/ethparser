package pro.belbix.ethparser.entity.eth;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "eth_uni_pairs", indexes = {
    @Index(name = "idx_eth_uni_pairs", columnList = "contract")
})
@Data
public class UniPairEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract", unique = true)
    private ContractEntity contract;
    private Long updatedBlock;

    // contract info
    private Long decimals;
    @ManyToOne
    private ContractEntity token0;
    @ManyToOne
    private ContractEntity token1;

}
