package pro.belbix.ethparser.dto.v0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "rewards", indexes = {
    @Index(name = "idx_rewards", columnList = "blockDate"),
    @Index(name = "idx_rewards_network", columnList = "network"),
    @Index(name = "idx_rewards_vault_address", columnList = "vaultAddress")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardDTO implements DtoI {

  @Id
  private String id;
  private String vault;
  private String vaultAddress;
  private String poolAddress;
  private long block;
  private long blockDate;
  private String network;
  private double reward;
  private long periodFinish;
  private double apy;
  private double weeklyApy;
  private double tvl;
  private double farmBalance;
  private Integer isWeeklyReward;

}
