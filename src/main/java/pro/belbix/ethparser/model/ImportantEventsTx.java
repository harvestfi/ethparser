package pro.belbix.ethparser.model;

import java.math.BigInteger;

public class ImportantEventsTx {
    private String hash;
    private String logId;
    private String vault;
    private String oldStrategy;
    private String newStrategy;
    private String methodName;
    private long block;
    private long blockDate;
    private BigInteger feeAmount;
    private BigInteger mintAmount = BigInteger.ZERO;
    private long earliestEffective;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public BigInteger getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigInteger feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigInteger getMintAmount() {
        return mintAmount;
    }

    public void setMintAmount(BigInteger mintAmount) {
        this.mintAmount = mintAmount;
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

    public String getOldStrategy() {
        return oldStrategy;
    }

    public void setOldStrategy(String oldStrategy) {
        this.oldStrategy = oldStrategy;
    }

    public String getNewStrategy() {
        return newStrategy;
    }

    public void setNewStrategy(String newStrategy) {
        this.newStrategy = newStrategy;
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

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getearliestEffective() {
        return earliestEffective;
    }

    public void setEarliestEffective(long earliestEffective) {
        this.earliestEffective = earliestEffective;
    }


}
