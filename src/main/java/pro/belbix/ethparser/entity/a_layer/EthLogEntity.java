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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "a_eth_log", indexes = {
//    @Index(name = "idx_eth_log_block", columnList = "blockNumber"),
//    @Index(name = "idx_eth_log_hash_log_id", columnList = "hash, logId")
})
@Data
@EqualsAndHashCode(exclude = {"tx", "blockNumber"})
@JsonInclude(Include.NON_NULL)
public class EthLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long logId;
    private int removed;
    private long transactionIndex;
    @Column(columnDefinition = "TEXT")
    private String data;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String topics;

    @ManyToOne
    @JoinColumn(name = "first_topic", referencedColumnName = "index")
    private EthHashEntity firstTopic;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EthTxEntity tx;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_number", referencedColumnName = "number")
    private EthBlockEntity blockNumber;

}
