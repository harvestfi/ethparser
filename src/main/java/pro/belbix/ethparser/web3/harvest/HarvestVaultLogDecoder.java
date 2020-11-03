package pro.belbix.ethparser.web3.harvest;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"unchecked", "rawtypes"})
public class HarvestVaultLogDecoder extends MethodDecoder {

    public HarvestTx decode(Log log) {
        if (!isValidLog(log)) {
            return null;
        }
        String topic0 = log.getTopics().get(0);
        String methodId = methodIdByFullHex.get(topic0);

        if (methodId == null) {
            throw new IllegalStateException("Unknown topic " + topic0);
        }
        String methodName = methodNamesByMethodId.get(methodId);


        List<TypeReference<Type>> parameters = parametersByMethodId.get(methodId);
        if (parameters == null) {
            throw new IllegalStateException("Not found parameters for topic " + topic0 + " with " + methodId);
        }

        List<Type> types = extractLogIndexedValues(log, parameters);
        HarvestTx tx = new HarvestTx();
        tx.setVault(new Address(log.getAddress()));
        tx.setLogId(log.getLogIndex().longValue());
        tx.setHash(log.getTransactionHash());
        tx.setBlock(log.getBlockNumber());
        tx.setBlockHash(log.getBlockHash());
        tx.setMethodName(methodName);
        tx.setSuccess(true); //from logs always success
        enrich(types, methodName, tx);
        return tx;
    }

    private boolean isValidLog(Log log) {
        if (log == null || log.getTopics().isEmpty()) {
            return false;
        }
        return Vaults.vaultNames.containsKey(log.getAddress());
    }

    private void enrich(List<Type> types, String methodName, HarvestTx tx) {
        switch (methodName) {
            case "Deposit":
            case "Withdraw":
            case "Staked":
            case "Withdrawn":
            case "RewardPaid":
            case "StrategyAnnounced":
                tx.setOwner((String) types.get(0).getValue());
                tx.setAmount((BigInteger) types.get(1).getValue());
                return;
            case "Invest":
            case "RewardAdded":
            case "withdraw":
            case "stake":
                tx.setAmount((BigInteger) types.get(0).getValue());
                return;
            case "StrategyChanged":
            case "addVaultAndStrategy":
                tx.setAddressFromArgs1(new Address((String) types.get(0).getValue()));
                tx.setAddressFromArgs2(new Address((String) types.get(1).getValue()));
                return;
            case "Transfer":
            case "Approval":
                tx.setAddressFromArgs1(new Address((String) types.get(0).getValue()));
                tx.setAddressFromArgs2(new Address((String) types.get(1).getValue()));
                tx.setAmount((BigInteger) types.get(2).getValue());
                return;
            case "depositAll":
                tx.setIntFromArgs(parseInts(types.get(0)));
                tx.setAddressFromArgs(parseAddresses(types.get(1)));
                return;
            case "migrateInOneTx":
                Address[] addresses = new Address[4];
                addresses[0] = (Address) types.get(0).getValue();
                addresses[1] = (Address) types.get(1).getValue();
                addresses[2] = (Address) types.get(2).getValue();
                addresses[3] = (Address) types.get(3).getValue();
                tx.setAddressFromArgs(addresses);
                return;
            case "exit":
                return;
        }
        throw new IllegalStateException("Unknown method " + methodName);
    }

    private static BigInteger[] parseInts(Type type) {
        List values = ((List) type.getValue());
        BigInteger[] integers = new BigInteger[values.size()];
        int i = 0;
        for (Object v : values) {
            integers[i] = (BigInteger) v;
            i++;
        }
        return integers;
    }

    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodID, Transaction transaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void initParameters() {
        if (parametersByMethodId.isEmpty()) {
            Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();
            try {
                parameters.put("addVaultAndStrategy",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address")

                    ));
                parameters.put("exit", Collections.emptyList());
                parameters.put("stake",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("depositAll",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256[]"),
                        TypeReference.makeTypeReference("address[]")

                    ));
                parameters.put("migrateInOneTx",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address")

                    ));
                parameters.put("withdraw",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("uint256")

                    ));
                //EVENTS--------------------**************************
                parameters.put("Withdraw",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Deposit",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Invest",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("StrategyAnnounced",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("StrategyChanged",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address")
                    ));
                parameters.put("Transfer",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Approval",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Staked",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Withdrawn",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("RewardPaid",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("RewardAdded",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("uint256")
                    ));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            writeParameters(parameters);
        }
    }
}
