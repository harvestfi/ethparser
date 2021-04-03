package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.Contract.createContracts;

import java.util.List;

public class BscPoolAddresses {

  private BscPoolAddresses() {
  }

  static final List<Contract> POOLS = createContracts(
      new Contract(5992750, "ST_PC_BUSD_BNB", "0xeaB819E2BE63FFC0dF64E7BBA4DDB3bDEa280310"),
      new Contract(5992754, "ST_PC_CAKE", "0x78963b538c4835E00648DF764029196700ea8eE9"),
      new Contract(5992762, "ST_PC_ETH_BNB", "0x221ED06024Ee4296fB544a44cfEDDf7c9f882cF3"),
      new Contract(5992765, "ST_PC_USDT_BNB", "0xf1121f56961C6DFD40355dDe61404D51b3f1c34E"),
      new Contract(5992769, "ST_PC_XVS_BNB", "0x063eB32430bD63f4144f3e87D3339E4D2a318C52"),
      new Contract(5992758, "ST_PC_CAKE_BNB", "0xD4bC6001937C6fF493e4bae3BA0F812799f86AB0"),
      new Contract(5992738, "ST_VENUS_XVS", "0x8709B440C0F4F6830a468c6f696D010e85c9510B"),
      new Contract(5992742, "ST_VENUS_VAI", "0xDA88e38735e75B58fee6ea4FC5Be576c1e22F6cd"),
      new Contract(5992726, "ST_VENUS_BETH", "0xb3b56c7BDc87F9DeB7972cD8b5c09329ce421F89"),
      new Contract(5992734, "ST_VENUS_ETH", "0x3331039530DD04B5DF06c2D226AC28E958BACc0f"),
      new Contract(5992746, "ST_VENUS_WBNB", "0xE83f395B076F9b95200f9bEC40f5E446599F4F06"),
      new Contract(5992730, "ST_VENUS_BTCB", "0xC6f39CFf6797baC5e29275177b6E8e315cF87D95")
  );

}
