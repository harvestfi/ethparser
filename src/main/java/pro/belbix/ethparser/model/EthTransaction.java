package pro.belbix.ethparser.model;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class EthTransaction implements EthTransactionI {
  // Generic transaction values that should be needed by all sub-types.
  // Helps establish consistency for future transactions that are captured.

  private String hash;
  private long idx;
  private long block;
  private String toAddress;
  private String fromAddress;
  private BigDecimal value;
  private BigInteger gasLimit;
  private BigInteger gasUsed;
  private BigInteger gasPrice;
  private String inputData;
  private boolean success;
}
