package pro.belbix.ethparser.entity.profit;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "token_price")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class TokenPrice {
  @Id
  String id;
  Double value;

  public static String toId(String vaultAddress, String block, String network) {
    return String.join("_", vaultAddress, block, network);
  }
}
