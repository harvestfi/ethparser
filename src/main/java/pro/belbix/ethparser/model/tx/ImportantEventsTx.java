package pro.belbix.ethparser.model.tx;

import java.math.BigInteger;
import lombok.Data;

@Data
public class ImportantEventsTx implements EthTransactionI {

  private String hash;
  private String logId;
  private String vault;
  private String oldStrategy;
  private String newStrategy;
  private String methodName;
  private long block;
  private long blockDate;
  private BigInteger mintAmount = BigInteger.ZERO;

}
