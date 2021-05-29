package pro.belbix.ethparser.model;

import java.util.ArrayList;
import java.util.List;
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

  //contract info
  private Long strategyCreated;
  private String strategyName;

  // strategy info
  private String strategyUnderlyingAddress;
  private String strategyUnderlyingName;
  private String platform;
  private Double strategyUnderlyingPrice;
  private Double strategyBalance;
  private Double strategyBalanceUsd;

  private List<StratRewardInfo> rewardTokens = new ArrayList<>();

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

  public StratInfo(String strategyAddress, long block, String network) {
    this.strategyAddress = strategyAddress;
    this.block = block;
    this.network = network;
  }

  public boolean isPlatform(PlatformType platformType) {
    return PlatformType.valueOf(platform) == platformType;
  }

}
