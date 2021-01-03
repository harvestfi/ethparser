package pro.belbix.ethparser.web3.harvest.decoder;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.ImportantEventsTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ImportantEventsLogDecoder extends MethodDecoder {

    public ImportantEventsTx decode(Log log) {
        if (!isValidLog(log)) {
            return null;
        }
        String topic0 = log.getTopics().get(0);
        String methodId = methodIdByFullHex.get(topic0);

        if (methodId == null) {
            throw new IllegalStateException("Unknown topic " + topic0);
        }
        String methodName = methodNamesByMethodId.get(methodId);

        List<TypeReference<Type>> parameters = parametersByMethodId.get(methodId);
        if (parameters == null) {
            throw new IllegalStateException("Not found parameters for topic " + topic0 + " with " + methodId);
        }

        List<Type> types = extractLogIndexedValues(log, parameters);
        ImportantEventsTx tx = new ImportantEventsTx();
        tx.setLogId(log.getLogIndex().toString());
        tx.setHash(log.getTransactionHash());
        tx.setMethodName(methodName);
        tx.setBlock(log.getBlockNumber().longValue());
        tx.setVault(log.getAddress());
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
        if ("Mint".equals(tx.getMethodName())) {
            tx.setMintAmount((BigInteger) types.get(0).getValue());
        }
    }

    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodID, Transaction transaction) {
        throw new UnsupportedOperationException();
    }
}
