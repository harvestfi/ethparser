package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.uniswap.UniswapTransactionsParser.FARM_TOKEN_CONTRACT;

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

@SuppressWarnings({"rawtypes", "unchecked"})
public class UniswapRouterDecoder {

    private final static Address WETH_ADDRESS = new Address("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2");
    private Map<String, List<TypeReference<Type>>> parametersByMethodId;
    private Map<String, String> methodNamesByMethodId;

    public UniswapRouterDecoder() {
        initParameters();
    }

    public UniswapTx decodeInputData(Transaction transaction) {
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

    UniswapTx mapTypesToModel(List<Type> types, String methodId, Transaction transaction) {
        String methodName = methodNamesByMethodId.get(methodId);
        UniswapTx tx = new UniswapTx();
        tx.setHash(transaction.getHash());
        tx.setOwner(transaction.getFrom());
        tx.setBlock(transaction.getBlockNumber());
        parseMethod(tx, types, methodName);

        //ensure that FARM coin setCorrect
        if (tx.isContainsAddress(FARM_TOKEN_CONTRACT)) {
            if (tx.tokenIsFirstOrLast(FARM_TOKEN_CONTRACT)) { //BUY
                tx.setCoinIn(new Address(FARM_TOKEN_CONTRACT));
                tx.setCoinOut(tx.getAllAddresses()[1]);
                tx.setBuy(false);
            } else { //SELL
                tx.setCoinOut(new Address(FARM_TOKEN_CONTRACT));
                tx.setCoinIn(tx.getAllAddresses()[tx.getAllAddresses().length - 2]);
                tx.setBuy(true);
            }
        }
        return tx;
    }

    private void parseMethod(UniswapTx tx, List<Type> types, String methodName) {
        Address[] addresses = new Address[2];
        switch (methodName) {
            case "addLiquidityETH":
                tx.setType(UniswapTx.ADD_LIQ);
                addresses[0] = WETH_ADDRESS;
                addresses[1] = new Address((String) types.get(0).getValue());
                tx.setAllAddresses(addresses);
                tx.setAmountOut((BigInteger) types.get(1).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return;
            case "addLiquidity":
                tx.setType(UniswapTx.ADD_LIQ);
                Object coinIn = types.get(0).getValue();
                if (coinIn instanceof String) {
                    addresses[0] = new Address((String) coinIn);
                } else {
                    addresses[0] = (Address) coinIn;
                }
                Object coinOut = types.get(1).getValue();
                if (coinOut instanceof String) {
                    addresses[1] = new Address((String) coinOut);
                } else {
                    addresses[1] = (Address) coinOut;
                }
                tx.setAllAddresses(addresses);
                tx.setAmountIn((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                return;
            case "removeLiquidityETH":
            case "removeLiquidityETHWithPermit":
            case "removeLiquidityETHWithPermitSupportingFeeOnTransferTokens":
            case "removeLiquidityETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.REMOVE_LIQ);
                addresses[0] = WETH_ADDRESS;
                addresses[1] = new Address((String) types.get(0).getValue());
                tx.setAllAddresses(addresses);
                tx.setLiquidity((BigInteger) types.get(1).getValue());
                tx.setAmountOut((BigInteger) types.get(2).getValue());
                tx.setAmountIn((BigInteger) types.get(3).getValue());
                return;
            case "removeLiquidity":
            case "removeLiquidityWithPermit":
                tx.setType(UniswapTx.REMOVE_LIQ);
                addresses[1] = new Address((String) types.get(0).getValue());
                addresses[0] = new Address((String) types.get(1).getValue());
                tx.setAllAddresses(addresses);
                tx.setLiquidity((BigInteger) types.get(2).getValue());
                tx.setAmountOut((BigInteger) types.get(3).getValue());
                tx.setAmountIn((BigInteger) types.get(4).getValue());
                return;
            case "swapExactTokensForETH": //0x75e17566b36eb7fc9bc1be4c95d2e36cd30b09faa803fd1e06732c504ecee1a9
            case "swapExactTokensForETHSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountEth((BigInteger) types.get(1).getValue());
//                tx.setCoinIn(parseAddress(types.get(2), 0));
//                tx.setCoinOut(parseAddress(types.get(2), 1));
                tx.setAllAddresses(parseAddresses(types.get(2)));
                return;
            case "swapExactTokensForTokens":
            case "swapExactTokensForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountIn((BigInteger) types.get(0).getValue());
                tx.setAmountOut((BigInteger) types.get(1).getValue());
//                tx.setCoinIn(parseAddress(types.get(2), -2)); //should parse last pair for detect farm
//                tx.setCoinOut(parseAddress(types.get(2), -1));
                tx.setAllAddresses(parseAddresses(types.get(2)));
                return;
            case "swapExactETHForTokens": //0xb28bfbcc048fca2193b4c56518f42a7a1c1951720b07e86fe171c9db19cda71b
            case "swapETHForExactTokens":
            case "swapExactETHForTokensSupportingFeeOnTransferTokens":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountEth((BigInteger) types.get(0).getValue());
//                tx.setCoinIn(parseAddress(types.get(1), -2));
//                tx.setCoinOut(parseAddress(types.get(1), -1));
                tx.setAllAddresses(parseAddresses(types.get(1)));
                return;
            case "swapTokensForExactTokens":
            case "swapTokensForExactETH":
                tx.setType(UniswapTx.SWAP);
                tx.setAmountOut((BigInteger) types.get(0).getValue());
                tx.setAmountIn((BigInteger) types.get(1).getValue());
//                tx.setCoinIn(parseAddress(types.get(2), 0));
//                tx.setCoinOut(parseAddress(types.get(2), 1));
                tx.setAllAddresses(parseAddresses(types.get(2)));
                return;
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

    private static Address[] parseAddresses(Type type) {
        List adrs = ((List) type.getValue());
        Address[] result = new Address[adrs.size()];
        int i = 0;
        for (Object a : adrs) {
            result[i] = (Address) a;
            i++;
        }
        return result;
    }

    String createMethodId(String name, List<TypeReference<Type>> parameters) {
        return methodSignatureToShortHex(createMethodSignature(name, parameters));
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

    void initParameters() {
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
    }
}
