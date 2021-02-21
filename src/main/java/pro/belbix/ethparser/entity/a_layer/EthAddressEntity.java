package pro.belbix.ethparser.entity.a_layer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "a_eth_address", indexes = {
//    @Index(name = "idx_eth_index", columnList = "index")
})
@Data
public class EthAddressEntity {

    @Id
    private String address;
    @NaturalId
    @Column(unique = true)
    private Long index;

    public EthAddressEntity(String address) {
        this.address = address;
    }

    public EthAddressEntity() {
    }
}
