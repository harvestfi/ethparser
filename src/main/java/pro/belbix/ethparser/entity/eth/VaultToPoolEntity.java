package pro.belbix.ethparser.entity.eth;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "eth_vault_to_pool")
@Data
public class VaultToPoolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long blockStart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="vault_id", nullable=false)
    @Fetch(FetchMode.JOIN)
    private VaultEntity vault;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="pool_id", nullable=false)
    @Fetch(FetchMode.JOIN)
    private PoolEntity pool;

}
