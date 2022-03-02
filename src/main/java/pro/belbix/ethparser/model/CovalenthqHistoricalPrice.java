package pro.belbix.ethparser.model;

import java.util.List;
import lombok.Data;

@Data
public class CovalenthqHistoricalPrice {
  private CovalenthqHistoricalPriceData data;

  @Data
  public static class CovalenthqHistoricalPriceData {
    private List<CovalenthqHistoricalPriceDataPrice> prices;

    @Data
    public static class CovalenthqHistoricalPriceDataPrice {
      private Double price;
    }
  }
}
