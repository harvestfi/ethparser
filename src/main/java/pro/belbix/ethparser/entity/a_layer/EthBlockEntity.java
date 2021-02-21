package pro.belbix.ethparser.entity.a_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "a_eth_block", indexes = {
    @Index(name = "idx_eth_block_hash", columnList = "hash")
})
@Data
@JsonInclude(Include.NON_NULL)
public class EthBlockEntity implements DtoI {

    @Id
    private long number;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hash", referencedColumnName = "index", unique = true)
    private EthHashEntity hash;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_hash", referencedColumnName = "index")
    private EthHashEntity parentHash;
    private String nonce;
    private String sha3Uncles;
    // bloom indexes disabled due to no reason to hold
//    @Column(columnDefinition = "TEXT")
//    private String logsBloom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transactions_root", referencedColumnName = "index")
    private EthHashEntity transactionsRoot;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_root", referencedColumnName = "index")
    private EthHashEntity stateRoot;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipts_root", referencedColumnName = "index")
    private EthHashEntity receiptsRoot;
    private String author;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "miner", referencedColumnName = "index")
    private EthAddressEntity miner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mix_hash", referencedColumnName = "index")
    private EthHashEntity mixHash;
    private String difficulty;
    private String totalDifficulty;
    private String extraData;
    private String size;
    private String gasLimit;
    private String gasUsed;
    private String timestamp;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blockNumber",
        fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EthTxEntity> transactions;

}
