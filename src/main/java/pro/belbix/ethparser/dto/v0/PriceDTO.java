package pro.belbix.ethparser.dto.v0;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "prices", indexes = {
    @Index(name = "idx_prices", columnList = "block"),
    @Index(name = "idx_prices_network", columnList = "network"),
    @Index(name = "idx_prices_source", columnList = "source")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceDTO implements DtoI {

  @Id
  private String id;
  private Long block;
  private Long blockDate;
  private String network;
  private String token;
  private String tokenAddress;
  private Double tokenAmount;
  private String otherToken;
  private String otherTokenAddress;
  private Double otherTokenAmount;
  private Double price;
  private Integer buy;
  private String source;
  private String sourceAddress;
  private Double lpTotalSupply;
  private Double lpToken0Pooled;
  private Double lpToken1Pooled;

  public String print() {
    return Instant.ofEpochSecond(blockDate) + " "
        + source + " "
        + String.format("%.1f", price) + " "
        + buy + " "
        + id;
  }

}
