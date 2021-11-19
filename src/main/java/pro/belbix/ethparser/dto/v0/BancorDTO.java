package pro.belbix.ethparser.dto.v0;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "bancor_tx")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BancorDTO implements DtoI {

  @Id
  private String id;
  private Long logId;
  private Long block;
  private Double amountFarm;
  private Double amountBnt;
  private Double priceBnt;
  private Double priceFarm;
  private Boolean farmAsSource; // 2 type of operations: FARM (source) -> BNT or BNT->FARM (target)

}
