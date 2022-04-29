package pro.belbix.ethparser.entity;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "harvest_vault_data")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HarvestVaultData {
  @Id
  String id;
  String vaultAddress;
  String rewardPool;
  String displayName;
  BigDecimal apy;
  BigDecimal tvl;
  BigDecimal totalSupply;
  String network;
}
