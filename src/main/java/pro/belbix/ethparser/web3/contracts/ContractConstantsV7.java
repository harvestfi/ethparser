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
}
