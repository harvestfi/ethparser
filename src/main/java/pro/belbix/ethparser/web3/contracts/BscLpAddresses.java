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
      new LpContract(1066604, "PC_BTCB_WBNB", "BTCB", "0x7561eee90e24f3b348e1087a005f78b4c8453524"),
      new LpContract(4739606, "PC_BUSD_EGG", "EGG", "0x19e7cbecdd23a16dfa5573df54d98f7caae03019"),
      new LpContract(1296446, "PC_DAI_BUSD", "DAI", "0x3ab77e40340ab084c3e23be8e5a6f7afed9d41dc"),
      new LpContract(2261748, "PC_USDC_BUSD", "USDC", "0x680dd100e4b394bda26a59dd5c119a391e747d18"),
      new LpContract(3442599, "PC_BDO_BUSD", "BDO", "0xc5b0d73a7c0e4eaf66babf7ee16a2096447f7ad6"),
      new LpContract(3493154, "PC_SBDO_BUSD", "SBDO", "0xa0718093baa3e7aae054eed71f303a4ebc1c076f"),
      new LpContract(5099757, "ONEINCH_BNB_ONEINCH", "ONEINCH",
          "0xdaF66c0B7e8E2FC76B15B07AD25eE58E04a66796"),
      new LpContract(5908100, "ONEINCH_ONEINCH_RENBTC", "RENBTC",
          "0xe3f6509818ccf031370bB4cb398EB37C21622ac4"),
      new LpContract(3411917, "PC_BDO_WBNB", "", "0x74690f829fec83ea424ee1F1654041b2491A7bE9"),
      new LpContract(4739700, "PC_WBNB_EGG", "", "0xd1B59D11316E87C3a0A069E80F590BA35cD8D8D3")
  );

}
