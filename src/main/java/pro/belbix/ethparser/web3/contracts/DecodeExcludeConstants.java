package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import java.util.Map;

public interface DecodeExcludeConstants {
  Map<String, List<String>> DECODE_UNISWAP_V3_EVENT = Map.of(
      ETH_NETWORK, List.of(
          "0xC1aa3966008ef13B9dD2867D41cA21d9C42932A1".toLowerCase(),
          "0xf301aF77793322Bbd6C9e7fa4465499cd2bDecDE".toLowerCase(),
          "0x0a1AB972612489a1a362f42559bcd281FBEc0786".toLowerCase(),
          "0x5c49E0386215077d1A3eCc425CC30ce34Ec08B60".toLowerCase(),
          "0xEA46CfcB43D5274991344cF6F56765e39A7Eae1a".toLowerCase(),
          "0xc53DaB6fDD18AF6CD5cF37fDE7C941d368f8664f".toLowerCase(),
          "0x65383Abd40f9f831018dF243287F7AE3612c62AC".toLowerCase(),
          "0xadB16dF01b9474347E8fffD6032360D3B54627fB".toLowerCase(),
          "0x2357685B07469eE80A389819C7A41edCD70cd88C".toLowerCase(),
          "0x7Fb7E4aE3a174E24e6Fd545BbF6E9fC5a14162Cc".toLowerCase(),
          "0xc3426599Ec933FbF657ee44b53e7f01d83Be1f63".toLowerCase(),
          "0x45A78dEbfb4d9E94836dC1680d7FAf32b3994a83".toLowerCase(),
          "0x0b4C4EA418Cd596B1204C0dd07E419707149C7C6".toLowerCase(),
          "0xC74075F5c9aD58C655a6160bA955B4aCD5dE8d0B".toLowerCase(),
          "0x3b2ED6013f961404AbA5a030e20A2AceB486832d".toLowerCase(),
          "0xe29385F6B90F25082972B75ccBC69900cE8A176A".toLowerCase(),
          "0x3F16b084Ff94c8a3f5A1b60834046f1febD15595".toLowerCase(),
          "0xd82964732cF95F904FD814511Be08b86d213232E".toLowerCase(),
          "0xeC665d477812C11Bf163841C83322FB4743D1Cfa".toLowerCase(),
          "0x8e1de189195a76baF8871f70AdcD090aD06a0B58".toLowerCase(),
          "0x6bA287890264cFdAb98100E9855b1423328269D2".toLowerCase(),
          "0x50dCcf8F83CCE8aA9168637c2Ec0114ae934F6d1".toLowerCase(),
          "0x04EdB1420A01547944eA57bBd4EBeBAE04ac116b".toLowerCase(),
          "0x25642078C595A7078f150e5E0486364077aE9eBB".toLowerCase(),
          "0x503Ea79B73995Cf0C8d323C17782047ED5cC72B2".toLowerCase(),
          "0xC905ccc1a1EC21C8bbE0c0b53d3D048D9055D4bB".toLowerCase(),
          "0x970CC1E0Bdb3B29a6A12BDE1954A8509acbC9158".toLowerCase(),
          "0x8137ac6dF358fe2D0DFbB1b5aA87C110950A16Cd".toLowerCase(),
          "0xFb387177fF9Db15294F7Aebb1ea1e941f55695bc".toLowerCase(),
          "0x1851A8fA2ca4d8Fb8B5c56EAC1813Fd890998EFc".toLowerCase(),
          "0x7095b06C02B66e4133F7B4b078B2720CB4437408".toLowerCase(),
          "0x744F77705749541926294f7b35A63f8691374640".toLowerCase(),
          "0x41d0Ab2Bf4E8Da44FEE38232E56b089f3Bb00587".toLowerCase()
          ),
      BSC_NETWORK, List.of(),
      MATIC_NETWORK, List.of()
  );

  Map<String, List<String>> DECODE_ONLY_TOPICS = Map.of(
      ETH_NETWORK, List.of(
          // iFARM
          "0x1571eD0bed4D987fe2b498DdBaE7DFA19519F651".toLowerCase()
      ),
      BSC_NETWORK, List.of(),
      MATIC_NETWORK, List.of()
  );
}
