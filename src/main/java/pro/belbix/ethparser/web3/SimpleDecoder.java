package pro.belbix.ethparser.web3;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
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

  @SuppressWarnings("rawtypes")
  public Optional<Log> findLogByPredicate(List<LogResult> results,
      Predicate<? super List<Type>> predicate) {
    TreeMap<Integer, Log> mintedBlocks = new TreeMap<>();
    for (LogResult result : results) {
      Log ethLog = (Log) result.get();
      if (decodeEthLog(ethLog).filter(predicate).isPresent()) {
        mintedBlocks.put(ethLog.getBlockNumber().intValue(), ethLog);
      }
    }
    var result = mintedBlocks.pollLastEntry();
    if (result == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(result.getValue());
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
