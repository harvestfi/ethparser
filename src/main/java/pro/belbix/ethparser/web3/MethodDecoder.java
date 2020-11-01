package pro.belbix.ethparser.web3;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
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

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class MethodDecoder {

    protected Map<String, List<TypeReference<Type>>> parametersByMethodId = new HashMap<>();
    protected Map<String, String> methodNamesByMethodId = new HashMap<>();
    protected Map<String, String> methodIdByFullHex = new HashMap<>();

    public MethodDecoder() {
        initParameters();
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
            String methodID = createMethodId(entry.getKey(), entry.getValue());
            String methodFullHex = createMethodFullHex(entry.getKey(), entry.getValue());
            parametersByMethodId.put(methodID, entry.getValue());
            methodNamesByMethodId.put(methodID, entry.getKey());
            methodIdByFullHex.put(methodFullHex, methodID);
        }
    }

    protected abstract void initParameters();
}
