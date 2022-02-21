package pro.belbix.ethparser.entity.profit;


import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "covalenthq_vault_tx")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CovalenthqVaultTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;
  String network;
  long block;
  String transactionHash;
  int contractDecimal;
  String contractAddress;
  String ownerAddress;
  long value;
  LocalDateTime signedAt;
  @Enumerated(EnumType.STRING)
  CovalenthqVaultTransactionType type;
}
