package pro.belbix.ethparser.entity.a_layer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "a_eth_address", indexes = {
    @Index(name = "idx_eth_address", columnList = "address")
})
@Data
public class EthAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true)
    private String address;

    public EthAddressEntity(String address) {
        this.address = address;
    }

    public EthAddressEntity() {
    }
}
