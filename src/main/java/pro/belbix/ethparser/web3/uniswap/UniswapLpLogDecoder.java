package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.uniswap.UniswapPoolDecoder.USDC_ADDRESS;
import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_TOKEN_CONTRACT;
import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.UNI_ROUTER;

import java.math.BigInteger;
import java.util.Arrays;
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
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UniswapLpLogDecoder extends MethodDecoder {

    public static final String FARM_USDC_LP_CONTRACT = "0x514906FC121c7878424a5C928cad1852CC545892".toLowerCase();
    public static final String FARM_TOPIC_ADDRESS1 = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D".toLowerCase(); //sell
    public static final String USDC_TOPIC_ADDRESS1 = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d".toLowerCase(); //buy
    private static final Set<String> allowedMethods = new HashSet<>(Arrays.asList("Mint", "Burn", "Swap"));

    public void decode(UniswapTx tx, Log log) {
        if (!isValidLog(log)) {
            return;
        }
        String topic0 = log.getTopics().get(0);
        String methodId = methodIdByFullHex.get(topic0);

        if (methodId == null) {
            throw new IllegalStateException("Unknown topic " + topic0);
        }
        String methodName = methodNamesByMethodId.get(methodId);

        if (!allowedMethods.contains(methodName)) {
            return;
        }

        List<TypeReference<Type>> parameters = parametersByMethodId.get(methodId);
        if (parameters == null) {
            throw new IllegalStateException("Not found parameters for topic " + topic0 + " with " + methodId);
        }

        List<Type> types = extractLogIndexedValues(log, parameters);
        tx.setHash(log.getTransactionHash());
        tx.setLogId(log.getLogIndex().longValue());
        tx.setBlock(log.getBlockNumber());
        tx.setSuccess(true);
        enrich(types, methodName, tx);
    }

    private boolean isValidLog(Log log) {
        if (log == null || log.getTopics() == null || log.getTopics().isEmpty()) {
            return false;
        }
        return FARM_USDC_LP_CONTRACT.equals(log.getAddress());
    }

    private void enrich(List<Type> types, String methodName, UniswapTx tx) {
        switch (methodName) {
            case "Swap":
                tx.setType(UniswapTx.SWAP);
                String sender = (String) types.get(0).getValue();
                BigInteger amount0In = (BigInteger) types.get(2).getValue();
                BigInteger amount1In = (BigInteger) types.get(3).getValue();
                BigInteger amount0Out = (BigInteger) types.get(4).getValue();
                BigInteger amount1Out = (BigInteger) types.get(5).getValue();

                if (!amount0In.equals(BigInteger.ZERO)) {
                    tx.setAmountIn(amount0In);
                } else if (!amount1In.equals(BigInteger.ZERO)) {
                    tx.setAmountIn(amount1In);
                } else {
                    throw new IllegalStateException("Zero amountIn");
                }
                if (!amount0Out.equals(BigInteger.ZERO)) {
                    tx.setAmountOut(amount0Out);
                } else if (!amount1Out.equals(BigInteger.ZERO)) {
                    tx.setAmountOut(amount1Out);
                } else {
                    throw new IllegalStateException("Zero amountOut");
                }

//                if (FARM_TOPIC_ADDRESS1.equals(sender)) {
                if (amount1In.equals(BigInteger.ZERO)) {
                    tx.setBuy(false);
                    tx.setCoinIn(new Address(FARM_TOKEN_CONTRACT));
                    tx.setCoinOut(new Address(USDC_ADDRESS));
//                } else if (USDC_TOPIC_ADDRESS1.equals(sender)) {
                } else if (amount0In.equals(BigInteger.ZERO)) {
                    tx.setBuy(true);
                    tx.setCoinIn(new Address(USDC_ADDRESS));
                    tx.setCoinOut(new Address(FARM_TOKEN_CONTRACT));
                } else {
                    throw new IllegalStateException("Unknown sender " + sender);
                }

                return;
            case "Mint":
                tx.setType(UniswapTx.ADD_LIQ);
                tx.setBuy(true);
                tx.setCoinIn(new Address(USDC_ADDRESS));
                tx.setCoinOut(new Address(FARM_TOKEN_CONTRACT));
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setAmountIn((BigInteger) types.get(2).getValue());

                return;
            case "Burn":
                tx.setType(UniswapTx.REMOVE_LIQ);
                tx.setBuy(false);
                tx.setCoinIn(new Address(FARM_TOKEN_CONTRACT));
                tx.setCoinOut(new Address(USDC_ADDRESS));
                tx.setAmountIn((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());

                return;
        }
        throw new IllegalStateException("Unknown method " + methodName);
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
                parameters.put("Burn",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address", true, false)
                    ));
                parameters.put("Sync",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint112"),
                        TypeReference.makeTypeReference("uint112")
                    ));
                parameters.put("Approval",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("Transfer",
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
