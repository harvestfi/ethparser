package pro.belbix.ethparser.dto;

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
    private String type;
    private String coin;
    private String owner;
    private double amount;
    private String otherCoin;
    private double otherAmount;
    private Double ethAmount;
    private String hash;
    private BigInteger block;
    private boolean confirmed = false;
    private Double lastPrice;
    private Double lastGas;
    private Long blockDate;
    private Integer ownerCount;

    public String print() {
        return Instant.ofEpochSecond(blockDate) + " "
            + type + " "
            + String.format("%.2f", amount) + " "
            + coin + " for "
            + otherCoin + " "
            + String.format("%.2f", otherAmount)
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
            ", ethAmount=" + ethAmount +
            ", hash='" + hash + '\'' +
            ", block=" + block +
            ", confirmed=" + confirmed +
            ", lastPrice=" + lastPrice +
            ", lastGas=" + lastGas +
            ", blockDate=" + blockDate +
            '}';
    }

    //------------- GETTERS & SETTERS -------------------------

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

    public Double getEthAmount() {
        return ethAmount;
    }

    public void setEthAmount(Double ethAmount) {
        this.ethAmount = ethAmount;
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

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
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
