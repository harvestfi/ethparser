package pro.belbix.ethparser.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitListResult {
  BigDecimal totalProfit;
  List<ProfitListResultItem> items;

  @Data
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ProfitListResultItem {
    String name;
    String contractAddress;
    BigDecimal profit;
  }
}
