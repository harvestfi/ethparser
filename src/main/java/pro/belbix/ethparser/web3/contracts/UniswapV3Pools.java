package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public interface UniswapV3Pools {

  Map<String, List<String>> EXCLUDED_STABLE_VAULTS = Map.of(
      ETH_NETWORK, List.of(
          "0x1851A8fA2ca4d8Fb8B5c56EAC1813Fd890998EFc".toLowerCase(),
          "0x7095b06C02B66e4133F7B4b078B2720CB4437408".toLowerCase()
      ),
      MATIC_NETWORK, List.of(),
      BSC_NETWORK, List.of()
  );


  // if you want to add new value, use contract 0x1f98431c8ad98523631ae4a59f267346ea31f984 getPool
  Map<String, Map<String, String>> VAULTS_TO_POOLS = Map.of(
      ETH_NETWORK, Map.ofEntries(
          new AbstractMap.SimpleEntry<>(
              "0xC1aa3966008ef13B9dD2867D41cA21d9C42932A1".toLowerCase(),
              "0xb6c52095F2df5967B2028724BC6389f83637f582"),
          new AbstractMap.SimpleEntry<>(
              "0xf301aF77793322Bbd6C9e7fa4465499cd2bDecDE".toLowerCase(),
              "0x8ad599c3A0ff1De082011EFDDc58f1908eb6e6D8"),
          new AbstractMap.SimpleEntry<>(
              "0x0a1AB972612489a1a362f42559bcd281FBEc0786".toLowerCase(),
              "0x4e68Ccd3E89f51C3074ca5072bbAC773960dFa36"),
          new AbstractMap.SimpleEntry<>(
              "0x5c49E0386215077d1A3eCc425CC30ce34Ec08B60".toLowerCase(),
              "0x4e68Ccd3E89f51C3074ca5072bbAC773960dFa36"),
          new AbstractMap.SimpleEntry<>(
              "0xEA46CfcB43D5274991344cF6F56765e39A7Eae1a".toLowerCase(),
              "0x4e68Ccd3E89f51C3074ca5072bbAC773960dFa36"),
          new AbstractMap.SimpleEntry<>(
              "0xc53DaB6fDD18AF6CD5cF37fDE7C941d368f8664f".toLowerCase(),
              "0x4e68Ccd3E89f51C3074ca5072bbAC773960dFa36"),
          new AbstractMap.SimpleEntry<>(
              "0x65383Abd40f9f831018dF243287F7AE3612c62AC".toLowerCase(),
              "0x7379e81228514a1D2a6Cf7559203998E20598346"),
          new AbstractMap.SimpleEntry<>(
              "0xadB16dF01b9474347E8fffD6032360D3B54627fB".toLowerCase(),
              "0xE2de090153403b0F0401142d5394da897630dCb7"),
          new AbstractMap.SimpleEntry<>(
              "0x2357685B07469eE80A389819C7A41edCD70cd88C".toLowerCase(),
              "0xCBCdF9626bC03E24f779434178A73a0B4bad62eD"),
          new AbstractMap.SimpleEntry<>(
              "0x7Fb7E4aE3a174E24e6Fd545BbF6E9fC5a14162Cc".toLowerCase(),
              "0x7Cf82E7b1Ee2a8A5D629F21a720740eCE2f8b7CD"),
          new AbstractMap.SimpleEntry<>(
              "0xc3426599Ec933FbF657ee44b53e7f01d83Be1f63".toLowerCase(),
              "0xfab26CFa923360fFC8ffc40827faeE5500988E9C"),
          new AbstractMap.SimpleEntry<>(
              "0x45A78dEbfb4d9E94836dC1680d7FAf32b3994a83".toLowerCase(),
              "0x8ad599c3A0ff1De082011EFDDc58f1908eb6e6D8"),
          new AbstractMap.SimpleEntry<>(
              "0x0b4C4EA418Cd596B1204C0dd07E419707149C7C6".toLowerCase(),
              "0x8ad599c3A0ff1De082011EFDDc58f1908eb6e6D8"),
          new AbstractMap.SimpleEntry<>(
              "0xC74075F5c9aD58C655a6160bA955B4aCD5dE8d0B".toLowerCase(),
              "0x8ad599c3A0ff1De082011EFDDc58f1908eb6e6D8"),
          new AbstractMap.SimpleEntry<>(
              "0x3b2ED6013f961404AbA5a030e20A2AceB486832d".toLowerCase(),
              "0x8ad599c3A0ff1De082011EFDDc58f1908eb6e6D8"),
          new AbstractMap.SimpleEntry<>(
              "0xe29385F6B90F25082972B75ccBC69900cE8A176A".toLowerCase(),
              "0xEe4Cf3b78A74aFfa38C6a926282bCd8B5952818d"),
          new AbstractMap.SimpleEntry<>(
              "0x3F16b084Ff94c8a3f5A1b60834046f1febD15595".toLowerCase(),
              "0x1d42064Fc4Beb5F8aAF85F4617AE8b3b5B8Bd801"),
          new AbstractMap.SimpleEntry<>(
              "0xd82964732cF95F904FD814511Be08b86d213232E".toLowerCase(),
              "0x3Fae0F474145A1A771F36bD188D1Cc7057A91B06"),
          new AbstractMap.SimpleEntry<>(
              "0xeC665d477812C11Bf163841C83322FB4743D1Cfa".toLowerCase(),
              "0x3Fae0F474145A1A771F36bD188D1Cc7057A91B06"),

          new AbstractMap.SimpleEntry<>(
              "0x8e1de189195a76baF8871f70AdcD090aD06a0B58".toLowerCase(),
              "0xeD1d49B6BadA7Bb00cC7C7351138BD959cf6B8ba"),
          new AbstractMap.SimpleEntry<>(
              "0x6bA287890264cFdAb98100E9855b1423328269D2".toLowerCase(),
              "0xD43b29AaF8aD938CfF4F478A0756defFfb329D07"),
          new AbstractMap.SimpleEntry<>(
              "0x50dCcf8F83CCE8aA9168637c2Ec0114ae934F6d1".toLowerCase(),
              "0x6BFe36d9a664289cE04F32E4F83f1566c4712F96"),
          new AbstractMap.SimpleEntry<>(
              "0x04EdB1420A01547944eA57bBd4EBeBAE04ac116b".toLowerCase(),
              "0x9359c87B38DD25192c5f2b07b351ac91C90E6ca7"),
          new AbstractMap.SimpleEntry<>(
              "0x25642078C595A7078f150e5E0486364077aE9eBB".toLowerCase(),
              "0xE73FE82f905E265d26e4a5A3D36d0D03bC4119Fc"),
          new AbstractMap.SimpleEntry<>(
              "0x503Ea79B73995Cf0C8d323C17782047ED5cC72B2".toLowerCase(),
              "0xC2e9F25Be6257c210d7Adf0D4Cd6E3E881ba25f8"),
          new AbstractMap.SimpleEntry<>(
              "0xC905ccc1a1EC21C8bbE0c0b53d3D048D9055D4bB".toLowerCase(),
              "0xC2e9F25Be6257c210d7Adf0D4Cd6E3E881ba25f8"),
          new AbstractMap.SimpleEntry<>(
              "0x970CC1E0Bdb3B29a6A12BDE1954A8509acbC9158".toLowerCase(),
              "0xC2e9F25Be6257c210d7Adf0D4Cd6E3E881ba25f8"),
          new AbstractMap.SimpleEntry<>(
              "0x8137ac6dF358fe2D0DFbB1b5aA87C110950A16Cd".toLowerCase(),
              "0xC2e9F25Be6257c210d7Adf0D4Cd6E3E881ba25f8"),
          new AbstractMap.SimpleEntry<>(
              "0xFb387177fF9Db15294F7Aebb1ea1e941f55695bc".toLowerCase(),
              "0xa63b490aA077f541c9d64bFc1Cc0db2a752157b5"),

          // exclude UST_USDT
          new AbstractMap.SimpleEntry<>(
              "0x1851A8fA2ca4d8Fb8B5c56EAC1813Fd890998EFc".toLowerCase(),
              ""),

          // exclued BUSD_USDC
          new AbstractMap.SimpleEntry<>(
              "0x7095b06C02B66e4133F7B4b078B2720CB4437408".toLowerCase(),
              ""),
          new AbstractMap.SimpleEntry<>(
              "0x2CE57694b635f6Ea0087A341654543E12b082538".toLowerCase(),
              "0x02DaA5fdd4B474c13A8D6D141471B87FBd2452cd")
      ),
      BSC_NETWORK, Map.of(),
      MATIC_NETWORK, Map.of()
  );
}
