package pro.belbix.ethparser.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "harvest_tvl", indexes = {
    @Index(name = "idx_harvest_tvl", columnList = "calculateTime")
})
@Cacheable(false)
public class HarvestTvlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private Long calculateTime;
    private Double lastTvl;
    private int lastOwnersCount;
    private String calculateHash;

    public String getCalculateHash() {
        return calculateHash;
    }

    public void setCalculateHash(String calculateHash) {
        this.calculateHash = calculateHash;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLastOwnersCount() {
        return lastOwnersCount;
    }

    public void setLastOwnersCount(int lastOwnersCount) {
        this.lastOwnersCount = lastOwnersCount;
    }

    public Long getCalculateTime() {
        return calculateTime;
    }

    public void setCalculateTime(Long time) {
        this.calculateTime = time;
    }

    public Double getLastTvl() {
        return lastTvl;
    }

    public void setLastTvl(Double value) {
        this.lastTvl = value;
    }
}
