package pro.belbix.ethparser.model;

import java.math.BigInteger;
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
    private String hash;
    private BigInteger block;
    private boolean confirmed = false;
    private Long blockDate;

    private String methodName;
    private String owner;
    private String timestamp;
    private Double amount;
    private String vault;
    private Double lastGas;
    private Double lastTVL;
    private Integer ownerCount;

    public void setLastGas(Double lastGas) {
        this.lastGas = lastGas;
    }

    public Double getLastTVL() {
        return lastTVL;
    }

    public void setLastTVL(Double lastTVL) {
        this.lastTVL = lastTVL;
    }

    public double getLastGas() {
        return lastGas;
    }

    public void setLastGas(double lastGas) {
        this.lastGas = lastGas;
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

    public long getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(long blockDate) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

    public String print() {
        return methodName + " "
            + String.format("%.18f", amount) + " "
            + vault
            + " " + hash
            + " " + lastTVL;
    }
}
