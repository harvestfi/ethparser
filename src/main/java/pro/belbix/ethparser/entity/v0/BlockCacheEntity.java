package pro.belbix.ethparser.entity.v0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "block_cache", indexes = {
    @Index(name = "idx_block_cache", columnList = "blockDate"),
    @Index(name = "idx_block_cache_net", columnList = "network")
})
@Data
public class BlockCacheEntity {

    @Id
    private long block;
    private long blockDate;
    private String network;
}
