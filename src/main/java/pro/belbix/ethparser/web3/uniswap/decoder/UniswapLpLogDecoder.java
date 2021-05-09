package pro.belbix.ethparser.web3.uniswap.decoder;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.model.tx.EthTransactionI;
import pro.belbix.ethparser.model.tx.UniswapTx;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
@Log4j2
public class UniswapLpLogDecoder extends MethodDecoder {
  private static final Set<String> allowedMethods = new HashSet<>(
      Arrays.asList("Mint", "Burn", "Swap"));

  public void decode(UniswapTx tx, Log ethLog) {
    if (!isValidLog(ethLog)) {
      return;
    }
    String topic0 = ethLog.getTopics().get(0);
    String methodId = methodIdByFullHex.get(topic0);

    if (methodId == null) {
      log.warn("Unknown topic " + topic0);
      return;
    }
    String methodName = methodNamesByMethodId.get(methodId);

    if (!allowedMethods.contains(methodName)) {
      return;
    }

    List<TypeReference<Type>> parameters = parametersByMethodId.get(methodId);
    if (parameters == null) {
      throw new IllegalStateException(
          "Not found parameters for topic " + topic0 + " with " + methodId);
    }

    List<Type> types = extractLogIndexedValues(ethLog.getTopics(), ethLog.getData(), parameters);
    tx.setHash(ethLog.getTransactionHash());
    tx.setLogId(ethLog.getLogIndex().longValue());
    tx.setBlock(ethLog.getBlockNumber());
    tx.setSuccess(true);
    tx.setLpAddress(ethLog.getAddress());
    tx.setMethodName(methodName);
    enrich(types, methodName, tx, ethLog);
  }

  private boolean isValidLog(Log ethLog) {
    if (ethLog == null || ethLog.getTopics() == null || ethLog.getTopics().isEmpty()) {
      return false;
    }
    return ContractUtils.isFullParsableLp(ethLog.getAddress(), ETH_NETWORK);
  }

  private void enrich(List<Type> types, String methodName, UniswapTx tx, Log log) {
    switch (methodName) {
      case "Swap":
        tx.setType(UniswapTx.SWAP);
        BigInteger amount0In = (BigInteger) types.get(2).getValue();
        BigInteger amount1In = (BigInteger) types.get(3).getValue();
        BigInteger amount0Out = (BigInteger) types.get(4).getValue();
        BigInteger amount1Out = (BigInteger) types.get(5).getValue();

        if (!amount0In.equals(BigInteger.ZERO)) {
          tx.setAmountIn(amount0In);
        } else if (!amount1In.equals(BigInteger.ZERO)) {
          tx.setAmountIn(amount1In);
        } else {
          throw new IllegalStateException("Zero amountIn for " + tx.getHash());
        }
        if (!amount0Out.equals(BigInteger.ZERO)) {
          tx.setAmountOut(amount0Out);
        } else if (!amount1Out.equals(BigInteger.ZERO)) {
          tx.setAmountOut(amount1Out);
        } else {
          throw new IllegalStateException("Zero amountOut for " + tx.getHash());
        }

        if (
            (amount1In.equals(BigInteger.ZERO) && tx.isFirstTokenIsKey())
                || (amount0In.equals(BigInteger.ZERO) && !tx.isFirstTokenIsKey())
        ) {
          tx.setBuy(false);
          tx.setCoinIn(new Address(tx.getCoin()));
          tx.setCoinOut(new Address(tx.getOtherCoin()));
        } else if (
            (amount0In.equals(BigInteger.ZERO) && tx.isFirstTokenIsKey())
                || (amount1In.equals(BigInteger.ZERO) && !tx.isFirstTokenIsKey())
        ) {
          tx.setBuy(true);
          tx.setCoinIn(new Address(tx.getOtherCoin()));
          tx.setCoinOut(new Address(tx.getCoin()));
        } else {
          throw new IllegalStateException("Wrong amount in " + log);
        }

        return;
      case "Mint":
        tx.setType(UniswapTx.ADD_LIQ);
        tx.setBuy(true);
        tx.setCoinIn(new Address(tx.getOtherCoin()));
        tx.setCoinOut(new Address(tx.getCoin()));
        if (tx.isFirstTokenIsKey()) {
          tx.setAmountOut((BigInteger) types.get(1).getValue());
          tx.setAmountIn((BigInteger) types.get(2).getValue());
        } else {
          tx.setAmountIn((BigInteger) types.get(1).getValue());
          tx.setAmountOut((BigInteger) types.get(2).getValue());
        }

        return;
      case "Burn":
        tx.setType(UniswapTx.REMOVE_LIQ);
        tx.setBuy(false);
        tx.setCoinIn(new Address(tx.getCoin()));
        tx.setCoinOut(new Address(tx.getOtherCoin()));
        if (tx.isFirstTokenIsKey()) {
          tx.setAmountIn((BigInteger) types.get(2).getValue());
          tx.setAmountOut((BigInteger) types.get(3).getValue());
        } else {
          tx.setAmountOut((BigInteger) types.get(2).getValue());
          tx.setAmountIn((BigInteger) types.get(3).getValue());
        }

        return;
    }
    throw new IllegalStateException("Unknown method " + methodName);
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
