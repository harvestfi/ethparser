package pro.belbix.ethparser.model;

import java.math.BigInteger;

public class HardWorkTx {
    private String hash;
    private String logId;
    private String vault;
    private String strategy;
    private String methodName;
    private long block;
    private long blockDate;
    private BigInteger oldSharePrice;
    private BigInteger newSharePrice;
    private BigInteger profitAmount;
    private BigInteger feeAmount;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public BigInteger getProfitAmount() {
        return profitAmount;
    }

    public void setProfitAmount(BigInteger profitAmount) {
        this.profitAmount = profitAmount;
    }

    public BigInteger getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigInteger feeAmount) {
        this.feeAmount = feeAmount;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getVault() {
        return vault;
    }

    public void setVault(String vault) {
        this.vault = vault;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public long getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(long blockDate) {
        this.blockDate = blockDate;
    }

    public BigInteger getOldSharePrice() {
        return oldSharePrice;
    }

    public void setOldSharePrice(BigInteger oldSharePrice) {
        this.oldSharePrice = oldSharePrice;
    }

    public BigInteger getNewSharePrice() {
        return newSharePrice;
    }

    public void setNewSharePrice(BigInteger newSharePrice) {
        this.newSharePrice = newSharePrice;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
