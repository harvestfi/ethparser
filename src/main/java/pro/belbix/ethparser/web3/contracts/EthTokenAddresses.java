package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.web3.contracts.TokenContract.createTokenContracts;

import java.util.List;

class EthTokenAddresses {

  private EthTokenAddresses() {
  }

  static final List<TokenContract> TOKENS = createTokenContracts(
      new TokenContract(0, "ZERO", "0x0000000000000000000000000000000000000000"),
      new TokenContract(10770079, "FARM", "0xa0246c9032bc3a600820415ae600c6388619a14d")
          .addLp(0, "UNI_LP_USDC_FARM").addLp(11609000, "UNI_LP_WETH_FARM"),
      new TokenContract(11348423, "BADGER", "0x3472A5A71965499acd81997a54BBA8D852C6E53d")
          .addLp(0, "UNI_LP_WBTC_BADGER"),
      new TokenContract(6082465, "USDC", "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"),
      new TokenContract(4719568, "ETH", "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
          .addLp(0, "UNI_LP_USDC_ETH"),
      new TokenContract(6766284, "WBTC", "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599")
          .addLp(0, "UNI_LP_ETH_WBTC"),
      new TokenContract(8928158, "DAI", "0x6b175474e89094c44da98b954eedeac495271d0f")
          .addLp(0, "UNI_LP_ETH_DAI"),
      new TokenContract(10867840, "TBTC", "0x8daebade922df735c38c80c7ebd708af50815faa")
          .addLp(0, "SUSHI_LP_WBTC_TBTC"),
      new TokenContract(4634748, "USDT", "0xdac17f958d2ee523a2206206994597c13d831ec7")
          .addLp(0, "UNI_LP_ETH_USDT"),
      new TokenContract(11000455, "IDX", "0x0954906da0Bf32d5479e25f46056d22f08464cab")
          .addLp(0, "UNI_LP_IDX_ETH"),
      new TokenContract(10830516, "DPI", "0x1494ca1f11d487c2bbe4543e90080aeba4ba3c2b")
          .addLp(0, "UNI_LP_ETH_DPI"),
      new TokenContract(11342057, "GRAIN", "0x6589fe1271A0F29346796C6bAf0cdF619e25e58e")
          .addLp(0, "UNI_LP_GRAIN_FARM"),
      new TokenContract(6988184, "TUSD", "0x0000000000085d4780b73119b644ae5ecd22b376")
          .addLp(0, "UNI_LP_TUSD_ETH"),
      new TokenContract(11355386, "BAC", "0x3449fc1cd036255ba1eb19d65ff4ba2b8903a69a")
          .addLp(0, "UNI_LP_BAC_DAI"),
      new TokenContract(11355392, "BAS", "0xa7ED29B253D8B4E3109ce07c80fc570f81B63696")
          .addLp(0, "UNI_LP_DAI_BAS"),
      new TokenContract(11548901, "MIC", "0x368B3a58B5f49392e5C9E4C998cb0bB966752E51")
          .addLp(0, "SUSHI_LP_MIC_USDT"),
      new TokenContract(11548905, "MIS", "0x4b4D2e899658FB59b1D518b68fe836B100ee8958")
          .addLp(0, "SUSHI_LP_MIS_USDT"),
      new TokenContract(11644470, "BSG", "0xb34ab2f65c6e4f764ffe740ab83f982021faed6d")
          .addLp(0, "UNI_LP_DAI_BSG"),
      new TokenContract(11644478, "BSGS", "0xa9d232cc381715ae791417b624d7c4509d2c28db")
          .addLp(0, "UNI_LP_DAI_BSGS"),
      new TokenContract(10722554, "ESD", "0x36f3fd68e7325a35eb768f1aedaae9ea0689d723")
          .addLp(0, "UNI_LP_ESD_USDC"),
      new TokenContract(11330242, "DSD", "0xbd2f0cd039e0bfcf88901c98c0bfac5ab27566e3")
          .addLp(0, "UNI_LP_USDC_DSD"),
      new TokenContract(5835251, "EURS", "0xdb25f211ab05b1c97d595516f45794528a807ad8")
          .addLp(0, "UNI_LP_USDC_EURS"),
      new TokenContract(11345219, "UST", "0xa47c8bf37f92abed4a126bda807a7b7498661acd")
          .addLp(0, "UNI_LP_UST_USDT"),
      new TokenContract(11345258, "MAAPL", "0xd36932143f6ebdedd872d5fb0651f4b72fd15a84")
          .addLp(0, "UNI_LP_MAAPL_UST"),
      new TokenContract(11345296, "MAMZN", "0x0cae9e4d663793c2a2a0b211c1cf4bbca2b9caa7")
          .addLp(0, "UNI_LP_MAMZN_UST"),
      new TokenContract(11345264, "MGOOGL", "0x59A921Db27Dd6d4d974745B7FfC5c33932653442")
          .addLp(0, "UNI_LP_MGOOGL_UST"),
      new TokenContract(11345271, "MTSLA", "0x21ca39943e91d704678f5d00b6616650f066fd63")
          .addLp(0, "UNI_LP_MTSLA_UST"),
      new TokenContract(6301780, "GUSD", "0x056fd409e1d7a124bd7017459dfea2f387b6d5cd")
          .addLp(0, "UNI_LP_GUSD_ETH"),
      new TokenContract(9476452, "YCRV", "0xdF5e0e81Dff6FAF3A7e52BA697820c5e32D806A8"),
      new TokenContract(10809467, "3CRV", "0x6c3F90f043a72FA612cbac8115EE7e52BDe6E490"),
      new TokenContract(9554031, "CRV_CMPND", "0x845838DF265Dcd2c412A1Dc9e959c7d08537f8a2"),
      new TokenContract(9567293, "CRV_BUSD", "0x3B3Ac5386837Dc563660FB6a0937DFAa5924333B"),
      new TokenContract(11010502, "CRV_USDN", "0x4f3E8F405CF5aFC05D68142F3783bDfE13811522"),
      new TokenContract(8174400, "HUSD", "0xdF574c24545E5FfEcb9a659c229253D4111d87e1")
          .addLp(0, "UNI_LP_USDT_HUSD"),
      new TokenContract(11466549, "CRV_UST", "0x94e131324b6054c0D789b190b2dAC504e4361b53"),
      new TokenContract(11005599, "CRV_GUSD", "0xD2967f45c4f384DEEa880F807Be904762a3DeA07"),
      new TokenContract(11466859, "CRV_EURS", "0x194eBd173F6cDacE046C53eACcE9B953F28411d1"),
      new TokenContract(11459238, "CRV_OBTC", "0x2fE94ea3d5d4a175184081439753DE15AeF9d614"),
      new TokenContract(11591275, "CRV_STETH", "0x06325440D014e39736583c165C2963BA99fAf14E"),
      new TokenContract(11497098, "CRV_AAVE", "0xfd2a8fa60abd58efe3eee34dd494cd491dc14900"),
      new TokenContract(10736094, "SUSHI", "0x6b3595068778dd592e39a122f4f5a5cf09c90fe2")
          .addLp(0, "SUSHI_LP_SUSHI_ETH"),
      new TokenContract(9076087, "HBTC", "0x0316eb71485b0ab14103307bf65a021042c6d380")
          .addLp(0, "UNI_LP_HBTC_ETH"),
      new TokenContract(8623122, "SBTC", "0xfE18be6b3Bd88A2D2A7f928d00292E7a9963CfC6"),
      new TokenContract(9736969, "RENBTC", "0xEB4C2781e4ebA804CE9a9803C67d0893436bB27D")
          .addLp(0, "UNI_LP_ETH_RENBTC"),
      new TokenContract(11818848, "BASv2", "0x106538cc16f938776c7c180186975bca23875287")
          .addLp(0, "UNI_LP_BASv2_DAI"),
      new TokenContract(11511393, "ONEINCH", "0x111111111117dC0aa78b770fA6A738034120C302")
          .addLp(0, "UNI_LP_ONEINCH_ETH"),
      new TokenContract(11733661, "KBTC", "0xE6C3502997f97F9BDe34CB165fBce191065E068f")
          .addLp(0, "UNI_LP_WBTC_KBTC"),
      new TokenContract(11733686, "KLON", "0xB97D5cF2864FB0D08b34a484FF48d5492B2324A0")
          .addLp(0, "UNI_LP_WBTC_KLON"),
      new TokenContract(11345280, "MNFLX", "0xc8d674114bac90148d11d3c1d33c61835a0f9dcd")
          .addLp(0, "UNI_LP_MNFLX_UST"),
      new TokenContract(11345288, "MTWTR", "0xedb0414627e6f1e3f082de65cd4f9c693d78cca9")
          .addLp(0, "UNI_LP_MTWTR_UST"),
      new TokenContract(11875162, "CRV_LINK", "0xcee60cFa923170e4f8204AE08B4fA6A3F5656F3a"),
      new TokenContract(11022769, "MUSE", "0xb6ca7399b4f9ca56fc27cbff44f4d2e4eef1fc81")
          .addLp(0, "UNI_LP_MUSE_ETH"),
      new TokenContract(12007358, "DUDES20", "0x2313e39841fb3809da0ff6249c2067ca84795846")
          .addLp(0, "UNI_LP_DUDES20_ETH"),
      new TokenContract(11841388, "MASK20", "0xc2bde1a2fa26890c8e6acb10c91cc6d9c11f4a73")
          .addLp(0, "UNI_LP_MASK20_ETH"),
      new TokenContract(11869798, "ROPE20", "0xb3cdc594d8c8e8152d99f162cf8f9edfdc0a80a2")
          .addLp(0, "UNI_LP_ROPE20_ETH"),
      new TokenContract(4281611, "LINK", "0x514910771af9ca656af840dff83e8264ecf986ca")
          .addLp(0, "UNI_LP_LINK_ETH"),
      new TokenContract(10151366, "CRV_RENWBTC", "0x49849C98ae39Fff122806C06791Fa73784FB3675"),
      new TokenContract(11869798, "MCAT20", "0xf961a1fa7c781ecd23689fe1d0b7f3b6cbb2f972")
          .addLp(0, "UNI_LP_MCAT20_ETH"),
      new TokenContract(10732326, "CRV_HBTC", "0xb19059ebb43466C323583928285a49f558E572Fd")
  );

}
