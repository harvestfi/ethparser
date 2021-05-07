package pro.belbix.ethparser.dto.v0;

import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "harvest_tx", indexes = {
    @Index(name = "idx_harvest_block_date", columnList = "blockDate"),
    @Index(name = "idx_harvest_method_name", columnList = "methodName"),
    @Index(name = "idx_harvest_network", columnList = "network"),
    @Index(name = "idx_harvest_vault", columnList = "vault"),
    @Index(name = "idx_harvest_vault_address", columnList = "vaultAddress")
})
@Cacheable(false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HarvestDTO implements DtoI {

  @Id
  private String id;
  private String hash;
  private Long block;
  private String network;
  private int confirmed;
  private Long blockDate;
  private String methodName;
  private String owner;
  private Double amount;
  private Double amountIn;
  private String vault;
  private String vaultAddress;
  private Double lastGas; // not historical data!
  private Double lastTvl;
  private Double lastUsdTvl;
  private Integer ownerCount;
  private Double sharePrice;
  private Long usdAmount;
  @Deprecated
  @Column(columnDefinition = "TEXT")
  private String prices;
  private String lpStat;
  @Deprecated
  private Double lastAllUsdTvl;
  private Double ownerBalance;
  private Double ownerBalanceUsd;
  private Integer allOwnersCount;
  private Integer allPoolsOwnersCount;
  private boolean migrated = false;
  private Double underlyingPrice;
  private String underlyingAddress;
  @Transient
  private HarvestDTO migration;
  private Double profit;
  private Double profitUsd;
  private Double totalAmount;  // fo PS only

  public String print() {
    return Instant.ofEpochSecond(blockDate) + " "
        + network + " "
        + methodName + " "
        + "usd: " + usdAmount + " "
        + "f: " + amount + " "
        + vault
        + " " + hash
        + " " + String.format("$%.0f", lastUsdTvl)
        + " profit: " + profit
        + " " + String.format("$%.4f", profitUsd);
  }
}
