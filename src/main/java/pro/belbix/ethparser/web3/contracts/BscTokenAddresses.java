package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.TokenContract.createTokenContracts;

import java.util.List;

public class BscTokenAddresses {

  private BscTokenAddresses() {
  }

  static final List<TokenContract> TOKENS = createTokenContracts(
      new TokenContract(124241, "BUSD", "0xe9e7cea3dedca5984780bafc599bd69add087d56").addLp(0, ""),
      new TokenContract(326031, "ETH", "0x2170Ed0880ac9A755fd29B2688956BD959F933F8").addLp(0, ""),
      new TokenContract(176416, "USDT", "0x55d398326f99059ff775485246999027b3197955").addLp(0, ""),
      new TokenContract(858561, "XVS", "0xcF6BB5389c92Bdda8a3747Ddb454cB7a64626C63").addLp(0, ""),
      new TokenContract(693963, "CAKE", "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82").addLp(0, ""),
      new TokenContract(2471342, "VAI", "0x4bd17003473389a42daf6a0a729f6fdb328bbbd7").addLp(0, ""),
      new TokenContract(3319412, "BETH", "0x250632378E573c6Be1AC2f97Fcdf00515d0Aa91B").addLp(0, ""),
      new TokenContract(149268, "WBNB", "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c").addLp(0, ""),
      new TokenContract(123969, "BTCB", "0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c").addLp(0, "")
  );
}
