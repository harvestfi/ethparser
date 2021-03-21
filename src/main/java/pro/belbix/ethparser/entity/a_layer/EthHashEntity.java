package pro.belbix.ethparser.entity.a_layer;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "a_eth_hash")
@Data
@EqualsAndHashCode
public class EthHashEntity implements Serializable {

    @Id
    private String hash;
    @NaturalId
    @Column(unique = true)
    private Long idx;

    public EthHashEntity(String hash) {
        this.hash = hash;
    }

    public EthHashEntity() {
    }
}
