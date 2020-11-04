package pro.belbix.ethparser.model;

import java.math.BigInteger;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.web3j.tuples.generated.Tuple2;

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
    @Transient
    private Tuple2<Double, Double> usdPrice;
    @Transient
    private Double tvlUsdFactor;

    public String print() {
        return methodName + " "
            + String.format("%.18f", amount) + " "
            + vault
            + " " + hash
            + " " + lastUsdTvl;
    }

    //------------- GETTERS & SETTERS -------------------------

    public Double getLastTvl() {
        return lastTvl;
    }

    public void setLastTvl(Double lastTvl) {
        this.lastTvl = lastTvl;
    }

    public Double getTvlUsdFactor() {
        return tvlUsdFactor;
    }

    public void setTvlUsdFactor(Double tvlUsdFactor) {
        this.tvlUsdFactor = tvlUsdFactor;
    }

    public Tuple2<Double, Double> getUsdPrice() {
        return usdPrice;
    }

    public void setUsdPrice(Tuple2<Double, Double> usdPrice) {
        this.usdPrice = usdPrice;
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
