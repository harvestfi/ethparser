package pro.belbix.ethparser.web3.erc20.decoder;

import java.math.BigInteger;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.TokenTx;
import pro.belbix.ethparser.web3.MethodDecoder;

public class ERC20Decoder extends MethodDecoder {

  public TokenTx decode(Log ethLog) {
    if (!isValidLog(ethLog)) {
      return null;
    }

    String methodId = parseMethodId(ethLog).orElse("");
    String methodName = methodNamesByMethodId.get(methodId);
    List<TypeReference<Type>> parameters = findParameters(methodId).orElse(List.of());

    List<Type> types = extractLogIndexedValues(ethLog, parameters);
    TokenTx tx = new TokenTx();
    tx.setLogId(ethLog.getLogIndex().toString());
    tx.setHash(ethLog.getTransactionHash());
    tx.setMethodName(methodName);
    tx.setBlock(ethLog.getBlockNumber().longValue());
    tx.setBlockHash(ethLog.getBlockHash());
    tx.setTokenAddress(ethLog.getAddress());
    enrich(types, tx);
    return tx;
  }

  private boolean isValidLog(Log log) {
    return log != null && !log.getTopics().isEmpty();
  }

  private void enrich(List<Type> types, TokenTx tx) {
    if (types == null) {
      return;
    }
    if ("Transfer".equals(tx.getMethodName())) {
      tx.setOwner((String) types.get(0).getValue());
      tx.setRecipient((String) types.get(1).getValue());
      tx.setValue((BigInteger) types.get(2).getValue());
    }
  }

  public String decodeMethodName(String input) {
    if (input == null) {
      return null;
    }
    if (input.length() < 10) {
      return null;
    }
    String methodID = input.substring(0, 10);

    List<TypeReference<Type>> parameters = parametersByMethodId.get(methodID);
    if (parameters == null) {
      return null;
    }
    return methodNamesByMethodId.get(methodID);
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }

}
