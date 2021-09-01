package pro.belbix.ethparser.dto.v0;

import java.time.Instant;
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
@Table(name = "hard_work", indexes = {
    @Index(name = "idx_hard_work", columnList = "blockDate"),
    @Index(name = "idx_hard_work_vault", columnList = "vault"),
    @Index(name = "idx_hard_work_vault_address", columnList = "vaultAddress"),
    @Index(name = "idx_hard_work_2", columnList = "fullRewardUsd"),
    @Index(name = "idx_hard_work_network", columnList = "network")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HardWorkDTO implements DtoI {

  @Id
  protected String id;
  protected String vault;
  protected String vaultAddress;
  protected long block;
  protected long blockDate;
  protected String network;
  // don't use it, share price doesn't change for AutoStake strats
  // keep for compatibility and statistic
  protected double shareChange;
  /**
   * strategy + ps sum of earns
   */
  protected double fullRewardUsd;
  protected double farmBuyback;
  protected double fee;
  protected double farmBuybackEth;
  protected double gasUsed;
  protected double invested;
  protected double investmentTarget;
  protected double farmPrice;
  protected double ethPrice;
  protected Double profitSharingRate;
  protected Double buyBackRate;
  protected Integer autoStake;

  // todo move to another table or parse it from chain
  // ------------- GENERATED VALUES -----------------------
  protected long idleTime;
  protected double feeEth;
  protected double savedGasFeesSum;
  protected double savedGasFees;
  protected int poolUsers;
  protected int callsQuantity;
  protected double farmBuybackSum;
  protected double psApr;
  protected double psTvlUsd;
  protected double weeklyProfit;
  /**
   * {@link #fullRewardUsd} sum of all vaults for the last week
   */
  protected double weeklyAllProfit;
  @Deprecated
  protected double apr;
  @Deprecated
  protected double perc;
  protected double fullRewardUsdTotal;
  protected double allProfit;

  public String print() {
    return Instant.ofEpochSecond(blockDate) + " "
        + network + " "
        + vault + " "
        + fullRewardUsd + " "
        + fullRewardUsdTotal + " "
        + id;

  }
}
