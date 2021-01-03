package pro.belbix.ethparser.web3;

import static org.web3j.abi.FunctionReturnDecoder.decodeIndexedValue;

import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Fixed;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Ufixed;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.web3.erc20.Tokens;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class MethodDecoder {

    protected Map<String, List<TypeReference<Type>>> parametersByMethodId = new HashMap<>();
    protected Map<String, String> methodNamesByMethodId = new HashMap<>();
    protected Map<String, String> methodIdByFullHex = new HashMap<>();

    public MethodDecoder() {
        initParameters();
    }

    public static Address[] parseAddresses(Type type) {
        List addresses = ((List) type.getValue());
        Address[] result = new Address[addresses.size()];
        int i = 0;
        for (Object a : addresses) {
            result[i] = (Address) a;
            i++;
        }
        return result;
    }

    public EthTransactionI decodeInputData(Transaction transaction) {
        String data = transaction.getInput();
        if (data.length() < 74) {
            return null;
        }
        String methodID = data.substring(0, 10);
        String input = data.substring(10);
        List<TypeReference<Type>> parameters = parametersByMethodId.get(methodID);
        if (parameters == null) {
            throw new IllegalStateException("Not found parameters for " + transaction.getHash());
        }
        List<Type> types = FunctionReturnDecoder.decode(input, parameters);
        return mapTypesToModel(types, methodID, transaction);
    }

    public abstract EthTransactionI mapTypesToModel(List<Type> types, String methodID, Transaction transaction);

    public String createMethodId(String name, List<TypeReference<Type>> parameters) {
        return methodSignatureToShortHex(createMethodSignature(name, parameters));
    }

    public String createMethodFullHex(String name, List<TypeReference<Type>> parameters) {
        return methodSignatureToFullHex(createMethodSignature(name, parameters));
    }

    public static BigInteger[] parseInts(Type type) {
        List values = ((List) type.getValue());
        BigInteger[] integers = new BigInteger[values.size()];
        int i = 0;
        for (Object v : values) {
            integers[i] = (BigInteger) v;
            i++;
        }
        return integers;
    }

    public static String methodSignatureToFullHex(String methodSignature) {
        final byte[] input = methodSignature.getBytes();
        final byte[] hash = Hash.sha3(input);
        return Numeric.toHexString(hash);
    }

    public String methodSignatureToShortHex(String methodSignature) {
        return methodSignatureToFullHex(methodSignature).substring(0, 10);
    }

    String createMethodSignature(String name, List<TypeReference<Type>> parameters) {
        StringBuilder result = new StringBuilder();
        result.append(name);
        result.append("(");
        String params =
            parameters.stream().map(this::getTypeName).collect(Collectors.joining(","));
        result.append(params);
        result.append(")");
        return result.toString();
    }

    <T extends Type> String getTypeName(TypeReference<T> typeReference) {
        try {
            java.lang.reflect.Type reflectedType = typeReference.getType();

            Class<?> type;
            if (reflectedType instanceof ParameterizedType) {
                type = (Class<?>) ((ParameterizedType) reflectedType).getRawType();
                return getParameterizedTypeName(typeReference, type);
            } else {
                type = Class.forName(reflectedType.getTypeName());
                return getSimpleTypeName(type);
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }

    <T extends Type, U extends Type> String getParameterizedTypeName(
        TypeReference<T> typeReference, Class<?> type) {

        try {
            if (type.equals(DynamicArray.class)) {
                Class<U> parameterizedType = getParameterizedTypeFromArray(typeReference);
                String parameterizedTypeName = getSimpleTypeName(parameterizedType);
                return parameterizedTypeName + "[]";
            } else if (type.equals(StaticArray.class)) {
                Class<U> parameterizedType = getParameterizedTypeFromArray(typeReference);
                String parameterizedTypeName = getSimpleTypeName(parameterizedType);
                return parameterizedTypeName
                    + "["
                    + ((TypeReference.StaticArrayTypeReference) typeReference).getSize()
                    + "]";
            } else {
                throw new UnsupportedOperationException("Invalid type provided " + type.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }

    String getSimpleTypeName(Class<?> type) {
        String simpleName = type.getSimpleName().toLowerCase();

        if (type.equals(Uint.class)
            || type.equals(Int.class)
            || type.equals(Ufixed.class)
            || type.equals(Fixed.class)) {
            return simpleName + "256";
        } else if (type.equals(Utf8String.class)) {
            return "string";
        } else if (type.equals(DynamicBytes.class)) {
            return "bytes";
        } else {
            return simpleName;
        }
    }

    public static double parseAmount(BigInteger amount, String address) {
        if (amount == null) {
            return 0.0;
        }
        Map<String, Double> dividers = new HashMap<>();
        dividers.putAll(Vaults.vaultDividers);
        dividers.putAll(LpContracts.lpHashToDividers);
        dividers.putAll(Tokens.tokenDividers);
        Double divider = dividers.get(address);
        if (divider == null) {
            throw new IllegalStateException("Divider not found for " + address);
        }
        return amount.doubleValue() / divider;
        //return new BigDecimal(amount).divide(BigDecimal.valueOf(divider)).doubleValue() ;
    }

    @SuppressWarnings("unchecked")
    <T extends Type> Class<T> getParameterizedTypeFromArray(TypeReference typeReference)
        throws ClassNotFoundException {

        java.lang.reflect.Type type = typeReference.getType();
        java.lang.reflect.Type[] typeArguments =
            ((ParameterizedType) type).getActualTypeArguments();

        String parameterizedTypeName = typeArguments[0].getTypeName();
        return (Class<T>) Class.forName(parameterizedTypeName);
    }

    public void writeParameters(Map<String, List<TypeReference<Type>>> parameters) {
        for (Map.Entry<String, List<TypeReference<Type>>> entry : parameters.entrySet()) {
            String methodName = entry.getKey();
            if (methodName.contains("#")) {
                methodName = methodName.split("#")[0];
            }

            String methodID = createMethodId(methodName, entry.getValue());
            String methodFullHex = createMethodFullHex(methodName, entry.getValue());
            parametersByMethodId.put(methodID, entry.getValue());
            methodNamesByMethodId.put(methodID, entry.getKey());
            methodIdByFullHex.put(methodFullHex, methodID);
//            System.out.println(this.getClass().getSimpleName() + " " + entry.getKey() + " " + methodID + " " + methodFullHex);
        }
    }

    public static List<Type> extractLogIndexedValues(Log log, List<TypeReference<Type>> parameters) {
        final List<String> topics = log.getTopics();

        List<Type> nonIndexedValues =
            FunctionReturnDecoder.decode(log.getData(), getNonIndexedParameters(parameters));
        List<TypeReference<Type>> indexedParameters = getIndexedParameters(parameters);
        List<Type> indexedValues = new ArrayList<>();
        for (int i = 0; i < indexedParameters.size(); i++) {
            String topic = topics.get(i + 1);
            Type value = decodeIndexedValue(topic, indexedParameters.get(i));
            indexedValues.add(value);
        }
        indexedValues.addAll(nonIndexedValues);
        return indexedValues;
    }

    public static List<TypeReference<Type>> getIndexedParameters(List<TypeReference<Type>> parameters) {
        return parameters.stream().filter(TypeReference::isIndexed).collect(Collectors.toList());
    }

    public static List<TypeReference<Type>> getNonIndexedParameters(List<TypeReference<Type>> parameters) {
        return parameters.stream().filter(p -> !p.isIndexed()).collect(Collectors.toList());
    }

    private void initParameters() {
        if (parametersByMethodId.isEmpty()) {
            Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();
            try {
                parameters.put("SmartContractRecorded",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("address", true, false)
                    ));
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
                parameters.put("migrateInOneTx",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address")

                    ));
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
                parameters.put("addLiquidity",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("addLiquidityETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("removeLiquidity",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("removeLiquidityETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")

                    ));
                parameters.put("removeLiquidityWithPermit",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("bool"),
                        TypeReference.makeTypeReference("uint8"),
                        TypeReference.makeTypeReference("bytes32"),
                        TypeReference.makeTypeReference("bytes32")
                    ));
                parameters.put("removeLiquidityETHWithPermit",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("bool"),
                        TypeReference.makeTypeReference("uint8"),
                        TypeReference.makeTypeReference("bytes32"),
                        TypeReference.makeTypeReference("bytes32")
                    ));
                parameters.put("removeLiquidityETHSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("removeLiquidityETHWithPermitSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("bool"),
                        TypeReference.makeTypeReference("uint8"),
                        TypeReference.makeTypeReference("bytes32"),
                        TypeReference.makeTypeReference("bytes32")
                    ));
                parameters.put("swapExactTokensForTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapTokensForExactTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactETHForTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapTokensForExactETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactTokensForETH",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapETHForExactTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactTokensForTokensSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactETHForTokensSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("swapExactTokensForETHSupportingFeeOnTransferTokens",
                    Arrays.asList(
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("address[]"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("RewardDenied",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address", true, false),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("UpdateLiquidityLimit",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("transfer",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("mint",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("execute",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("bytes")
                    ));
                parameters.put("addMinter",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("address")
                    ));
                parameters.put("allowance",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address")
                    ));
                parameters.put("transferFrom",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("increaseAllowance",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("decreaseAllowance",
                    Arrays.asList(
                        TypeReference.makeTypeReference("address"),
                        TypeReference.makeTypeReference("uint256")
                    ));
                parameters.put("setStorage",
                    Collections.singletonList(
                        TypeReference.makeTypeReference("address")
                    ));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            writeParameters(parameters);
        }
    }

    public Map<String, String> getMethodNamesByMethodId() {
        return methodNamesByMethodId;
    }
}
