package pro.belbix.ethparser.model.tx;

import java.math.BigInteger;
import lombok.Data;
import org.web3j.abi.datatypes.Address;

@Data
public class HarvestTx implements EthTransactionI {

  private long logId;
  private String hash;
  private String methodName;
  private String owner;
  private BigInteger block;
  private String blockHash;
  private BigInteger amount;
  private BigInteger amountIn;
  private Address vault;
  private Address fToken;
  private Address[] addressFromArgs;
  private Address addressFromArgs1;
  private Address addressFromArgs2;
  private BigInteger[] intFromArgs;
  private BigInteger intFromArgs1;
  private BigInteger intFromArgs2;
  private boolean success = false;
  private boolean enriched;
  private boolean migration = false;

}
