package pro.belbix.ethparser.entity.a_layer;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "a_eth_log", indexes = {
    @Index(name = "idx_eth_log_tx_id_log_id", columnList = "tx_id, logId", unique = true),
    @Index(name = "idx_eth_log_address", columnList = "address"),
    @Index(name = "idx_eth_log_first_topic", columnList = "first_topic"),
    @Index(name = "idx_eth_log_block_number", columnList = "block_number")
})
@Data
@EqualsAndHashCode(exclude = {"tx", "blockNumber"})
@JsonInclude(Include.NON_NULL)
@ToString(exclude = {"tx", "blockNumber"})
public class EthLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long logId;
    private int removed;
    private long transactionIndex;
    @Column(columnDefinition = "TEXT")
    private String data;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String topics;

    @ManyToOne
    @JoinColumn(name = "address", referencedColumnName = "idx")
    private EthAddressEntity address;

    @ManyToOne
    @JoinColumn(name = "first_topic", referencedColumnName = "idx")
    private EthHashEntity firstTopic;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private EthTxEntity tx;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_number", referencedColumnName = "number")
    private EthBlockEntity blockNumber;

}
