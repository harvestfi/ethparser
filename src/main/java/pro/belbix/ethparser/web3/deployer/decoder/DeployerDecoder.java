package pro.belbix.ethparser.web3.deployer.decoder;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import pro.belbix.ethparser.model.DeployerTx;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractConstants;

import java.util.List;

@SuppressWarnings({"rawtypes"})
@Component
@Log4j2
public class DeployerDecoder extends MethodDecoder {
  private final Web3Service web3Service;

  public DeployerDecoder(Web3Service web3Service) {
    this.web3Service = web3Service;
  }

  public DeployerTx decodeTransaction(Transaction tx) {
    DeployerTx deployerTx = null;
    try {
      if (!isValidTransaction(tx)) {
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
      TransactionReceipt transactionReceipt = web3Service.fetchTransactionReceipt(tx.getHash());
      deployerTx.setGasUsed(transactionReceipt.getGasUsed());
      deployerTx.setSuccess("0x1".equalsIgnoreCase(transactionReceipt.getStatus()));

    } catch (Exception e) {
      log.error("Error tx " + tx.getHash(), e);
    }
    return deployerTx;
  }

  private boolean isValidTransaction(Transaction tx) {
    // If deployer address ever changes -- supply a list and check here
    return ContractConstants.DEPLOYER.equalsIgnoreCase(tx.getFrom());
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
