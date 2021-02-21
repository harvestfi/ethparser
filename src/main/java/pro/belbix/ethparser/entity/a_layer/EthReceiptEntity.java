package pro.belbix.ethparser.entity.a_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "a_eth_receipt", indexes = {
    @Index(name = "idx_eth_receipt_block", columnList = "blockNumber")
})
@Data
@JsonInclude(Include.NON_NULL)
public class EthReceiptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hash", referencedColumnName = "index", unique = true)
    private EthHashEntity hash;
    private String transactionIndex;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "block_hash", referencedColumnName = "index")
    private EthHashEntity blockHash;
    private String blockNumber;
    private String cumulativeGasUsed;
    private String gasUsed;
    private String contractAddress;
    private String root;
    private String status;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_address", referencedColumnName = "index")
    private EthAddressEntity fromAddress;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_address", referencedColumnName = "index")
    private EthAddressEntity toAddress;
    // bloom indexes disabled due to no reason to hold
//    @Column(columnDefinition = "TEXT")
//    private String logsBloom;
    private String revertReason;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<EthLogEntity> logs;

}
