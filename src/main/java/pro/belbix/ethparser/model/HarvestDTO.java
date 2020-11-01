package pro.belbix.ethparser.model;

import java.math.BigInteger;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "harvest_tx", indexes = {
    @Index(name = "idx_harvest_tx", columnList = "blockDate")
})
@Cacheable(false)
public class HarvestDTO implements DtoI {

    @Id
    private String hash;
    private BigInteger block;
    private boolean confirmed = false;
    private long blockDate;

    private String methodName;
    private String owner;
    private String timestamp;
    private double amount;
    private String vault;
    private String addressFromArgs;
    private BigInteger intFromArgs1;
    private BigInteger intFromArgs2;
    private double lastGas;

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

    public String getAddressFromArgs() {
        return addressFromArgs;
    }

    public void setAddressFromArgs(String addressFromArgs) {
        this.addressFromArgs = addressFromArgs;
    }

    public BigInteger getIntFromArgs1() {
        return intFromArgs1;
    }

    public void setIntFromArgs1(BigInteger intFromArgs1) {
        this.intFromArgs1 = intFromArgs1;
    }

    public BigInteger getIntFromArgs2() {
        return intFromArgs2;
    }

    public void setIntFromArgs2(BigInteger intFromArgs2) {
        this.intFromArgs2 = intFromArgs2;
    }

    public String print() {
        String result = methodName + " "
            + String.format("%.18f", amount) + " "
            + vault
            + " " + hash;
        return result;
    }
}
