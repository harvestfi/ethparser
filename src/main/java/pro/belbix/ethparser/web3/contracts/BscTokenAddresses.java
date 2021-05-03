package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.models.TokenContract.createTokenContracts;

import java.util.List;
import pro.belbix.ethparser.web3.contracts.models.TokenContract;

public class BscTokenAddresses {

  private BscTokenAddresses() {
  }

  static final List<TokenContract> TOKENS = createTokenContracts(
      new TokenContract(124241, "BUSD", "0xe9e7cea3dedca5984780bafc599bd69add087d56"),
      new TokenContract(326031, "ETH", "0x2170Ed0880ac9A755fd29B2688956BD959F933F8")
          .addLp(0, "0x70d8929d04b60af4fb9b58713ebcf18765ade422"),
      new TokenContract(176416, "USDT", "0x55d398326f99059ff775485246999027b3197955")
          .addLp(0, "0x20bcc3b8a0091ddac2d0bc30f68e6cbb97de59cd"),
      new TokenContract(858561, "XVS", "0xcF6BB5389c92Bdda8a3747Ddb454cB7a64626C63")
          .addLp(0, "0x41182c32f854dd97ba0e0b1816022e0acb2fc0bb"),
      new TokenContract(693963, "CAKE", "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82")
          .addLp(0, "0xa527a61703d82139f8a06bc30097cc9caa2df5a6"),
      new TokenContract(2471342, "VAI", "0x4bd17003473389a42daf6a0a729f6fdb328bbbd7")
          .addLp(0, "0xff17ff314925dff772b71abdff2782bc913b3575"),
      new TokenContract(3319412, "BETH", "0x250632378E573c6Be1AC2f97Fcdf00515d0Aa91B")
          .addLp(0, "0x99d865ed50d2c32c1493896810fa386c1ce81d91"),
      new TokenContract(149268, "WBNB", "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c")
          .addLp(0, "0x1b96b92314c44b159149f7e0303511fb2fc4774f"),
      new TokenContract(123969, "BTCB", "0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c")
          .addLp(0, "0x7561eee90e24f3b348e1087a005f78b4c8453524"),
      new TokenContract(4737506, "EGG", "0xF952Fc3ca7325Cc27D15885d37117676d25BfdA6")
          .addLp(0, "PC_BUSD_EGG"),
      new TokenContract(325036, "DAI", "0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3")
          .addLp(0, "PC_DAI_BUSD"),
      new TokenContract(1477489, "USDC", "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d")
          .addLp(0, "PC_USDC_BUSD"),
      new TokenContract(3376455, "BDO", "0x190b589cf9Fb8DDEabBFeae36a813FFb2A702454")
          .addLp(0, "PC_BDO_BUSD"),
      new TokenContract(3491136, "SBDO", "0x0d9319565be7f53CeFE84Ad201Be3f40feAE2740")
          .addLp(0, "PC_SBDO_BUSD"),
      new TokenContract(5013255, "ONEINCH", "0x111111111117dC0aa78b770fA6A738034120C302")
          .addLp(0, "ONEINCH_BNB_ONEINCH"),
      new TokenContract(2132296, "RENBTC", "0xfCe146bF3146100cfe5dB4129cf6C82b0eF4Ad8c")
          .addLp(0, "ONEINCH_ONEINCH_RENBTC"),
      new TokenContract(5491276, "bFARM", "0x4B5C23cac08a567ecf0c1fFcA8372A45a5D33743"),
      new TokenContract(5935718, "EPS3", "0xaF4dE8E872131AE328Ce21D909C74705d3Aaf452"),
      new TokenContract(5936650, "EPS", "0xA7f552078dcC247C2684336020c03648500C6d9F"),
      new TokenContract(5246875, "BELT", "0xE0e514c71282b6f4e823703a39374Cf58dc3eA4f")
          .addLp(0, "PC_WBNB_BELT"),
      new TokenContract(5797193, "SWIRL", "0x52d86850bc8207b520340b7e39cdaf22561b9e56")
          .addLp(0, "PC_WBNB_SWIRL"),
      new TokenContract(5907924, "SPACE", "0x0abd3E3502c15ec252f90F64341cbA74a24fba06")
          .addLp(0, "PC_WBNB_SPACE"),
      new TokenContract(6046882, "ICE", "0xf16e81dce15B08F326220742020379B855B87DF9")
          .addLp(0, "PC_WBNB_ICE"),
      new TokenContract(6310794, "EPS_FUSDT", "0x373410A99B64B089DFE16F1088526D399252dacE"),
      new TokenContract(6570993, "EPS_BTC", "0x2a435Ecb3fcC0E316492Dc1cdd62d0F189be5640")
  );
}
