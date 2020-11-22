package pro.belbix.ethparser.web3.harvest;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.HardWorkTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"unchecked", "rawtypes"})
public class HardWorkLogDecoder extends MethodDecoder {

    public HardWorkTx decode(Log log) {
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
        HardWorkTx tx = new HardWorkTx();
        tx.setLogId(log.getLogIndex().toString());
        tx.setHash(log.getTransactionHash());
        tx.setMethodName(methodName);
        tx.setBlock(log.getBlockNumber().longValue());
        enrich(types, tx);
        return tx;
    }

    private boolean isValidLog(Log log) {
        return log != null && !log.getTopics().isEmpty();
    }

    private void enrich(List<Type> types, HardWorkTx tx) {
        if ("SharePriceChangeLog".equals(tx.getMethodName())) {
            tx.setVault((String) types.get(0).getValue());
            tx.setStrategy((String) types.get(1).getValue());
            tx.setOldSharePrice((BigInteger) types.get(2).getValue());
            tx.setNewSharePrice((BigInteger) types.get(3).getValue());
            tx.setBlockDate(((BigInteger) types.get(4).getValue()).longValue());
        } else if ("ProfitLogInReward".equals(tx.getMethodName())) {
            tx.setProfitAmount((BigInteger) types.get(0).getValue());
            tx.setFeeAmount((BigInteger) types.get(1).getValue());
        }
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
                parameters.put("Migrated",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("OwnershipTransferred",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false)
                    ));
                parameters.put("Staked#V2",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Withdraw#V2",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("ProfitLogInReward",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("SharePriceChangeLog",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Sync",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint112"),
                        TypeReference.makeTypeReference("uint112")
                    ));
                parameters.put("Swap",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address", true, false)
                    ));
                parameters.put("Mint",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Deposit#V2",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Rewarded",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            writeParameters(parameters);
        }
    }
}
