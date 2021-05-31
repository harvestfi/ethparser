package pro.belbix.ethparser.entity;

import java.util.ArrayList;
import java.util.List;
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
import pro.belbix.ethparser.model.StratRewardInfo;
import pro.belbix.ethparser.web3.deployer.transform.PlatformType;

@Entity
@Table(name = "strat_info", indexes = {
    @Index(name = "idx_strat_info", columnList = "block"),
    @Index(name = "idx_strat_info_network", columnList = "network"),
    @Index(name = "idx_strat_info_stadr", columnList = "strategyAddress"),
    @Index(name = "idx_strat_info_source_vadr", columnList = "vaultAddress")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StratInfo {

  @Id
  private String id;
  private String strategyAddress;
  private long block;
  private Long blockDate;
  private String network;

  //contract info
  private Long strategyCreated;
  private Long strategyCreatedDate;
  private String strategyName;
  private String vaultAddress;

  // strategy info
  private String strategyUnderlyingAddress;
  private String strategyUnderlyingName;
  private String platform;
  private Double strategyUnderlyingPrice;
  private Double strategyBalance;
  private Double strategyBalanceUsd;

  @Transient
  private List<StratRewardInfo> rewardTokens = new ArrayList<>();
  @Column(columnDefinition = "TEXT")
  private String rewardTokensRaw;

  // pool can be a token or gauge or another governance contract
  private String poolAddress;
  private Double poolBalance;
  private Double poolTotalSupply;
  private String poolSpecificUnderlying;
  private String poolExtraInfo1;
  private String poolExtraInfo2;
  private String poolExtraInfo3;

  // how much we invested
  private Double percentOfPool;
  private Double percentOfInvested;

  private Long rewardPeriod;
  private Double apr;
  private Double apy;

  public StratInfo(String strategyAddress, long block, long blockDate, String network) {
    this.strategyAddress = strategyAddress;
    this.block = block;
    this.blockDate = blockDate;
    this.network = network;
    this.id = network + "_" + strategyAddress + "_" + block;
  }

  public boolean isPlatform(PlatformType platformType) {
    return PlatformType.valueOf(platform) == platformType;
  }

}
