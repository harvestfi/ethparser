package pro.belbix.ethparser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@Data
public class HarvestVaultInfo {
  @JsonProperty("eth")
  private Map<String, HarvestVaultItemInfo> ethereumNetwork;
  @JsonProperty("matic")
  private Map<String, HarvestVaultItemInfo> maticNetwork;
  @JsonProperty("bsc")
  private Map<String, HarvestVaultItemInfo> bscNetwork;


  @Data
  public static class HarvestVaultItemInfo {
    private String vaultAddress;
    private String id;
    private String rewardPool;
    private String displayName;
  }
}
