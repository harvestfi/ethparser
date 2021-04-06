package pro.belbix.ethparser.model;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Data
public class LpStat {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private String coin1;
  private String coin2;
  private Double amount1;
  private Double amount2;
  private Double price1;
  private Double price2;

  public static String createJson(String lpHash,
      double firstCoinAmount,
      double secondCoinAmount,
      double firstCoinPrice,
      double secondCoinPrice
  ) {
    ContractUtils contractUtils = ContractUtils.getInstance(ETH_NETWORK);
    try {
      Tuple2<String, String> lpTokens = contractUtils.tokenAddressesByUniPairAddress(lpHash);
      LpStat lpStat = new LpStat();
      lpStat.setCoin1(contractUtils.getNameByAddress(lpTokens.component1()).orElse("unknown"));
      lpStat.setCoin2(contractUtils.getNameByAddress(lpTokens.component2()).orElse("unknown"));
      lpStat.setAmount1(firstCoinAmount);
      lpStat.setAmount2(secondCoinAmount);
      lpStat.setPrice1(firstCoinPrice);
      lpStat.setPrice2(secondCoinPrice);
      return OBJECT_MAPPER.writeValueAsString(lpStat);
    } catch (JsonProcessingException ignored) {
    }
    return null;
  }
}
