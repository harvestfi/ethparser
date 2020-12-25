package pro.belbix.ethparser.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import lombok.Data;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Data
public class HarvestTx implements EthTransactionI {

    private long logId;
    private String hash;
    private String methodName;
    private String owner;
    private BigInteger block;
    private String blockHash;
    private BigInteger amount;
    private BigInteger amountIn;
    private Address vault;
    private Address fToken;
    private Address[] addressFromArgs;
    private Address addressFromArgs1;
    private Address addressFromArgs2;
    private BigInteger[] intFromArgs;
    private BigInteger intFromArgs1;
    private BigInteger intFromArgs2;
    private boolean success = false;
    private boolean enriched;
    private boolean migration = false;

    public boolean isContainsAddress(Map<String, String> addresses) {
        return addresses.containsKey(vault.getValue().toLowerCase());
    }

    public HarvestDTO toDto() {
        HarvestDTO dto = new HarvestDTO();
        dto.setId(hash + "_" + logId);
        dto.setHash(hash);
        dto.setBlock(block);
        dto.setVault(removeBracers(Vaults.vaultHashToName.get(vault.getValue())));
        dto.setConfirmed(success);
        dto.setMethodName(methodName);
        dto.setAmount(parseAmount(amount, vault.getValue()));
        if (amountIn != null) {
            dto.setAmountIn(parseAmount(amountIn, fToken.getValue()));
        }
        dto.setOwner(owner);

        enrichMethodDepend(dto);
        return dto;
    }

    public static String removeBracers(String s) {
        if (s.equals("_3CRV")) {
            return "3CRV";
        }
        return s;
    }

    public static double parseAmount(BigInteger amount, String address) {
        if (amount == null) {
            return 0.0;
        }
        Double divider = Vaults.vaultDividers.get(address);
        if (divider == null) {
            divider = LpContracts.lpHashToDividers.get(address);
            if (divider == null) {
                throw new IllegalStateException("Divider not found for " + address);
            }
        }
        return amount.doubleValue() / divider;
        //return new BigDecimal(amount).divide(BigDecimal.valueOf(divider)).doubleValue() ;
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
}
