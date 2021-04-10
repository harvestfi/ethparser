package pro.belbix.ethparser.web3;

import static org.web3j.abi.FunctionReturnDecoder.decodeIndexedValue;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.web3.abi.CommonMethods;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

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

  public static List<Type> extractLogIndexedValues(
      List<String> topics,
      String data,
      List<TypeReference<Type>> parameters) {
    List<Type> indexedValues = new ArrayList<>();
    if (parameters == null || data == null) {
      return indexedValues;
    }
    List<Type> nonIndexedValues;
    try {
      nonIndexedValues = FunctionReturnDecoder.decode(data, getNonIndexedParameters(parameters));
    } catch (NullPointerException e) {
      // it is an odd bug with loader sometimes happens when the app is not warmed up
      e.printStackTrace();
      return null;
    }
    List<TypeReference<Type>> indexedParameters = getIndexedParameters(parameters);
    for (int i = 0; i < indexedParameters.size(); i++) {
      String topic = topics.get(i + 1);
      Type value = decodeIndexedValue(topic, indexedParameters.get(i));
      indexedValues.add(value);
    }
    indexedValues.addAll(nonIndexedValues);
    return indexedValues;
  }

  public static List<TypeReference<Type>> getNonIndexedParameters(
      List<TypeReference<Type>> parameters) {
    return parameters.stream().filter(p -> !p.isIndexed()).collect(Collectors.toList());
  }

  public static List<TypeReference<Type>> getIndexedParameters(
      List<TypeReference<Type>> parameters) {
    return parameters.stream().filter(TypeReference::isIndexed).collect(Collectors.toList());
  }

  public static String typesToString(List<Type> types) throws JsonProcessingException {
    if (types == null || types.size() == 0) {
      return "";
    }
    return ObjectMapperFactory.getObjectMapper().writeValueAsString(
        types.stream()
            .map(t -> typeValueToString(t.getValue()))
            .collect(Collectors.toList())
    );
  }

  public static String typeValueToString(Object value) {
    if (value instanceof byte[]) {
      return byteToHex((byte[]) value);
    }
    if (value instanceof List) {
      try {
        return typesToString((List<Type>) value);
      } catch (JsonProcessingException e) {
        return "";
      }
    }
    return value.toString();
  }

  public static String byteToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  protected Optional<String> parseMethodId(Log ethLog) {
    String topic0 = ethLog.getTopics().get(0);
    return Optional.ofNullable(methodIdByFullHex.get(topic0));
  }

  protected Optional<List<TypeReference<Type>>> findParameters(String methodId) {
    return Optional.ofNullable(parametersByMethodId.get(methodId));
  }

  public EthTransactionI decodeInputData(Transaction transaction) {
    String data = transaction.getInput();
    // Corporate 2/9/21 -- Changed length from 74 to 10 to decode txns that only have method id.
    if (data.length() < 10) {
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

  public abstract EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction);

  public static String createMethodId(String name, List<TypeReference<Type>> parameters) {
    return methodSignatureToShortHex(createMethodSignature(name, parameters));
  }

  public static String createMethodFullHex(String name, List<TypeReference<Type>> parameters) {
    return methodSignatureToFullHex(createMethodSignature(name, parameters));
  }

  public static String methodSignatureToShortHex(String methodSignature) {
    return methodSignatureToFullHex(methodSignature).substring(0, 10);
  }

  static String createMethodSignature(String name, List<TypeReference<Type>> parameters) {
    StringBuilder result = new StringBuilder();
    result.append(name);
    result.append("(");
    String params =
        parameters.stream().map(MethodDecoder::getTypeName).collect(Collectors.joining(","));
    result.append(params);
    result.append(")");
    return result.toString();
  }

  static <T extends Type> String getTypeName(TypeReference<T> typeReference) {
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

  static <T extends Type, U extends Type> String getParameterizedTypeName(
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
      } else if (type.getSuperclass().equals(StaticArray.class)) {
        Class<U> parameterizedType = getParameterizedTypeFromArray(typeReference);
        String parameterizedTypeName = getSimpleTypeName(parameterizedType);
        int size = Integer.parseInt(
            type.getSimpleName().replace("StaticArray", ""));
        return parameterizedTypeName + "[" + size + "]";
      } else {
        throw new UnsupportedOperationException("Invalid type provided " + type.getName());
      }
    } catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException("Invalid class reference provided", e);
    }
  }

  static String getSimpleTypeName(Class<?> type) {
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
  static <T extends Type> Class<T> getParameterizedTypeFromArray(TypeReference typeReference)
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

    private void initParameters() {
        if (parametersByMethodId.isEmpty()) {
            Map<String, List<TypeReference<Type>>> parameters = new HashMap<>();
            try {
                parameters = CommonMethods.getMethods();
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
