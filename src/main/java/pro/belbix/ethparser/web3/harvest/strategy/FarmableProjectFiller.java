package pro.belbix.ethparser.web3.harvest.strategy;

import pro.belbix.ethparser.model.StratInfo;

public interface FarmableProjectFiller {

  void fillRewards(StratInfo stratInfo);

  void fillPoolInfo(StratInfo stratInfo);

  void fillRewardTokenAddress(StratInfo stratInfo);

  int lastClaimBlock(StratInfo stratInfo);

  void fillPoolAddress(StratInfo stratInfo);
}
