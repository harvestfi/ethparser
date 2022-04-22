package pro.belbix.ethparser.entity.contracts;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "eth_vaults", indexes = {
    @Index(name = "idx_eth_vaults", columnList = "contract")
})
@Data
public class VaultEntity {

    @Id
//  TODO Can not save for HarvestVaultInfoTask, after return GeneratedValue annotation
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contract", unique = true)
    @Fetch(FetchMode.JOIN)
    private ContractEntity contract;
    private Long updatedBlock;

    // contract info
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private ContractEntity controller;
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private ContractEntity governance;
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private ContractEntity strategy;
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private ContractEntity underlying;
    private String name;
    private String symbol;
    private Long decimals;
    private Long underlyingUnit;
}
