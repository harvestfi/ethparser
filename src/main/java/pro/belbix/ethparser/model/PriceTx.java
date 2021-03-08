package pro.belbix.ethparser.model;

import java.math.BigInteger;
import lombok.Data;

@Data
public class PriceTx {

  private String hash;
  private long logId;
  private BigInteger block;
  private String blockHash;
  private BigInteger[] integers;
  private String[] addresses;
  private String methodName;
  private String source;

}
