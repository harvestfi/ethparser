package pro.belbix.ethparser.web3.harvest.strategy;

import pro.belbix.ethparser.entity.StratInfo;

public interface FarmableProjectFiller {

  void fillPoolAddress(StratInfo stratInfo);

  /**
   * <p>PoolSpecificUnderlying</p>
   * <p>PoolBalance</p>
   * <p>PoolTotalSupply</p>
   */
  void fillRewardTokenAddress(StratInfo stratInfo);

  void fillPoolInfo(StratInfo stratInfo);

  void fillRewards(StratInfo stratInfo);

  int lastClaimBlock(StratInfo stratInfo);
}
