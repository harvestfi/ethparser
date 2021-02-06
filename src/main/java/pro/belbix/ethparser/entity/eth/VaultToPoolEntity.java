package pro.belbix.ethparser.entity.eth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "eth_vault_to_pool")
@Data
public class VaultToPoolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="vault_id", nullable=false)
    private VaultEntity vault;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="pool_id", nullable=false)
    private PoolEntity pool;
    private Long blockStart;

}
