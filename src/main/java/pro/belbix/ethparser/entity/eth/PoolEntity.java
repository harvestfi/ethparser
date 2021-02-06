package pro.belbix.ethparser.entity.eth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "eth_pools", indexes = {
    @Index(name = "idx_eth_pools", columnList = "contract")
})
@Data
public class PoolEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract", unique = true)
    private ContractEntity contract;
    private Long updatedBlock;

    // contract info
    @ManyToOne
    @JoinColumn(name = "controller")
    private ContractEntity controller;
    @ManyToOne
    @JoinColumn(name = "governance")
    private ContractEntity governance;
    @ManyToOne
    @JoinColumn(name = "owner")
    private ContractEntity owner;
    @ManyToOne
    @JoinColumn(name = "lp_token")
    private ContractEntity lpToken;
    @ManyToOne
    @JoinColumn(name = "reward_token")
    private ContractEntity rewardToken;

//    @JsonIgnore
//    @OneToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name="pool_id")
//    private List<VaultToPoolEntity> vaultToPoolEntries;


}
