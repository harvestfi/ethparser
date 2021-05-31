package pro.belbix.ethparser.entity.contracts;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "eth_strategies", indexes = {
    @Index(name = "idx_eth_strategies_contracts", columnList = "contract")
})
@Data
public class StrategyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "contract", unique = true)
  @Fetch(FetchMode.JOIN)
  private ContractEntity contract;
  private Long updatedBlock;

}
