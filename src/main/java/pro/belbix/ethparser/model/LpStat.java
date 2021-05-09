package pro.belbix.ethparser.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class LpStat {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private String coin1;
  private String coin1Address;
  private String coin2;
  private String coin2Address;
  private Double amount1;
  private Double amount2;
  private Double price1;
  private Double price2;

  public static String createJson(
      String firstCoinName,
      String firstCoinAddress,
      String secondCoinName,
      String secondCoinAddress,
      double firstCoinAmount,
      double secondCoinAmount,
      double firstCoinPrice,
      double secondCoinPrice
  ) {
    try {
      LpStat lpStat = new LpStat();
      lpStat.setCoin1(firstCoinName);
      lpStat.setCoin1Address(firstCoinAddress);
      lpStat.setCoin2(secondCoinName);
      lpStat.setCoin2Address(secondCoinAddress);
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
