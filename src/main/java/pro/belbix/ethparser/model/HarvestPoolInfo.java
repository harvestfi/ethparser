package pro.belbix.ethparser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HarvestPoolInfo {
  @JsonProperty("eth")
  private List<HarvestPoolItemInfo> ethereumNetwork;
  @JsonProperty("matic")
  private List<HarvestPoolItemInfo> maticNetwork;
  @JsonProperty("bsc")
  private List<HarvestPoolItemInfo> bscNetwork;

  @Data
  @ToString
  public static class HarvestPoolItemInfo {
    private String contractAddress;
    private String type;
    private String id;
  }
}
