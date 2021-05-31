package pro.belbix.ethparser.entity.v0;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import pro.belbix.ethparser.repositories.c_layer.ViewI;

@Entity
@Immutable
@Subselect("select * from harvest_tvl_view")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

//@Table(name = "harvest_tvl", indexes = {
//    @Index(name = "idx_harvest_tvl", columnList = "calculateTime")
//})

public class HarvestTvlViewEntity implements ViewI {

  @Id
  private String calculateHash;
  private String network;
  private Long calculateTime;
  private Double lastTvl;
  private int lastOwnersCount;
  private int lastAllOwnersCount;
  private Double lastPrice;
}
