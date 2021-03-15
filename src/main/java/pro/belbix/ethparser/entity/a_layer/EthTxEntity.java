package pro.belbix.ethparser.entity.a_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

//@NamedEntityGraph(
//    name = "tx-graph.all",
//    attributeNodes = {
//        @NamedAttributeNode("hash"),
//        @NamedAttributeNode("blockHash"),
//        @NamedAttributeNode("fromAddress"),
//        @NamedAttributeNode("toAddress"),
//        @NamedAttributeNode("r"),
//        @NamedAttributeNode("s"),
//    }
//)
@Entity
@Table(name = "a_eth_tx")
@Data
@EqualsAndHashCode(exclude = {"logs", "blockNumber"})
@JsonInclude(Include.NON_NULL)
public class EthTxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nonce;
    private long transactionIndex;
    private String value;
    private long gasPrice;
    private long gas;
    @Column(columnDefinition = "TEXT")
    private String input;
    private String creates;
    private String publicKey;
    private String raw;

    // receipt info
    private long cumulativeGasUsed;
    private long gasUsed;
    private String contractAddress;
    private String root;
    private String status;
    private String revertReason;

    @ManyToOne
    @JoinColumn(name = "hash", referencedColumnName = "idx", unique = true)
    private EthHashEntity hash;

    @ManyToOne
    @JoinColumn(name = "from_address", referencedColumnName = "idx")
    private EthAddressEntity fromAddress;

    @ManyToOne
    @JoinColumn(name = "to_address", referencedColumnName = "idx")
    private EthAddressEntity toAddress;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_number", referencedColumnName = "number")
    private EthBlockEntity blockNumber;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tx",
        fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<EthLogEntity> logs;

}
