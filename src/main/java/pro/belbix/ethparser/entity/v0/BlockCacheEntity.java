package pro.belbix.ethparser.entity.v0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "block_cache", indexes = {
    @Index(name = "idx_block_cache", columnList = "blockDate")
})
public class BlockCacheEntity {

    @Id
    private long block;
    private long blockDate;

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public long getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(long blockDate) {
        this.blockDate = blockDate;
    }
}
