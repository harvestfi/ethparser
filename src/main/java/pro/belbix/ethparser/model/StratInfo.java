package pro.belbix.ethparser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pro.belbix.ethparser.web3.deployer.transform.PlatformType;

@Data
@Builder
@AllArgsConstructor
public class StratInfo {

  private String id;
  private final String strategyAddress;
  private final long block;
  private final String network;

  // strategy info
  private String strategyUnderlyingAddress;
  private String strategyUnderlyingName;
  private String platform;
  private Double strategyUnderlyingPrice;
  private Double strategyBalance;
  private Double strategyBalanceUsd;

  private String rewardTokenAddress;
  private Double rewardTokenPrice;

  private String poolAddress;
  private Double poolBalance;
  private Double poolTotalSupply;
  private Double poolRewardsBalance;

  // how much we invested
  private Double percentOfPool;
  private Double percentOfInvested;

  private Double claimableTokens;
  private Double claimableTokensUsd;
  private Long rewardPeriod;
  private Double apr;
  private Double apy;

  public StratInfo(String strategyAddress, long block, String network) {
    this.strategyAddress = strategyAddress;
    this.block = block;
    this.network = network;
  }

  public boolean isPlatform(PlatformType platformType) {
    return PlatformType.valueOfName(strategyUnderlyingName) == platformType;
  }

}
