package pro.belbix.ethparser.web3;

import static org.web3j.abi.FunctionReturnDecoder.decodeIndexedValue;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.tx.EthTransactionI;

@Component
@Slf4j
public class SimpleDecoder extends MethodDecoder {

  public Optional<List<Type>> decodeEthLog(Log ethLog) {
    return parseMethodId(ethLog)
        .flatMap(this::findParameters)
        .map(parameters ->
            extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), parameters));
  }

  public Optional<List<Type>> decodeEthLogForDepositAndWithdraw(Log ethLog, String network, int paramSize) {
    return parseMethodId(ethLog)
        .flatMap(this::findParameters)
        .map(parameters ->
            extractLogIndexedValuesWrapped(ethLog.getTopics(), ethLog.getData(), parameters, network, paramSize));
  }

  // deposit and withdraw
  public List<Type> decodeOnlyTopics(Log ethLog) {
    try {
      return extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), List.of(
          TypeReference.makeTypeReference("address", true, false),
          TypeReference.makeTypeReference("uint256", true, false)
      ));
    } catch (Exception e) {
      log.error("Error during decodeOnlyTopics", e);
      return List.of();
    }
  }

  // deposit and withdraw
  public List<Type> decodeOnlyData(List<String> topics, String data, int paramSize) {
    try {
      return paramSize > 2 ?
          extractLogIndexedValues(topics, data, List.of(
              TypeReference.makeTypeReference("address", false, false),
              TypeReference.makeTypeReference("uint256", false, false),
              TypeReference.makeTypeReference("uint256", false, false)
          )) :
          extractLogIndexedValues(topics, data, List.of(
              TypeReference.makeTypeReference("address", false, false),
              TypeReference.makeTypeReference("uint256", false, false)
          ));
    } catch (Exception e) {
      log.error("Error during decodeOnlyTopics", e);
      return List.of();
    }
  }

  public List<Type> extractLogIndexedValuesWrapped(List<String> topics, String data, List<TypeReference<Type>> parameters, String network, int paramSize) {
    try {
      return extractLogIndexedValues(topics, data, parameters);
    } catch (Exception e) {
      log.error("Get error during parse log: {}", e.getMessage());
      return BSC_NETWORK.equals(network) ?
          decodeOnlyData(topics, data, paramSize) :
          extractLogIndexValues(topics, data);
    }
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

  // method only for deposit and withdraw
  private List<Type> extractLogIndexValues(List<String> topics, String data) {
    try {
      var values = new ArrayList<Type>();

      List<TypeReference<Type>> parameters = List.of(
          TypeReference.makeTypeReference("uint256"),
          TypeReference.makeTypeReference("uint256")
      );

      if (data == null) {
        return values;
      }

      if (topics == null || topics.size() < 2) {
        log.error("Can not extract logs for uniswapv3, topics - {}", topics);
        return values;
      }

      values.add(
          decodeIndexedValue(topics.get(1), TypeReference.makeTypeReference("address", true, false))
      );

      values.addAll(
          FunctionReturnDecoder.decode(data, getNonIndexedParameters(parameters))
      );

      return values;
    } catch (Exception e) {
      log.error("Can not extract logs for uniswapv3", e);
      throw new IllegalStateException(e);
    }
  }
}
