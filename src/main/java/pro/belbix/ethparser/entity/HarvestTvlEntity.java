package pro.belbix.ethparser.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "harvest_tvl", indexes = {
    @Index(name = "idx_harvest_tvl", columnList = "calculateTime")
})
@Cacheable(false)
@Data
public class HarvestTvlEntity {

    @Id
    private String calculateHash;
    private Long calculateTime;
    private Double lastTvl;
    private int lastOwnersCount;
    private int lastAllOwnersCount;
    private Double lastPrice;
}
