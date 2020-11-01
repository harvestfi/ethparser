package pro.belbix.ethparser.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.web3.harvest.Vaults;

public class HarvestTx implements EthTransactionI {

    private String hash;
    private String methodName;
    private String owner;
    private String timestamp;
    private BigInteger block;
    private BigInteger amount = new BigInteger("0");
    private Address vault;
    private Address[] addressFromArgs;
    private Address addressFromArgs1;
    private Address addressFromArgs2;
    private BigInteger[] intFromArgs;
    private BigInteger intFromArgs1;
    private BigInteger intFromArgs2;

    private boolean success = false;
    private boolean enriched;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

    public BigInteger getBlock() {
        return block;
    }

    public void setBlock(BigInteger block) {
        this.block = block;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isEnriched() {
        return enriched;
    }

    public void setEnriched(boolean enriched) {
        this.enriched = enriched;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public Address getVault() {
        return vault;
    }

    public void setVault(Address vault) {
        this.vault = vault;
    }

    public Address getAddressFromArgs1() {
        return addressFromArgs1;
    }

    public void setAddressFromArgs1(Address addressFromArgs1) {
        this.addressFromArgs1 = addressFromArgs1;
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

    public Address getAddressFromArgs2() {
        return addressFromArgs2;
    }

    public void setAddressFromArgs2(Address addressFromArgs2) {
        this.addressFromArgs2 = addressFromArgs2;
    }

    public Address[] getAddressFromArgs() {
        return addressFromArgs;
    }

    public void setAddressFromArgs(Address[] addressFromArgs) {
        this.addressFromArgs = addressFromArgs;
    }

    public BigInteger[] getIntFromArgs() {
        return intFromArgs;
    }

    public void setIntFromArgs(BigInteger[] intFromArgs) {
        this.intFromArgs = intFromArgs;
    }

    public boolean isContainsAddress(Map<String, String> addresses) {
        return addresses.containsKey(vault.getValue().toLowerCase());
    }

    public HarvestDTO toDto() {
        HarvestDTO dto = new HarvestDTO();
        dto.setHash(hash);
        dto.setBlock(block);
        dto.setVault(Vaults.vaultNames.get(vault.getValue()));
        dto.setConfirmed(success);
        dto.setMethodName(methodName);
        dto.setAmount(parseAmount(amount, vault.getValue()));

        enrichMethodDepend(dto);
        return dto;
    }

    public static double parseAmount(BigInteger amount, String vault) {
        if (amount == null) {
            return 0.0;
        }
        return amount.doubleValue() / Vaults.vaultDividers.get(vault);
    }

    private void enrichMethodDepend(HarvestDTO dto) {
        switch (methodName) {
            case "deposit":
            case "withdraw":
                break;
            case "underlyingBalanceWithInvestmentForHolder":
            case "setStrategy":
                break;
            case "setVaultFractionToInvest":
                break;
            case "depositFor":
                break;
            case "withdrawAll":
            case "underlyingBalanceInVault":
            case "underlyingBalanceWithInvestment":
            case "governance":
            case "controller":
            case "underlying":
            case "strategy":
            case "getPricePerFullShare":
            case "doHardWork":
            case "rebalance":
                break;
        }
    }

    @Override
    public String toString() {
        return "HarvestTx{" +
            "hash='" + hash + '\'' +
            ", methodName='" + methodName + '\'' +
            ", owner='" + owner + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", block=" + block +
            ", amount=" + amount +
            ", vault=" + vault +
            ", addressFromArgs=" + Arrays.toString(addressFromArgs) +
            ", addressFromArgs1=" + addressFromArgs1 +
            ", addressFromArgs2=" + addressFromArgs2 +
            ", intFromArgs=" + Arrays.toString(intFromArgs) +
            ", intFromArgs1=" + intFromArgs1 +
            ", intFromArgs2=" + intFromArgs2 +
            ", success=" + success +
            ", enriched=" + enriched +
            '}';
    }
}
