package pro.belbix.ethparser.entity.c_layer;

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
@Subselect("select * from vault_actions_view")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VaultActionsViewEntity implements ViewI {

  @Id
  private Long id;
  private String address;
  private Long blockNumber;
  private String blockHash;
  private String sourceName;
  private String network;
  private Long txId;
  private String txHash;
  private String funcName;
  private Integer logId;
  private String fromAdr;
  private String toAdr;
  private Double ftokenAmount;
  private String opType;
  private Double sharedPrice;
  private Double ftokenTotalSupply;
  private Double tvl;
  private String underlying;
  private String underlyingName;
  private Integer underlyingType;


}
