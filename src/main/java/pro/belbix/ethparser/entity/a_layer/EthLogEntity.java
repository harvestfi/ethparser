package pro.belbix.ethparser.entity.a_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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

@Entity
@Table(name = "a_eth_log", indexes = {
    @Index(name = "idx_eth_log_block", columnList = "blockNumber"),
    @Index(name = "idx_eth_log_hash_log_id", columnList = "hash, logId")
})
@Data
@JsonInclude(Include.NON_NULL)
public class EthLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hash", referencedColumnName = "index")
    private EthHashEntity hash;
    private long logId;
    private int removed;
    private long transactionIndex;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "block_hash", referencedColumnName = "index")
    private EthHashEntity blockHash;
    private long blockNumber;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address", referencedColumnName = "index")
    private EthAddressEntity address;
    @Column(columnDefinition = "TEXT")
    private String data;
    private String type;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "first_topic", referencedColumnName = "index")
    private EthHashEntity firstTopic;
    @Column(columnDefinition = "TEXT")
    private String topics;

}
