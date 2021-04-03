package pro.belbix.ethparser.web3.uniswap.decoder;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_UNI_PAIRS;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.model.EthTransactionI;
import pro.belbix.ethparser.model.UniswapTx;
import pro.belbix.ethparser.web3.MethodDecoder;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UniswapLpLogDecoder extends MethodDecoder {
  private static final ContractUtils contractUtils = new ContractUtils(ETH_NETWORK);
  private static final Set<String> allowedMethods = new HashSet<>(
      Arrays.asList("Mint", "Burn", "Swap"));

  public void decode(UniswapTx tx, Log ethLog) {
    if (!isValidLog(ethLog)) {
      return;
    }
    String topic0 = ethLog.getTopics().get(0);
    String methodId = methodIdByFullHex.get(topic0);

    if (methodId == null) {
      throw new IllegalStateException("Unknown topic " + topic0);
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
    tx.setCoinAddress(contractUtils.findKeyTokenForUniPair(ethLog.getAddress())
        .orElseThrow(
            () -> new IllegalStateException("Not found key token for " + ethLog.getAddress())));
    tx.setLpAddress(ethLog.getAddress());
    tx.setMethodName(methodName);
    enrich(types, methodName, tx, ethLog);
  }

  private boolean isValidLog(Log log) {
    if (log == null || log.getTopics() == null || log.getTopics().isEmpty()) {
      return false;
    }
    return PARSABLE_UNI_PAIRS.contains(log.getAddress());
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
            (amount1In.equals(BigInteger.ZERO) && firstTokenIsKey(log.getAddress()))
                || (amount0In.equals(BigInteger.ZERO) && !firstTokenIsKey(log.getAddress()))
        ) {
          tx.setBuy(false);
          tx.setCoinIn(new Address(mapLpAddressToCoin(log.getAddress())));
          tx.setCoinOut(new Address(mapLpAddressToOtherCoin(log.getAddress())));
        } else if (
            (amount0In.equals(BigInteger.ZERO) && firstTokenIsKey(log.getAddress()))
                || (amount1In.equals(BigInteger.ZERO) && !firstTokenIsKey(log.getAddress()))
        ) {
          tx.setBuy(true);
          tx.setCoinIn(new Address(mapLpAddressToOtherCoin(log.getAddress())));
          tx.setCoinOut(new Address(mapLpAddressToCoin(log.getAddress())));
        } else {
          throw new IllegalStateException("Wrong amount in " + log);
        }

        return;
      case "Mint":
        tx.setType(UniswapTx.ADD_LIQ);
        tx.setBuy(true);
        tx.setCoinIn(new Address(mapLpAddressToOtherCoin(log.getAddress())));
        tx.setCoinOut(new Address(mapLpAddressToCoin(log.getAddress())));
        if (firstTokenIsKey(log.getAddress())) {
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
        tx.setCoinIn(new Address(mapLpAddressToCoin(log.getAddress())));
        tx.setCoinOut(new Address(mapLpAddressToOtherCoin(log.getAddress())));
        if (firstTokenIsKey(log.getAddress())) {
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

  public static boolean firstTokenIsKey(String lpAddress) {
    Tuple2<String, String> tokens = contractUtils.tokenAddressesByUniPairAddress(lpAddress);
    String keyCoin = contractUtils.findKeyTokenForUniPair(lpAddress)
        .orElseThrow(() -> new IllegalStateException("Key coin not found for " + lpAddress));
    if (tokens.component1().equalsIgnoreCase(keyCoin)) {
      return true;
    } else if (tokens.component2().equalsIgnoreCase(keyCoin)) {
      return false;
    } else {
      throw new IllegalStateException("Not found key name in lp " + lpAddress);
    }
  }

  private static String mapLpAddressToOtherCoin(String address) {
    return mapLpAddress(address, false);
  }

  private static String mapLpAddressToCoin(String address) {
    return mapLpAddress(address, true);
  }

  private static String mapLpAddress(String address, boolean isKeyCoin) {
    String keyCoinAdr = contractUtils.findKeyTokenForUniPair(address)
        .orElseThrow(() -> new IllegalStateException("Key coin not found for " + address));
    Tuple2<String, String> tokensAdr = contractUtils.tokenAddressesByUniPairAddress(address);

    int i;
    if (tokensAdr.component1().equalsIgnoreCase(keyCoinAdr)) {
      i = 1;
    } else if (tokensAdr.component2().equalsIgnoreCase(keyCoinAdr)) {
      i = 2;
    } else {
      throw new IllegalStateException("Key coin not found in " + tokensAdr);
    }
    if (isKeyCoin) {
      return getStringFromPair(tokensAdr, i, false);
    } else {
      return getStringFromPair(tokensAdr, i, true);
    }
  }

  private static String getStringFromPair(Tuple2<String, String> pair, int i, boolean inverse) {
    if (i == 1) {
      if (inverse) {
        return pair.component2();
      } else {
        return pair.component1();
      }
    } else if (i == 2) {
      if (inverse) {
        return pair.component1();
      } else {
        return pair.component2();
      }
    } else {
      throw new IllegalStateException("Wrong index for pair " + i);
    }
  }

  @Override
  public EthTransactionI mapTypesToModel(List<Type> types, String methodID,
      Transaction transaction) {
    throw new UnsupportedOperationException();
  }
}
