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
    @Index(name = "idx_prices", columnList = "block")
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
  private String token;
  private Double tokenAmount;
  private String otherToken;
  private Double otherTokenAmount;
  private Double price;
  private Integer buy;
  private String source;
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
