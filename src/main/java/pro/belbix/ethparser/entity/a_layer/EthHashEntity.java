package pro.belbix.ethparser.entity.a_layer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "a_eth_hash", indexes = {
//    @Index(name = "idx_eth_index", columnList = "index")
})
@Data
public class EthHashEntity {

    @Id
    private String hash;
    @NaturalId
    @Column(unique = true)
    private Long index;

    public EthHashEntity(String hash) {
        this.hash = hash;
    }

    public EthHashEntity() {
    }
}
