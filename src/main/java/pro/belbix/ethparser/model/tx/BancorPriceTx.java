package pro.belbix.ethparser.model.tx;

import java.math.BigInteger;
import lombok.Data;
import pro.belbix.ethparser.web3.bancor.BancorOperationEnum;

@Data
public class BancorPriceTx {

  public static final String FARM = "FARM";
  public static final String BNT = "BNT";

  private String hash;
  private long logId;
  private long block;
  private BigInteger amountFarm = new BigInteger("0");
  private BigInteger amountBnt = new BigInteger("0");
  private Boolean farmAsSource;
  private BancorOperationEnum type;
  private String coin;
  private String coinAddress;
  private String otherCoin;
  private String otherCoinAddress;
  private Double lastPrice;
}
