package pro.belbix.ethparser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StratRewardInfo {

  private final String address;
  private String name;
  private Double price;
  private Double amount;
  private Double amountUsd;

  public StratRewardInfo(String address) {
    this.address = address;
  }
}
