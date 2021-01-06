package pro.belbix.ethparser.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "rewards", indexes = {
    @Index(name = "idx_rewards", columnList = "blockDate")
})
@Data
public class RewardDTO implements DtoI {

    @Id
    private String id;
    private String vault;
    private long block;
    private long blockDate;
    private double reward;
    private long periodFinish;
    private double apy;
    private double tvl;

}
