package pro.belbix.ethparser.web3.prices.decoder;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.PriceTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"rawtypes"})
@Log4j2
public class PriceDecoder extends MethodDecoder {
  private static final Set<String> allowedMethods = new HashSet<>(Arrays.asList("Swap"));

  public PriceTx decode(Log ethLog) {
    String methodId = parseMethodId(ethLog)
        .orElse(null);
    if (methodId == null) {
      return null;
    }
    String methodName = methodNamesByMethodId.get(methodId);
    List<TypeReference<Type>> parameters;
    try {
      parameters = findParameters(methodId)
          .orElseThrow(() -> new IllegalStateException("Not found parameters for " + methodId));
    } catch (IllegalStateException e) {
      log.warn("Can't parse parameters {}", ethLog);
      return null;
    }
    if (!allowedMethods.contains(methodName)) {
      return null;
    }

    List<Type> types = extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), parameters);
    PriceTx tx = new PriceTx();
    tx.setHash(ethLog.getTransactionHash());
    tx.setLogId(ethLog.getLogIndex().longValue());
    tx.setBlock(ethLog.getBlockNumber());
    tx.setBlockHash(ethLog.getBlockHash());
    tx.setSource(ethLog.getAddress());
    tx.setMethodName(methodName);
    enrich(types, tx);
    return tx;
  }

  private void enrich(List<Type> types, PriceTx tx) {
    if ("Swap".equals(tx.getMethodName())) {
      BigInteger[] integers = new BigInteger[4];
      String[] addresses = new String[2];
      addresses[0] = (String) types.get(0).getValue(); // sender
      addresses[1] = (String) types.get(1).getValue(); // to
      integers[0] = (BigInteger) types.get(2).getValue(); // amount0In
      integers[1] = (BigInteger) types.get(3).getValue(); // amount1In
      integers[2] = (BigInteger) types.get(4).getValue(); // amount0Out
      integers[3] = (BigInteger) types.get(5).getValue(); // amount1Out
      tx.setAddresses(addresses);
      tx.setIntegers(integers);
    }
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
