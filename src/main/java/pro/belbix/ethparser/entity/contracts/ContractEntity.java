package pro.belbix.ethparser.entity.contracts;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "eth_contracts", indexes = {
    @Index(name = "idx_eth_contracts_address", columnList = "address"),
    @Index(name = "idx_eth_contracts_name", columnList = "name")
}, uniqueConstraints = {
    @UniqueConstraint(name = "eth_c_adr_net_pk", columnNames = {"address", "network"})
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractEntity {

  @Id
//  TODO Can not save for HarvestVaultInfoTask, after return GeneratedValue annotation
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String address;
  private String name;
  private Long created;
  private Long createdDate;
  private Long updated;
  private Long updatedDate;
  private int type = -1;
  private String network;
  private String underlying;

}
