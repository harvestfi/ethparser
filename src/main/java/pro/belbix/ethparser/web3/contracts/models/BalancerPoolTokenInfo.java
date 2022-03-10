package pro.belbix.ethparser.web3.contracts.models;

import java.math.BigInteger;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalancerPoolTokenInfo {
  List<String> address;
  List<BigInteger> balances;
}
