package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.LpContract.createLpContracts;

import java.util.List;

public class BscLpAddresses {

  private BscLpAddresses() {
  }

  static final List<LpContract> LPS = createLpContracts(
      new LpContract(589414, "PC_WBNB_BUSD", "WBNB", "0x1b96b92314c44b159149f7e0303511fb2fc4774f"),
      new LpContract(706860, "PC_ETH_WBNB", "ETH", "0x70d8929d04b60af4fb9b58713ebcf18765ade422"),
      new LpContract(648115, "PC_USDT_WBNB", "USDT", "0x20bcc3b8a0091ddac2d0bc30f68e6cbb97de59cd"),
      new LpContract(1095074, "PC_WBNB_XVS", "XVS", "0x41182c32f854dd97ba0e0b1816022e0acb2fc0bb"),
      new LpContract(695058, "PC_CAKE_WBNB", "CAKE", "0xa527a61703d82139f8a06bc30097cc9caa2df5a6"),
      new LpContract(2530996, "PC_VAI_BUSD", "VAI", "0xff17ff314925dff772b71abdff2782bc913b3575"),
      new LpContract(3906347, "PC_ETH_BETH", "BETH", "0x99d865ed50d2c32c1493896810fa386c1ce81d91"),
      new LpContract(1066604, "PC_BTCB_WBNB", "BTCB", "0x7561eee90e24f3b348e1087a005f78b4c8453524")
  );

}
