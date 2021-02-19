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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "a_eth_tx", indexes = {
//    @Index(name = "idx_eth_tx_block", columnList = "block")
})
@Data
@JsonInclude(Include.NON_NULL)
public class EthTxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    private EthHashEntity hash;
    private String nonce;
    private long blockNumber;
    @ManyToOne(fetch = FetchType.EAGER)
    private EthHashEntity blockHash;
    private String transactionIndex;
    @ManyToOne(fetch = FetchType.EAGER)
    private EthAddressEntity fromAddress;
    @ManyToOne(fetch = FetchType.EAGER)
    private EthAddressEntity toAddress;
    private String value;
    private String gasPrice;
    private String gas;
    @Column(columnDefinition = "TEXT")
    private String input;
    private String creates;
    private String publicKey;
    private String raw;
    @ManyToOne(fetch = FetchType.LAZY)
    private EthHashEntity r;
    @ManyToOne(fetch = FetchType.LAZY)
    private EthHashEntity s;
    private long v;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hash")
    @Fetch(FetchMode.JOIN)
    private EthReceiptEntity receipt;

}
