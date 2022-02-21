package pro.belbix.ethparser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CovalenthqTransactionHistory {
  private CovalenthqTransactionHistoryItems data;

  @Data
  public static class CovalenthqTransactionHistoryItems {
    private List<CovalenthqTransactionHistoryItem> items;
    private CovalenthqTransactionHistoryPagination pagination;

    @Data
    public static class CovalenthqTransactionHistoryItem {
      @JsonProperty("block_signed_at")
      private LocalDateTime signedAt;
      @JsonProperty("block_height")
      private long blockHeight;
      @JsonProperty("tx_hash")
      private String transactionHash;
      @JsonProperty("from_address")
      private String fromAddress;
      @JsonProperty("to_address")
      private String toAddress;
      @JsonProperty("log_events")
      private List<CovalenthqTransactionHistoryItemLog> logs;

      @Data
      public static class CovalenthqTransactionHistoryItemLog {
        @JsonProperty("block_height")
        private long blockHeight;
        @JsonProperty("tx_hash")
        private String transactionHash;
        private CovalenthqTransactionHistoryItemLogDecode decoded;
        @JsonProperty("sender_contract_decimals")
        private int contractDecimal;

        @Data
        public static class CovalenthqTransactionHistoryItemLogDecode {
          private String name;
          private List<CovalenthqTransactionHistoryItemLogDecodeParam> params;

          @Data
          public static class CovalenthqTransactionHistoryItemLogDecodeParam {
            private String name;
            private String type;
            private boolean indexed;
            private boolean decoded;
            private String value;
          }
        }
      }
    }

    @Data
    public static class CovalenthqTransactionHistoryPagination {
      @JsonProperty("has_more")
      private boolean hasMore;
    }
  }
}
