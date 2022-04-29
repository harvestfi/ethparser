package pro.belbix.ethparser.web3.contracts;

import java.util.List;

public interface ContractConstantsV5 {
  List<String> ONE_DOLLAR_TOKENS = List.of(
      "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase(), //USDC
      "0xe9e7cea3dedca5984780bafc599bd69add087d56".toLowerCase(), //BUSD
      "0xdAC17F958D2ee523a2206206994597C13D831ec7".toLowerCase(), //USDT
      "0x0000000000085d4780B73119b644AE5ecd22b376".toLowerCase(), //TUSD
      "0x6B175474E89094C44Da98b954EedeAC495271d0F".toLowerCase(), //DAI
      "0x2791bca1f2de4661ed88a30c99a7a9449aa84174".toLowerCase(), //matic USDC
      "0xc2132d05d31c914a87c6611c10748aeb04b58e8f".toLowerCase(), //matic USDT
      "0xE840B73E5287865EEc17d250bFb1536704B43B21".toLowerCase(), //matic mUSD
      "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174".toLowerCase() //matic USDC PoS
  );
}
