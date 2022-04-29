package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import java.util.Map;

public interface ContractConstantsV3 {

  Map<String, List<String>> PS_ADDRESSES_BY_NETWORK = Map.of(
      ETH_NETWORK, List.of(
          "0xd3093e3efbe00f010e8f5efe3f1cb5d9b7fe0eb1".toLowerCase(), //
          "0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C".toLowerCase(), // ST_PS
          "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase(), // FARM TOKEN
          "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50".toLowerCase(), // PS
          "0x59258F4e15A5fC74A7284055A8094F58108dbD4f".toLowerCase() // PS_V0
      ),
      BSC_NETWORK, List.of(
          "0x4B5C23cac08a567ecf0c1fFcA8372A45a5D33743".toLowerCase()
      ),
      MATIC_NETWORK, List.of(
          "0xab0b2ddb9c7e440fac8e140a89c0dbcbf2d7bbff".toLowerCase()
      )
  );

  Map<String, Map<Long, String>> ORACLES = Map.of(
      ETH_NETWORK,
      Map.of(12015724L, "0x48DC32eCA58106f06b41dE514F29780FFA59c279".toLowerCase(),
          12820106L, "0x1358c91D5b25D3eDAc2b7B26A619163d78f1717d".toLowerCase()),
      BSC_NETWORK,
      Map.of(6442627L, "0xE0e9F05054Ad3a2b6414AD13D768be91a84b47e8".toLowerCase(),
          6952687L, "0x643cF46eef91Bd878D9710ceEB6a7E6F929F2608".toLowerCase(),
          9142012L, "0x0E74303d0D18884Ce2CEb3670e72686645c4f38B".toLowerCase()),
      MATIC_NETWORK,
      Map.of(16841617L, "0x0E74303d0D18884Ce2CEb3670e72686645c4f38B".toLowerCase())
  );

  Map<String, Map<String, String>> ORACLES_BY_FACTORY = Map.of(
      ETH_NETWORK,
      Map.of("0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f".toLowerCase(),
          "0x48DC32eCA58106f06b41dE514F29780FFA59c279".toLowerCase()),
      BSC_NETWORK,
      Map.of("0xbcfccbde45ce874adcb698cc183debcf17952812".toLowerCase(),
          "0xE0e9F05054Ad3a2b6414AD13D768be91a84b47e8".toLowerCase(), // V1
          "0xca143ce32fe78f1f7019d7d551a6402fc5350c73".toLowerCase(),
          "0x643cF46eef91Bd878D9710ceEB6a7E6F929F2608".toLowerCase()) // V2
  );
}
