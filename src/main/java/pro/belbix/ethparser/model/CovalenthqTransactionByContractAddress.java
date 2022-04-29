package pro.belbix.ethparser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CovalenthqTransactionByContractAddress {
  private CovalenthqTransactionByContractAddressData data;

  @Data
  public static class CovalenthqTransactionByContractAddressData {
    private CovalenthqTransactionByContractAddressDataItems items;

    @Data
    public static class CovalenthqTransactionByContractAddressDataItems {
      @JsonProperty("tx_hash")
      private String hash;
      private boolean successful;
      @JsonProperty("from_address")
      private String fromAddress;
      @JsonProperty("to_address")
      private String toAddress;

    }
  }
}
