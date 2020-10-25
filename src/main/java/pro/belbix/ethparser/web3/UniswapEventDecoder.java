package pro.belbix.ethparser.web3;


import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.Arrays;
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
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;
import pro.belbix.ethparser.model.UniswapTx;

public class UniswapEventDecoder {
    private final static Address WETH_ADDRESS = new Address("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2");
    private Map<String, List<TypeReference<Type>>> parametersByMethodId;
    private Map<String, String> methodNamesByMethodId;

    public UniswapEventDecoder() {
        initParameters();
    }

    public UniswapTx decodeInputData(Transaction transaction) {
        String data = transaction.getInput();
        String methodID = data.substring(0, 10);
        String input = data.substring(10);
        List<TypeReference<Type>> parameters = parametersByMethodId.get(methodID);
        if (parameters == null) {
            throw new IllegalStateException("Not found parameters for " + data);
        }
        List<Type> types = FunctionReturnDecoder.decode(input, parameters);
        return mapTypesToModel(types, methodID, transaction);
    }

    UniswapTx mapTypesToModel(List<Type> types, String methodId, Transaction transaction) {
        String methodName = methodNamesByMethodId.get(methodId);
        UniswapTx tx = new UniswapTx();
        tx.setHash(transaction.getHash());
        tx.setOwner(transaction.getFrom());
        tx.setBlock(transaction.getBlockHash());
        switch (methodName) {
            case "addLiquidityETH":
                tx.setType(UniswapTx.ADD_LIQ);
                tx.setCoinIn(WETH_ADDRESS);
                tx.setCoinOut(new Address((String) types.get(0).getValue()));
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return tx;
            case "addLiquidity":
                tx.setType(UniswapTx.ADD_LIQ);
                Object coinIn = types.get(0).getValue();
                if (coinIn instanceof String) {
                    tx.setCoinIn(new Address((String) coinIn));
                } else {
                    tx.setCoinIn((Address) coinIn);
                }
                Object coinOut = types.get(1).getValue();
                if (coinOut instanceof String) {
                    tx.setCoinOut(new Address((String) coinOut));
                } else {
                    tx.setCoinOut((Address) coinOut);
                }
                tx.setAmountIn((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                return tx;
            case "removeLiquidityETH":
            case "removeLiquidityETHWithPermit":
            case "removeLiquidityETHWithPermitSupportingFeeOnTransferTokens":
            case "removeLiquidityETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.REMOVE_LIQ);
                tx.setCoinIn(WETH_ADDRESS);
                tx.setCoinOut(new Address((String) types.get(0).getValue()));
                tx.setLiquidity((BigInteger) types.get(1).getValue());
                tx.setAmountOut((BigInteger) types.get(2).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return tx;
            case "removeLiquidity":
            case "removeLiquidityWithPermit":
                tx.setType(UniswapTx.REMOVE_LIQ);
                tx.setCoinOut(new Address((String) types.get(0).getValue()));
                tx.setCoinIn(new Address((String) types.get(1).getValue()));
                tx.setLiquidity((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                tx.setAmountIn((BigInteger) types.get(4).getValue());
                return tx;
            case "swapExactTokensForETH":
            case "swapExactTokensForETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setCoinIn(parseAddress(types.get(2), 0));
                tx.setCoinOut(parseAddress(types.get(2), 1));
                return tx;
            case "swapExactETHForTokens":
            case "swapETHForExactTokens":
            case "swapExactETHForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
//                tx.setAmountIn((BigInteger) types.get(0).getValue()); //TODO!
                tx.setAmountOut((BigInteger) types.get(0).getValue());
                tx.setCoinOut(parseAddress(types.get(1), 0));
                tx.setCoinIn(parseAddress(types.get(1), 1));
                return tx;
            case "swapExactTokensForTokens":
            case "swapExactTokensForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setCoinIn(parseAddress(types.get(2), -2));
                tx.setCoinOut(parseAddress(types.get(2), -1));
                return tx;
            case "swapTokensForExactETH":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountOut((BigInteger) types.get(0).getValue());
                tx.setAmountIn((BigInteger) types.get(1).getValue());
                tx.setCoinOut(parseAddress(types.get(2), 0));
                tx.setCoinIn(parseAddress(types.get(2), 1));
                return tx;
            case "swapTokensForExactTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountOut((BigInteger) types.get(0).getValue());
                tx.setAmountIn((BigInteger) types.get(1).getValue());
                tx.setCoinIn(parseAddress(types.get(2), 1));
                tx.setCoinOut(parseAddress(types.get(2), 0));
                return tx;
        }
        throw new IllegalStateException("Unknown method");
    }

    private static Address parseAddress(Type type, int i) {
        if (i < 0) {
            List adrs = (List) type.getValue();
            return (Address) adrs.get(adrs.size() + i);
        }
        return (Address) ((List) type.getValue()).get(i);
    }

    String createMethodId(String name, List<TypeReference<Type>> parameters) {
        return methodSignatureToShortHex(createMethodSignature(name, parameters));
    }

    String createMethodSignature(String name, List<TypeReference<Type>> parameters) {
        StringBuilder result = new StringBuilder();
        result.append(name);
        result.append("(");
        String params =
            parameters.stream().map(p -> getTypeName(p)).collect(Collectors.joining(","));
        result.append(params);
        result.append(")");
        return result.toString();
    }

    String methodSignatureToShortHex(String methodSignature) {
        final byte[] input = methodSignature.getBytes();
        final byte[] hash = Hash.sha3(input);
        return Numeric.toHexString(hash).substring(0, 10);
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

    @SuppressWarnings("unchecked")
    <T extends Type> Class<T> getParameterizedTypeFromArray(TypeReference typeReference)
        throws ClassNotFoundException {

        java.lang.reflect.Type type = typeReference.getType();
        java.lang.reflect.Type[] typeArguments =
            ((ParameterizedType) type).getActualTypeArguments();

        String parameterizedTypeName = typeArguments[0].getTypeName();
        return (Class<T>) Class.forName(parameterizedTypeName);
    }


    Map<String, List<TypeReference<Type>>> initParameters() {
        if (parametersByMethodId == null) {
            Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();
            try {
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            parametersByMethodId = new HashMap<>();
            methodNamesByMethodId = new HashMap<>();
            for (Map.Entry<String, List<TypeReference<Type>>> entry : parameters.entrySet()) {
                String methodID = createMethodId(entry.getKey(), entry.getValue());
                parametersByMethodId.put(methodID, entry.getValue());
                methodNamesByMethodId.put(methodID, entry.getKey());
            }
        }
        return parametersByMethodId;
    }
}
