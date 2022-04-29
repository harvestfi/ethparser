package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ContractConstantsV8 {

  String QUICK_FACTORY_ADDRESS = "0x5757371414417b8C6CAad45bAeF941aBc7d3Ab32"
      .toLowerCase();
  String SUSHI_FACTORY_ADDRESS = "0xc35dadb65012ec5796536bd9864ed8773abc74c4"
      .toLowerCase();
  String PCS_V1_FACTORY_ADDRESS = "0xbcfccbde45ce874adcb698cc183debcf17952812"
      .toLowerCase();
  String PCS_V2_FACTORY_ADDRESS = "0xca143ce32fe78f1f7019d7d551a6402fc5350c73"
      .toLowerCase();
  String UNISWAP_FACTORY_ADDRESS = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"
      .toLowerCase();
  String SUSHISWAP_FACTORY_ADDRESS = "0xC0AEe478e3658e2610c5F7A4A2E1777cE9e4f2Ac"
      .toLowerCase();


  List<String> BANCOR_USDC_CONVERT_PATH = List.of(
      "0x1F573D6Fb3F13d689FF844B4cE37794d79a7FF1C".toLowerCase(), // BNT
      "0x874d8dE5b26c9D9f6aA8d7bab283F9A9c6f777f4".toLowerCase(), // Liquidity Pool (USDCBNT)
      "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48".toLowerCase()  // USDC
  );


  //Key tokens are used to find liquidity for any given token on Uni, Sushi and Curve.
  Map<String, Set<String>> KEY_TOKENS = Map.of(
      ETH_NETWORK, Set.of(
          "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48".toLowerCase(), //USDC
          "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2".toLowerCase(), //WETH
          "0x6B175474E89094C44Da98b954EedeAC495271d0F".toLowerCase(), //DAI
          "0xdAC17F958D2ee523a2206206994597C13D831ec7".toLowerCase(), //USDT
          "0xa47c8bf37f92aBed4A126BDA807A7b7498661acD".toLowerCase(), //UST
          "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599".toLowerCase(), //WBTC
          "0xdB25f211AB05b1c97D595516F45794528a807ad8".toLowerCase(), //EURS
          "0x514910771AF9Ca656af840dff83E8264EcF986CA".toLowerCase()  //LINK
      ),
      BSC_NETWORK, Set.of(
          "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d".toLowerCase(), //USDC
          "0x2170Ed0880ac9A755fd29B2688956BD959F933F8".toLowerCase(), //ETH
          "0x1AF3F329e8BE154074D8769D1FFa4eE058B1DBc3".toLowerCase(), //DAI
          "0x55d398326f99059fF775485246999027B3197955".toLowerCase(), //USDT
          "0x23396cF899Ca06c4472205fC903bDB4de249D6fC".toLowerCase(), //UST
          "0x7130d2A12B9BCbFAe4f2634d864A1Ee1Ce3Ead9c".toLowerCase(), //BTCB
          "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56".toLowerCase(), //BUSD
          "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c".toLowerCase(), //WBNB
          "0x4BD17003473389A42DAF6a0a729f6Fdb328BbBd7".toLowerCase(), //VAI
          "0x111111111117dC0aa78b770fA6A738034120C302".toLowerCase() //1INCH
      ),
      MATIC_NETWORK, Set.of(
          "0x2791bca1f2de4661ed88a30c99a7a9449aa84174".toLowerCase(), //USDC
          "0x7ceb23fd6bc0add59e62ac25578270cff1b9f619".toLowerCase(), //ETH
          "0x8f3cf7ad23cd3cadbd9735aff958023239c6a063".toLowerCase(), //DAI
          "0xc2132d05d31c914a87c6611c10748aeb04b58e8f".toLowerCase(), //USDT
          "0x692597b009d13c4049a947cab2239b7d6517875f".toLowerCase(), //UST
          "0xdab529f40e671a1d4bf91361c21bf9f0c9712ab7".toLowerCase(), //BUSD
          "0x1bfd67037b42cf73acf2047067bd4f2c47d9bfd6".toLowerCase() //WBTC
      )
  );

  Map<String, Map<String, Integer>> UNI_FACTORIES = Map.of(
      ETH_NETWORK, Map.of(
          UNISWAP_FACTORY_ADDRESS, 10000835,
          SUSHISWAP_FACTORY_ADDRESS, 10794229
      ), BSC_NETWORK, Map.of(
          PCS_V1_FACTORY_ADDRESS, 586851,
          PCS_V2_FACTORY_ADDRESS, 6809737
      ), MATIC_NETWORK, Map.of(
          QUICK_FACTORY_ADDRESS, 4931780,
          SUSHI_FACTORY_ADDRESS, 11333218
      )
  );
}
