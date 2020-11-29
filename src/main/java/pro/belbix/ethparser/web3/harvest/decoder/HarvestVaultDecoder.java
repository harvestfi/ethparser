package pro.belbix.ethparser.web3.harvest.decoder;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"rawtypes", "unchecked"})
public class HarvestVaultDecoder extends MethodDecoder {

    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodID, Transaction transaction) {
        String methodName = methodNamesByMethodId.get(methodID);
        HarvestTx tx = new HarvestTx();
        tx.setHash(transaction.getHash());
        tx.setOwner(transaction.getFrom());
        tx.setBlock(transaction.getBlockNumber());
        tx.setMethodName(methodName);
        tx.setVault(new Address(transaction.getTo()));
        switch (methodName) {
            case "deposit":
            case "withdraw":
                tx.setAmount((BigInteger) types.get(0).getValue());
                return tx;
            case "underlyingBalanceWithInvestmentForHolder":
            case "setStrategy":
                tx.setAddressFromArgs1((Address) types.get(0).getValue());
                return tx;
            case "setVaultFractionToInvest":
                tx.setIntFromArgs1((BigInteger) types.get(0).getValue());
                tx.setIntFromArgs2((BigInteger) types.get(1).getValue());
                return tx;
            case "depositFor":
                tx.setAmount((BigInteger) types.get(0).getValue());
                tx.setAddressFromArgs1((Address) types.get(1).getValue());
                return tx;
            case "approve":
                tx.setAddressFromArgs1(new Address((String) types.get(0).getValue()));
                tx.setAmount((BigInteger) types.get(1).getValue());
                return tx;
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
                return tx;
        }
        throw new IllegalStateException("Unknown method " + methodName + " from " + transaction.getHash());
    }

    @Override
    protected void initParameters() {
        if (parametersByMethodId.isEmpty()) {
            Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();
            try {
                parameters.put("underlyingBalanceInVault", Collections.emptyList());
                parameters.put("underlyingBalanceWithInvestment", Collections.emptyList());
                parameters.put("governance", Collections.emptyList());
                parameters.put("controller", Collections.emptyList());
                parameters.put("underlying", Collections.emptyList());
                parameters.put("strategy", Collections.emptyList());
                parameters.put("withdrawAll", Collections.emptyList());
                parameters.put("getPricePerFullShare", Collections.emptyList());
                parameters.put("doHardWork", Collections.emptyList());
                parameters.put("rebalance", Collections.emptyList());
                parameters.put("setStrategy",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("address")

                    ));
                parameters.put("setVaultFractionToInvest",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("deposit",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("depositFor",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address")

                    ));
                parameters.put("withdraw",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("underlyingBalanceWithInvestmentForHolder",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("address")

                    ));
                parameters.put("depositAll",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256[]"),
                        TypeReference.makeTypeReference("address[]")

                    ));
                parameters.put("approve",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            writeParameters(parameters);
        }
    }

}
