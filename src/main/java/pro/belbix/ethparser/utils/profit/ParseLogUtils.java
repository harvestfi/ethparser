package pro.belbix.ethparser.utils.profit;

import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.DEPOSIT;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.DEPOSIT_UNI;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.TRANSFER;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.WITHDRAW;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.WITHDRAW_UNI;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ZERO_ADDRESS;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransaction;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType;
import pro.belbix.ethparser.error.exceptions.CanNotFetchPriceException;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem.CovalenthqTransactionHistoryItemLog;
import pro.belbix.ethparser.web3.SimpleDecoder;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.DecodeExcludeConstants;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class ParseLogUtils {
  private static final Map<String, CovalenthqVaultTransactionType> LOG_EVENTS_V1 = Map.of(
      "0xf279e6a1f5e320cca91135676d9cb6e44ca8a08c0b88342bcdb1144f6511b568", WITHDRAW_UNI,
      "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364", WITHDRAW,
      "0x90890809c654f11d6e72a28fa60149770a0d11ec6c92319d6ceb2bb0a4ea1a15", DEPOSIT_UNI,
      "0xe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c", DEPOSIT
  );

  private static final Map<String, CovalenthqVaultTransactionType> LOG_EVENTS_V2 = Map.of(
      "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef", TRANSFER
  );

  FunctionsUtils functionsUtils;
  SimpleDecoder simpleDecoder;


  // filter deposit and withdraw
  // filter by contractAddress
  public boolean isCorrectLog(CovalenthqTransactionHistoryItemLog log, String contractAddress) {
    return log.getTopics() != null
        && !log.getTopics().isEmpty()
        && log.getSenderAddress().equalsIgnoreCase(contractAddress)
        && LOG_EVENTS_V2.get(log.getTopics().get(0)) != null;
  }

  // only for event logs deposit and withdraw
  public CovalenthqVaultTransaction toCovalenthqVaultTransactionFromDepositOrWithdraw(CovalenthqTransactionHistoryItemLog item,
      String network, String contractAddress) {
    try {
      var transaction = new CovalenthqVaultTransaction();
      var covalenthqType = Optional.ofNullable(toCovalenthqVaultTransactionTypeFromDepositOrWithdraw(item))
          .orElseThrow(() -> {
            log.error("Can not find log event name");
            throw new IllegalStateException();
          });
      var value = getParamValue(item, covalenthqType.value, contractAddress, network, covalenthqType.paramSize);
      var decimal = item.getContractDecimal() == 0
          ? functionsUtils.getDecimal(contractAddress, network)
          : item.getContractDecimal();

      transaction.setNetwork(network);
      transaction.setBlock(item.getBlockHeight());
      transaction.setTransactionHash(item.getTransactionHash());
      transaction.setContractDecimal(decimal);
      transaction.setContractAddress(contractAddress);
      transaction.setType(covalenthqType.type);
      transaction.setOwnerAddress(getParamValue(item, covalenthqType.address, contractAddress, network, covalenthqType.paramSize));
      transaction.setValue(
          StringUtils.isEmpty(value) ? BigDecimal.ZERO : new BigDecimal(value)
      );
      transaction.setSignedAt(item.getSignedAt());

      return transaction;
    } catch (Exception e) {
      log.error("Can not parse covalenthq log: {}", item, e);
      throw new CanNotFetchPriceException();
    }
  }

  // only for event logs transfer
  public List<CovalenthqVaultTransaction> toCovalenthqVaultTransactionFromTransfer(CovalenthqTransactionHistoryItemLog item,
      String network, String contractAddress) {
    try {
      var covalenthqType = Optional.ofNullable(toCovalenthqVaultTransactionTypeFromTransfer(item))
          .orElseThrow(() -> {
            log.error("Can not find log event name");
            throw new IllegalStateException();
          });
      var value = getParamValue(item, covalenthqType.value);
      var fromAddress = getParamValue(item, covalenthqType.address);
      var toAddress = getParamValue(item, covalenthqType.toAddress);

      var transactions = new ArrayList<CovalenthqVaultTransaction>();

      if (StringUtils.isNotEmpty(fromAddress) && !ZERO_ADDRESS.equalsIgnoreCase(fromAddress)) {
        transactions.add(toCovalenthqVaultTransaction(item, contractAddress, network, WITHDRAW.type, fromAddress, value));
      }

      if (StringUtils.isNotEmpty(toAddress) && !ZERO_ADDRESS.equalsIgnoreCase(toAddress)) {
        transactions.add(toCovalenthqVaultTransaction(item, contractAddress, network, DEPOSIT.type, toAddress, value));
      }

      return transactions;
    } catch (Exception e) {
      log.error("Can not parse covalenthq log: {}", item, e);
      throw new CanNotFetchPriceException();
    }
  }

  private CovalenthqVaultTransaction toCovalenthqVaultTransaction(CovalenthqTransactionHistoryItemLog item, String contractAddress,
      String network, String type, String ownerAddress, String value) {
    var transaction = new CovalenthqVaultTransaction();
    var decimal = item.getContractDecimal() == 0
        ? functionsUtils.getDecimal(contractAddress, network)
        : item.getContractDecimal();

    transaction.setNetwork(network);
    transaction.setBlock(item.getBlockHeight());
    transaction.setTransactionHash(item.getTransactionHash());
    transaction.setContractDecimal(decimal);
    transaction.setContractAddress(contractAddress);
    transaction.setType(type);
    transaction.setOwnerAddress(ownerAddress);
    transaction.setValue(
        StringUtils.isEmpty(value) ? BigDecimal.ZERO : new BigDecimal(value)
    );
    transaction.setSignedAt(item.getSignedAt());

    return transaction;
  }

  private CovalenthqVaultTransactionType toCovalenthqVaultTransactionTypeFromDepositOrWithdraw(CovalenthqTransactionHistoryItemLog log) {
    if (log.getTopics() != null && !log.getTopics().isEmpty()) {
      return LOG_EVENTS_V1.get(log.getTopics().get(0));
    }

    return null;
  }

  private CovalenthqVaultTransactionType toCovalenthqVaultTransactionTypeFromTransfer(CovalenthqTransactionHistoryItemLog log) {
    if (log.getTopics() != null && !log.getTopics().isEmpty()) {
      return LOG_EVENTS_V2.get(log.getTopics().get(0));
    }

    return null;
  }

  private String getParamValue(CovalenthqTransactionHistoryItemLog item, String param, String address, String network, int paramSize) {
    var ethLog = new Log();
    ethLog.setTopics(item.getTopics());
    ethLog.setData(item.getData());

    List<Type> result = simpleDecoder.decodeEthLogForDepositAndWithdraw(ethLog, network, paramSize)
        .orElseThrow();

    if (ethLog.getData() == null && DecodeExcludeConstants.DECODE_ONLY_TOPICS.get(ETH_NETWORK).contains(address.toLowerCase())) {
      ethLog.setData(StringUtils.EMPTY);
      result = simpleDecoder.decodeOnlyTopics(ethLog);
    }

    return getParamValue(param, result);
  }

  private String getParamValue(CovalenthqTransactionHistoryItemLog item, String param) {
    var ethLog = new Log();
    ethLog.setTopics(item.getTopics());
    ethLog.setData(item.getData());

    List<Type> result = simpleDecoder.decodeEthLog(ethLog)
        .orElseThrow();

    return getParamValue(param, result);
  }

  private String getParamValue(String param, List<Type> result) {

    var indexParam = -1;
    var castToString = false;

    switch (param) {
      case "account":
      case "user":
      case "dst":
      case "provider":
      case "from":
        indexParam = 0;
        castToString = true;
        break;
      case "wad":
      case "value":
        indexParam = 1;
        break;
      case "to":
        indexParam = 1;
        castToString = true;
        break;
      case "amount" :
      case "valueTransfer":
      case "writeAmount":
        indexParam = 2;
        break;
    }
    if (result.isEmpty() || indexParam == -1 || indexParam >= result.size()) {
      log.error("Unknown param or can not parse data");
      throw new IllegalStateException();
    }

    return castToString
        ? (String) result.get(indexParam).getValue()
        : ((BigInteger) result.get(indexParam).getValue()).toString();
  }
}
