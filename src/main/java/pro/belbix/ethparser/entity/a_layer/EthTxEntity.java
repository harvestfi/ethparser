package pro.belbix.ethparser.entity.a_layer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
    @JoinColumn(name = "hash", referencedColumnName = "index")
    private EthHashEntity hash;
    private String nonce;
    private long blockNumber;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "block_hash", referencedColumnName = "index")
    private EthHashEntity blockHash;
    private long transactionIndex;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_address", referencedColumnName = "index")
    private EthAddressEntity fromAddress;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_address", referencedColumnName = "index")
    private EthAddressEntity toAddress;
    private String value;
    private long gasPrice;
    private long gas;
    @Column(columnDefinition = "TEXT")
    private String input;
    private String creates;
    private String publicKey;
    private String raw;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "r", referencedColumnName = "index")
    private EthHashEntity r;
    @JoinColumn(name = "s", referencedColumnName = "index")
    @ManyToOne(fetch = FetchType.LAZY)
    private EthHashEntity s;
    private long v;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "receipt")
    @Fetch(FetchMode.JOIN)
    private EthReceiptEntity receipt;

}
