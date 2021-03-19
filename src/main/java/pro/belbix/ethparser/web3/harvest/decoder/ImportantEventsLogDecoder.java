package pro.belbix.ethparser.web3.harvest.decoder;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.ImportantEventsTx;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.contracts.ContractConstants;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ImportantEventsLogDecoder extends MethodDecoder {

  private static final Set<String> allowedMethods = new HashSet<>(
      Arrays.asList("StrategyChanged", "StrategyAnnounced", "Transfer")
  );

  public ImportantEventsTx decode(Log ethLog) {
    if (!isValidLog(ethLog)) {
      return null;
    }
    String methodId = parseMethodId(ethLog).orElse("");
    String methodName = methodNamesByMethodId.get(methodId);
    List<TypeReference<Type>> parameters = findParameters(methodId).orElse(List.of());

    if (!allowedMethods.contains(methodName)) {
      return null;
    }

    List<Type> types = extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), parameters);

    // Mint function emits only Transfer event from zero address
    // and we want only FARM token
    if ("Transfer".equals(methodName)) {
      if (!ContractConstants.FARM_TOKEN.equals(ethLog.getAddress()) || !types.get(0).getValue()
          .equals(ZERO_ADDRESS)) {
        return null;
      }
    }

    ImportantEventsTx tx = new ImportantEventsTx();
    tx.setLogId(ethLog.getLogIndex().toString());
    tx.setHash(ethLog.getTransactionHash());
    tx.setMethodName(methodName);
    tx.setBlock(ethLog.getBlockNumber().longValue());
    tx.setVault(ethLog.getAddress());
    enrich(types, tx);
    return tx;
  }

  private boolean isValidLog(Log log) {
    return log != null && !log.getTopics().isEmpty();
  }

  private void enrich(List<Type> types, ImportantEventsTx tx) {
    if ("StrategyChanged".equals(tx.getMethodName())) {
      tx.setNewStrategy((String) types.get(0).getValue());
      tx.setOldStrategy((String) types.get(1).getValue());
    }
    if ("StrategyAnnounced".equals(tx.getMethodName())) {
      tx.setNewStrategy((String) types.get(0).getValue());
    }
    if ("Transfer".equals(tx.getMethodName())) {
      tx.setMintAmount((BigInteger) types.get(2).getValue());
    }
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
