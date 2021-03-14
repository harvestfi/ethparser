package pro.belbix.ethparser.dto.v0;

import static pro.belbix.ethparser.model.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.model.UniswapTx.REMOVE_LIQ;

import java.math.BigInteger;
import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Data;
import pro.belbix.ethparser.dto.DtoI;

@Entity
@Table(name = "uni_tx", indexes = {
    @Index(name = "idx_uni_tx", columnList = "blockDate")
})
@Cacheable(false)
@Data
public class UniswapDTO implements DtoI {

  @Id
  private String id;
  private String hash;
  private BigInteger block;
  private Long blockDate;
  private String owner;
  private boolean confirmed = false;
  private String type;
  private String coin;
  private double amount;
  private String otherCoin;
  private double otherAmount;
  private Double lastPrice;
  private Double lastGas;
  private String lp;
  private String methodName;
  // ---- ADDITIONAL STATISTIC INFO ----
  private Integer ownerCount;
  private Double psWeekApy;
  private Double psIncomeUsd;
  private Double ownerBalance;
  private Double ownerBalanceUsd;

  public void setPrice(double price) {
    double fee = (price * 0.003);
    if (isBuy()) {
      lastPrice = price - fee;
    } else if (isSell()) {
      lastPrice = price + fee;
    } else {
      lastPrice = price;
    }
  }

  public boolean isBuy() {
    return "BUY".equals(type);
  }

  public boolean isSell() {
    return "SELL".equals(type);
  }

  public boolean isLiquidity() {
    return ADD_LIQ.equals(type) || REMOVE_LIQ.equals(type);
  }

  public String print() {
    return Instant.ofEpochSecond(blockDate) + " "
        + type + " "
        + String.format("%.1f", amount) + " "
        + coin + " for "
        + otherCoin + " "
        + String.format("%.6f", otherAmount)
        + " " + hash
        + " " + lastPrice;
  }
}
