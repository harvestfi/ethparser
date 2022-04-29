package pro.belbix.ethparser.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class CovalenthqTransactionHistory {
  private CovalenthqTransactionHistoryItems data;

  @Data
  @JsonIgnoreProperties
  public static class CovalenthqTransactionHistoryItems {
    private List<CovalenthqTransactionHistoryItem> items;
    private CovalenthqTransactionHistoryPagination pagination;

    @Data
    @JsonIgnoreProperties
    public static class CovalenthqTransactionHistoryItem {
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
      @JsonIgnoreProperties
      public static class CovalenthqTransactionHistoryItemLog {
        @JsonProperty("block_signed_at")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime signedAt;
        @JsonProperty("block_height")
        private long blockHeight;
        @JsonProperty("tx_hash")
        private String transactionHash;
        @JsonProperty("sender_contract_decimals")
        private int contractDecimal;
        @JsonProperty(value = "raw_log_topics")
        private List<String> topics;
        @JsonProperty("raw_log_data")
        private String data;
        @JsonProperty("sender_address")
        private String senderAddress;
      }
    }

    @Data
    public static class CovalenthqTransactionHistoryPagination {
      @JsonProperty("has_more")
      private boolean hasMore;
    }
  }
}
