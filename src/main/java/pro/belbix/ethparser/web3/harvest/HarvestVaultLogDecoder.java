package pro.belbix.ethparser.web3.harvest;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.FunctionReturnDecoder;
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

    public void enrichFromLog(HarvestTx tx, Log log) {
        if (log.getTopics().isEmpty()) {
            return;
        }
        String topic0 = log.getTopics().get(0);
        String methodId = HarvestTopics.topicToMethodId.get(topic0);

        if (methodId == null) {
            throw new IllegalStateException("Unknown topic " + topic0);
        }

        List<TypeReference<Type>> parameters = parametersByMethodId.get(methodId);
        if (parameters == null) {
            throw new IllegalStateException("Not found parameters for topic " + topic0 + " with " + methodId);
        }

        List<Type> types = FunctionReturnDecoder.decode(log.getData(), parameters);
        tx.setHash(log.getTransactionHash());
        tx.setBlock(log.getBlockNumber());
        enrich(types, methodId, tx);
    }

    private void enrich(List<Type> types, String methodID, HarvestTx tx) {
        String methodName = methodNamesByMethodId.get(methodID);
        tx.setMethodName(methodName);
        switch (methodName) {
            case "addVaultAndStrategy":
                tx.setAddressFromArgs1(new Address((String) types.get(0).getValue()));
                tx.setAddressFromArgs2(new Address((String) types.get(1).getValue()));
                return;
            case "withdraw":
            case "stake":
                tx.setAmount((BigInteger) types.get(0).getValue());
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
        }
        throw new IllegalStateException("Unknown method");
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

    private static Address[] parseAddresses(Type type) {
        List values = ((List) type.getValue());
        Address[] addresses = new Address[values.size()];
        int i = 0;
        for (Object v : values) {
            addresses[i] = (Address) v;
            i++;
        }
        return addresses;
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            writeParameters(parameters);
        }
    }
}
