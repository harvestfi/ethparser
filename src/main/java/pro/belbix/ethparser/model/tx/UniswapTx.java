package pro.belbix.ethparser.model.tx;

import java.math.BigInteger;
import lombok.Data;
import org.web3j.abi.datatypes.Address;

@Data
public class UniswapTx implements EthTransactionI {

  public static final String SWAP = "SWAP";
  public static final String ADD_LIQ = "ADD";
  public static final String REMOVE_LIQ = "REM";

  private String hash;
  private long logId;
  private String type;
  private String owner;
  private BigInteger block;
  private BigInteger amountIn = new BigInteger("0");
  private Address coinIn;
  private BigInteger amountOut = new BigInteger("0");
  private BigInteger amountEth = new BigInteger("0");
  private Address coinOut;
  private BigInteger liquidity;
  private boolean success = false;
  private boolean enriched;
  private Boolean buy;
  private Address[] allAddresses;
  private String coinAddress;
  private String lpAddress;
  private String methodName;
}
