package pro.belbix.ethparser.web3.deployer.decoder;

import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import pro.belbix.ethparser.model.DeployerTx;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.web3.MethodDecoder;

import java.util.List;

@SuppressWarnings({"rawtypes"})
@Component
public class DeployerDecoder extends MethodDecoder
{
    @Override
    public EthTransactionI mapTypesToModel(List<Type> types, String methodId, Transaction transaction)
    {
        DeployerTx deployerTx = new DeployerTx();
        deployerTx.setHash(transaction.getHash());
        deployerTx.setIdx(transaction.getTransactionIndex().longValue());
        deployerTx.setBlock(transaction.getBlockNumber().longValue());
        deployerTx.setToAddress(transaction.getTo());
        deployerTx.setFromAddress(transaction.getFrom());
        deployerTx.setValue(Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER));
        deployerTx.setGasLimit(transaction.getGas());
        deployerTx.setGasPrice(transaction.getGasPrice().divide(Convert.Unit.GWEI.getWeiFactor().toBigInteger()));
        String methodName = getMethodName(methodId);
        deployerTx.setType(DeployerActivityEnum.getEnumByMethodName(methodName));
        deployerTx.setMethodName(methodName);
        parseMethod(deployerTx, methodName);
        return deployerTx;
    }

    private void parseMethod(DeployerTx tx, String methodName)
    {
        DeployerActivityEnum deployerActivityEnum = DeployerActivityEnum.getEnumByMethodName(methodName);
        if (deployerActivityEnum != null)
        {
            tx.setType(deployerActivityEnum);
        }
    }

    private String getMethodName(String methodId)
    {
        String methodName = methodNamesByMethodId.get(methodId);
        if (methodName == null)
        {
            methodName = methodId;
        }
        return methodName;
    }
}
