package pro.belbix.ethparser.dto;

import static pro.belbix.ethparser.model.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.model.UniswapTx.REMOVE_LIQ;

import java.math.BigInteger;
import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "uni_tx", indexes = {
    @Index(name = "idx_uni_tx", columnList = "blockDate")
})
@Cacheable(false)
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
    // ---- ADDITIONAL STATISTIC INFO ----
    private Integer ownerCount;
    private Double psWeekApy;
    private Double psIncomeUsd;

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

    @Override
    public String toString() {
        return "TransactionDTO{" +
            "type='" + type + '\'' +
            ", coin='" + coin + '\'' +
            ", amount=" + amount +
            ", otherCoin='" + otherCoin + '\'' +
            ", otherAmount=" + otherAmount +
            ", hash='" + hash + '\'' +
            ", block=" + block +
            ", confirmed=" + confirmed +
            ", lastPrice=" + lastPrice +
            ", lastGas=" + lastGas +
            ", blockDate=" + blockDate +
            '}';
    }

    //------------- GETTERS & SETTERS -------------------------

    public Double getPsIncomeUsd() {
        return psIncomeUsd;
    }

    public void setPsIncomeUsd(Double psIncomeUsd) {
        this.psIncomeUsd = psIncomeUsd;
    }

    public Double getPsWeekApy() {
        return psWeekApy;
    }

    public void setPsWeekApy(Double psWeekApy) {
        this.psWeekApy = psWeekApy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOtherCoin() {
        return otherCoin;
    }

    public void setOtherCoin(String otherCoin) {
        this.otherCoin = otherCoin;
    }

    public double getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(double otherAmount) {
        this.otherAmount = otherAmount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BigInteger getBlock() {
        return block;
    }

    public void setBlock(BigInteger block) {
        this.block = block;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public Double getLastGas() {
        return lastGas;
    }

    public void setLastGas(Double lastGas) {
        this.lastGas = lastGas;
    }

    public Long getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(Long blockDate) {
        this.blockDate = blockDate;
    }

    public Integer getOwnerCount() {
        return ownerCount;
    }

    public void setOwnerCount(Integer ownerCount) {
        this.ownerCount = ownerCount;
    }
}
