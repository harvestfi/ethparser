package pro.belbix.ethparser.entity.v0;

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
@Subselect("select * from harvest_tvl_material_view")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class HarvestTvlViewEntity implements ViewI {

  @Id
  private String calculateHash;
  private String network;
  private Long calculateTime;
  private Double lastTvl;
  private Integer lastOwnersCount;
  private Integer lastAllOwnersCount;
  private Double lastPrice;
}
