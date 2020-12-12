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
import pro.belbix.ethparser.model.HardWorkTx;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"unchecked", "rawtypes"})
public class HardWorkLogDecoder extends MethodDecoder {

    public HardWorkTx decode(Log log) {
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
        HardWorkTx tx = new HardWorkTx();
        tx.setLogId(log.getLogIndex().toString());
        tx.setHash(log.getTransactionHash());
        tx.setMethodName(methodName);
        tx.setBlock(log.getBlockNumber().longValue());
        enrich(types, tx);
        return tx;
    }

    private boolean isValidLog(Log log) {
        return log != null && !log.getTopics().isEmpty();
    }

    private void enrich(List<Type> types, HardWorkTx tx) {
        if ("SharePriceChangeLog".equals(tx.getMethodName())) {
            tx.setVault((String) types.get(0).getValue());
            tx.setStrategy((String) types.get(1).getValue());
            tx.setOldSharePrice((BigInteger) types.get(2).getValue());
            tx.setNewSharePrice((BigInteger) types.get(3).getValue());
            tx.setBlockDate(((BigInteger) types.get(4).getValue()).longValue());
        } else if ("ProfitLogInReward".equals(tx.getMethodName())) {
            tx.setProfitAmount((BigInteger) types.get(0).getValue());
            tx.setFeeAmount((BigInteger) types.get(1).getValue());
        }
    }

    private static BigInteger[] parseInts(Type type) {
        List values = ((List) type.getValue());
        BigInteger[] integers = new BigInteger[values.size()];
        int i = 0;
        for (Object v : values) {
            integers[i] = (BigInteger) v;
            i++;
        }
        return integers;
    }

    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodID, Transaction transaction) {
        throw new UnsupportedOperationException();
    }
}
