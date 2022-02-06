package pro.belbix.ethparser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class CovalenthqTransaction {
  private CovalenthqTransactionItems data;

  @Data
  public static class CovalenthqTransactionItems {
    private List<CovalenthqTransactionItem> items;

    @Data
    public static class CovalenthqTransactionItem {
      @JsonProperty("block_height")
      private long blockHeight;
    }
  }

}
