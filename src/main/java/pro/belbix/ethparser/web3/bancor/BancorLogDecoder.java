package pro.belbix.ethparser.web3.bancor;

import static pro.belbix.ethparser.model.tx.BancorPriceTx.BNT;
import static pro.belbix.ethparser.model.tx.BancorPriceTx.FARM;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BANCOR_CONVERSION_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstantsV7.FARM_TOKEN;

import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.tx.BancorPriceTx;
import pro.belbix.ethparser.model.tx.EthTransactionI;
import pro.belbix.ethparser.web3.MethodDecoder;

@Service
@Log4j2
public class BancorLogDecoder extends MethodDecoder {

  public BancorPriceTx decode(Log ethLog) {
    if (!isValidLog(ethLog)) {
      return null;
    }
    String topic0 = ethLog.getTopics().get(0);
    String methodId = methodIdByFullHex.get(topic0);

    if (methodId == null) {
      log.warn("Unknown topic " + topic0);
      return null;
    }
    String methodName = methodNamesByMethodId.get(methodId);

    if (!methodName.equals("Conversion")) {
      return null;
    }

    List<TypeReference<Type>> parameters = parametersByMethodId.get(methodId);
    if (parameters == null) {
      throw new IllegalStateException(
          "Not found parameters for topic " + topic0 + " with " + methodId);
    }

    List<Type> types = extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), parameters);
    BancorPriceTx tx = new BancorPriceTx();
    tx.setHash(ethLog.getTransactionHash());
    tx.setLogId(ethLog.getLogIndex().longValue());
    tx.setBlock(ethLog.getBlockNumber().longValue());
    tx.setType(BancorOperationEnum.CONVERSION);
    String sourceToken = (String) types.get(1).getValue();
    String targetToken = (String) types.get(2).getValue();
    BigInteger sourceAmount = (BigInteger) types.get(3).getValue();
    BigInteger targetAmount = (BigInteger) types.get(4).getValue();

    if (sourceToken.toLowerCase().equals(FARM_TOKEN)) {
      tx.setFarmAsSource(true);
      tx.setAmountFarm(sourceAmount);
      tx.setAmountBnt(targetAmount);
      tx.setCoin(FARM);//FARM->BNT
      tx.setCoinAddress(FARM_TOKEN);
      tx.setOtherCoin(BNT);
      tx.setOtherCoinAddress(BANCOR_CONVERSION_ADDRESS);
      return tx;
    }

    if (targetToken.toLowerCase().equals(FARM_TOKEN)) {
      tx.setFarmAsSource(false);
      tx.setAmountFarm(targetAmount);
      tx.setAmountBnt(sourceAmount);
      tx.setCoin(BNT);//BNT->FARM
      tx.setCoinAddress(BANCOR_CONVERSION_ADDRESS);
      tx.setOtherCoin(FARM);
      tx.setOtherCoinAddress(FARM_TOKEN);
      return tx;
    }

    return null;
  }

  private boolean isValidLog(Log ethLog) {
    if (ethLog == null || ethLog.getTopics() == null || ethLog.getTopics().isEmpty()) {
      return false;
    }
    return true;
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
