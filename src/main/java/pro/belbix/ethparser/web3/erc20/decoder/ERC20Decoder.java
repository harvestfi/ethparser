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

    public TokenTx decode(Log log) {
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
        TokenTx tx = new TokenTx();
        tx.setLogId(log.getLogIndex().toString());
        tx.setHash(log.getTransactionHash());
        tx.setMethodName(methodName);
        tx.setBlock(log.getBlockNumber().longValue());
        tx.setBlockHash(log.getBlockHash());
        tx.setTokenAddress(log.getAddress());
        enrich(types, tx);
        return tx;
    }

    private boolean isValidLog(Log log) {
        return log != null && !log.getTopics().isEmpty();
    }

    private void enrich(List<Type> types, TokenTx tx) {
        if ("Transfer".equals(tx.getMethodName())) {
            tx.setOwner((String) types.get(0).getValue());
            tx.setRecipient((String) types.get(1).getValue());
            tx.setValue((BigInteger) types.get(2).getValue());
        }
    }

    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodID, Transaction transaction) {
        throw new UnsupportedOperationException();
    }

}
