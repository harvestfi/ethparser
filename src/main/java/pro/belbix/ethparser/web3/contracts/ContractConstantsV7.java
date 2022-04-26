package pro.belbix.ethparser.web3.contracts;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.Map;
import pro.belbix.ethparser.model.TokenInfo;

public interface ContractConstantsV7 {
  String iPS_ADDRESS = "0x1571eD0bed4D987fe2b498DdBaE7DFA19519F651"
      .toLowerCase();
  String FARM_TOKEN = "0xa0246c9032bc3a600820415ae600c6388619a14d"
      .toLowerCase();
  String BANCOR_CONVERSION_ADDRESS = "0x2f9ec37d6ccfff1cab21733bdadede11c823ccb0";

  Map<TokenInfo, TokenInfo> COIN_PRICE_IN_OTHER_CHAIN = Map.of(
      // Denarius (DEN-0121) to Denarius BSC
      TokenInfo.builder()
          .address("0xf379CB529aE58E1A03E62d3e31565f4f7c1F2020".toLowerCase())
          .network(MATIC_NETWORK)
          .build(),
      TokenInfo.builder()
          .address("0xf6B53b4c982b9B7e87af9dc5c66C85117A5df303")
          .network(BSC_NETWORK)
          .build(),
      // Denarius (DEN-MAR22) to Denarius BSC
      TokenInfo.builder()
          .address("0xa286eeDAa5aBbAE98F65b152B5057b8bE9893fbB".toLowerCase())
          .network(MATIC_NETWORK)
          .build(),
      TokenInfo.builder()
          .address("0xf6B53b4c982b9B7e87af9dc5c66C85117A5df303")
          .network(BSC_NETWORK)
          .build(),
      // Aureus (AUR-FEB22) to Aureus
      TokenInfo.builder()
          .address("0x6Fb2415463e949aF08ce50F83E94b7e008BABf07".toLowerCase())
          .network(MATIC_NETWORK)
          .build(),
      TokenInfo.builder()
          .address("0xe8F6311A615b4E5f50bb2C6071c725518207337d")
          .network(BSC_NETWORK)
          .build(),
      // Aureus (AUR-APR22) to Aureus
      TokenInfo.builder()
          .address("0xBF06D9b11126B140788D842a6ed8dC7885C722B3".toLowerCase())
          .network(MATIC_NETWORK)
          .build(),
      TokenInfo.builder()
          .address("0xe8F6311A615b4E5f50bb2C6071c725518207337d")
          .network(BSC_NETWORK)
          .build()
  );

  Map<String, String> DEPLOYERS = Map.of(
      ETH_NETWORK, "0xf00dD244228F51547f0563e60bCa65a30FBF5f7f".toLowerCase(),
      BSC_NETWORK, "0xf00dd244228f51547f0563e60bca65a30fbf5f7f".toLowerCase(),
      MATIC_NETWORK, "0xf00dd244228f51547f0563e60bca65a30fbf5f7f".toLowerCase()
  );

  Map<String, Map<Long, String>> CONTROLLERS = Map.of(
      ETH_NETWORK, Map.of(
          10770087L, "0x222412af183BCeAdEFd72e4Cb1b71f1889953b1C".toLowerCase(),
          12652885L, "0x3cc47874dc50d98425ec79e647d83495637c55e3".toLowerCase()),
      BSC_NETWORK, Map.of(
          5990839L, "0x222412af183bceadefd72e4cb1b71f1889953b1c".toLowerCase()),
      MATIC_NETWORK,  Map.of(
          16612698L, "0x2ce34b1bb247f242f1d2a33811e01138968efbff".toLowerCase()
      )
  );

  Map<String, String> NOTIFY_HELPER = Map.of(
      ETH_NETWORK, "0xe20c31e3d08027f5aface84a3a46b7b3b165053c".toLowerCase(),
      BSC_NETWORK, "0xf71042c88458ff1702c3870f62f4c764712cc9f0".toLowerCase(),
      MATIC_NETWORK, "0xe85c8581e60d7cd32bbfd86303d2a4fa6a951dac".toLowerCase()
  );

  Map<String, Map<String, Integer>> PARSABLE_BANCOR_TRANSACTIONS = Map.of(
      ETH_NETWORK, Map.of(
          BANCOR_CONVERSION_ADDRESS, 10285676
      ),
      BSC_NETWORK, Map.of(),
      MATIC_NETWORK, Map.of()
  );
}
