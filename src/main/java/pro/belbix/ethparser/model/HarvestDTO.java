package pro.belbix.ethparser.model;

import java.math.BigInteger;
import java.time.Instant;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "harvest_tx", indexes = {
    @Index(name = "idx_harvest_tx", columnList = "blockDate"),
    @Index(name = "idx_harvest_tx2", columnList = "methodName, vault")
})
@Cacheable(false)
public class HarvestDTO implements DtoI {

    @Id
    private String id;
    private String hash;
    private BigInteger block;
    private boolean confirmed = false;
    private Long blockDate;
    private String methodName;
    private String owner;
    private Double amount;
    private Double amountIn;
    private String vault;
    private Double lastGas;
    private Double lastTvl;
    private Double lastUsdTvl;
    private Integer ownerCount;
    private Double sharePrice;
    private Long usdAmount;
    private String prices;
    private String lpStat;

    public String print() {
        return Instant.ofEpochSecond(blockDate) + " "
            + methodName + " "
            + "usd: " + usdAmount + " "
            + "f: " + amount + " "
            + vault
            + " " + hash
            + " " + String.format("%f.0", lastUsdTvl);
    }

    //------------- GETTERS & SETTERS -------------------------

    public String getLpStat() {
        return lpStat;
    }

    public void setLpStat(String lpStat) {
        this.lpStat = lpStat;
    }

    public String getPrices() {
        return prices;
    }

    public void setPrices(String prices) {
        this.prices = prices;
    }

    public Double getLastTvl() {
        return lastTvl;
    }

    public void setLastTvl(Double lastTvl) {
        this.lastTvl = lastTvl;
    }

    public Long getUsdAmount() {
        return usdAmount;
    }

    public void setUsdAmount(Long usdAmount) {
        this.usdAmount = usdAmount;
    }

    public Double getSharePrice() {
        return sharePrice;
    }

    public void setSharePrice(Double sharePrice) {
        this.sharePrice = sharePrice;
    }

    public Double getAmountIn() {
        return amountIn;
    }

    public void setAmountIn(Double amountIn) {
        this.amountIn = amountIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Long getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(Long blockDate) {
        this.blockDate = blockDate;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

    public Double getLastGas() {
        return lastGas;
    }

    public void setLastGas(Double lastGas) {
        this.lastGas = lastGas;
    }

    public Double getLastUsdTvl() {
        return lastUsdTvl;
    }

    public void setLastUsdTvl(Double lastTVL) {
        this.lastUsdTvl = lastTVL;
    }

    public Integer getOwnerCount() {
        return ownerCount;
    }

    public void setOwnerCount(Integer ownerCount) {
        this.ownerCount = ownerCount;
    }
}
