package pro.belbix.ethparser.model.tx;

import java.math.BigInteger;
import lombok.Data;

@Data
public class BancorPriceTx {

  private String hash;
  private long logId;
  private long block;
  private BigInteger amountFarm = new BigInteger("0");
  private BigInteger amountBnt = new BigInteger("0");
  private Boolean farmAsSource;
  private String type;
  private String coin;
  private String coinAddress;
  private String otherCoin;
  private String otherCoinAddress;
  private Double lastPrice;
}
