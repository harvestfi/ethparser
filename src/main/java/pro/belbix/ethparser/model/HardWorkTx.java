package pro.belbix.ethparser.model;

import java.math.BigInteger;
import lombok.Data;

@Data
public class HardWorkTx {

  private String hash;
  private String logId;
  private String vault;
  private String strategy;
  private String methodName;
  private long block;
  private long blockDate;
  private BigInteger oldSharePrice;
  private BigInteger newSharePrice;
  private BigInteger profitAmount;
  private BigInteger feeAmount;
  private BigInteger reward;

}
