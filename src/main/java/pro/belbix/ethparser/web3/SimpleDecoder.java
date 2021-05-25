package pro.belbix.ethparser.web3;

import java.util.List;
import java.util.Optional;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.tx.EthTransactionI;

public class SimpleDecoder extends MethodDecoder {

  public Optional<List<Type>> decodeEthLog(Log ethLog) {
    return parseMethodId(ethLog)
        .flatMap(this::findParameters)
        .map(parameters ->
            extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), parameters));
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
