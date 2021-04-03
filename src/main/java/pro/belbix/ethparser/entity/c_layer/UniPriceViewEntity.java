package pro.belbix.ethparser.entity.c_layer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import pro.belbix.ethparser.repositories.c_layer.ViewI;

@Entity
@Immutable
@Subselect("select * from uni_prices_view")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniPriceViewEntity implements ViewI {

  @Id
  private Long id;
  private String address;
  private Long blockNumber;
  private String blockHash;
  private String sourceName;
  private Long txId;
  private String txHash;
  private String funcName;
  private String sender;
  private String toAdr;
  private String amount0In;
  private String amount1In;
  private String amount0Out;
  private String amount1Out;
  private String keyTokenName;
  private String keyTokenAddress;
  private Double keyTokenAmount;
  private String otherTokenName;
  private String otherTokenAddress;
  private Double otherTokenAmount;
  private Integer isBuy;
  private Double lpTotalSupply;
  @Column(name = "lp_token_0_pooled")
  private Double lpToken0Pooled;
  @Column(name = "lp_token_1_pooled")
  private Double lpToken1Pooled;

}
