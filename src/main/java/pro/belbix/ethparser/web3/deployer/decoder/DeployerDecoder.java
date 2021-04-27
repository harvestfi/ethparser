package pro.belbix.ethparser.web3.deployer.decoder;

import static pro.belbix.ethparser.web3.contracts.ContractConstants.DEPLOYERS;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import pro.belbix.ethparser.model.tx.DeployerTx;
import pro.belbix.ethparser.model.tx.EthTransactionI;
import pro.belbix.ethparser.web3.MethodDecoder;

@SuppressWarnings({"rawtypes"})
@Log4j2
public class DeployerDecoder extends MethodDecoder {

  public DeployerTx decodeTransaction(Transaction tx, String network) {
    DeployerTx deployerTx = null;
    try {
      if (!isValidTransaction(tx, network)) {
        return null;
      }
      if (tx.getTo() == null) {
        // contract creation
        deployerTx =
            (DeployerTx)
                mapTypesToModel(null, DeployerActivityEnum.CONTRACT_CREATION.getMethodName(), tx);
      } else if ("0x".equalsIgnoreCase(tx.getInput())) {
        // no data, probably sending eth
        deployerTx =
            (DeployerTx)
                mapTypesToModel(null, DeployerActivityEnum.NO_INPUT_DATA.getMethodName(), tx);
      } else {
        // everything else
        try {
          deployerTx = (DeployerTx) decodeInputData(tx);
        } catch (IllegalStateException ise) {
          // unknown tx
          deployerTx = (DeployerTx) mapTypesToModel(null, tx.getInput().substring(0, 10), tx);
          log.warn(
              "Unknown tx methodId: "
                  + deployerTx.getMethodName()
                  + " hash: "
                  + deployerTx.getHash());
        }
      }
    } catch (Exception e) {
      log.error("Error tx " + tx.getHash(), e);
    }
    return deployerTx;
  }

  private boolean isValidTransaction(Transaction tx, String network) {
    // If deployer address ever changes -- supply a list and check here
    return DEPLOYERS.get(network).equalsIgnoreCase(tx.getFrom());
  }

  @Override
  public EthTransactionI mapTypesToModel(
      List<Type> types, String methodId, Transaction transaction) {
    DeployerTx deployerTx = new DeployerTx();
    deployerTx.setHash(transaction.getHash());
    deployerTx.setIdx(transaction.getTransactionIndex().longValue());
    deployerTx.setBlock(transaction.getBlockNumber().longValue());
    deployerTx.setToAddress(transaction.getTo());
    deployerTx.setFromAddress(transaction.getFrom());
    deployerTx.setValue(Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER));
    deployerTx.setGasLimit(transaction.getGas());
    deployerTx.setGasPrice(
        transaction.getGasPrice().divide(Convert.Unit.GWEI.getWeiFactor().toBigInteger()));
    String methodName = getMethodName(methodId);
    deployerTx.setType(DeployerActivityEnum.getEnumByMethodName(methodName));
    deployerTx.setMethodName(methodName);
    parseMethod(deployerTx, methodName);
    return deployerTx;
  }

  private void parseMethod(DeployerTx tx, String methodName) {
    DeployerActivityEnum deployerActivityEnum =
        DeployerActivityEnum.getEnumByMethodName(methodName);
    if (deployerActivityEnum != null) {
      tx.setType(deployerActivityEnum);
    }
  }

  private String getMethodName(String methodId) {
    String methodName = methodNamesByMethodId.get(methodId);
    if (methodName == null) {
      methodName = methodId;
    }
    return methodName;
  }
}
