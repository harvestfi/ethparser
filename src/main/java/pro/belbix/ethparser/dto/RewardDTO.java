package pro.belbix.ethparser.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "rewards", indexes = {
    @Index(name = "idx_rewards", columnList = "blockDate")
})
public class RewardDTO implements DtoI{

    @Id
    private String id;
    private String vault;
    private long block;
    private long blockDate;
    private double reward;
    private long periodFinish;

    @Override
    public String toString() {
        return "RewardDTO{" +
            "id='" + id + '\'' +
            ", vault='" + vault + '\'' +
            ", block=" + block +
            ", blockDate=" + blockDate +
            ", reward=" + reward +
            ", periodFinish=" + periodFinish +
            '}';
    }

    public long getPeriodFinish() {
        return periodFinish;
    }

    public void setPeriodFinish(long periodFinish) {
        this.periodFinish = periodFinish;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

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

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }
}
