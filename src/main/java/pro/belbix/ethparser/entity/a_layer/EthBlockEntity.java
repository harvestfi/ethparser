package pro.belbix.ethparser.entity.a_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pro.belbix.ethparser.dto.DtoI;

//@NamedEntityGraph(
//    name = "block-graph.all",
//    attributeNodes = {
//        @NamedAttributeNode("hash"),
//        @NamedAttributeNode("parentHash"),
//        @NamedAttributeNode("transactionsRoot"),
//        @NamedAttributeNode("stateRoot"),
//        @NamedAttributeNode("receiptsRoot"),
//        @NamedAttributeNode("miner"),
//        @NamedAttributeNode("mixHash"),
//        @NamedAttributeNode(value = "transactions", subgraph = "tx.all"),
//    },
//    subgraphs = {
//        @NamedSubgraph(
//            name = "tx.all",
//            attributeNodes = {
//                @NamedAttributeNode("hash"),
//                @NamedAttributeNode("blockHash"),
//                @NamedAttributeNode("fromAddress"),
//                @NamedAttributeNode("toAddress"),
//                @NamedAttributeNode("r"),
//                @NamedAttributeNode("s"),
//            }
//        ),
//    }
//)
@Entity
@Table(name = "a_eth_block", indexes = {
    @Index(name = "idx_eth_block_hash", columnList = "hash")
})
@Data
@JsonInclude(Include.NON_NULL)
public class EthBlockEntity implements DtoI {

    @Id
    private long number;
    private String nonce;
    private String author;
    private String difficulty;
    private String totalDifficulty;
    @Column(columnDefinition = "TEXT")
    private String extraData;
    private long size;
    private long gasLimit;
    private long gasUsed;
    private long timestamp;

    @ManyToOne
    @JoinColumn(name = "hash", referencedColumnName = "idx", unique = true)
    private EthHashEntity hash;

    @ManyToOne
    @JoinColumn(name = "parent_hash", referencedColumnName = "idx")
    private EthHashEntity parentHash;

    @ManyToOne
    @JoinColumn(name = "miner", referencedColumnName = "idx")
    private EthAddressEntity miner;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blockNumber",
        fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<EthTxEntity> transactions;

}
