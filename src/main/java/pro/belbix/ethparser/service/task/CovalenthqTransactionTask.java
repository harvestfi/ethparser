package pro.belbix.ethparser.service.task;

import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.DEPOSIT;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.DEPOSIT_UNI;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.WITHDRAW;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.WITHDRAW_UNI;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransaction;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType;
import pro.belbix.ethparser.error.exceptions.CanNotFetchPriceException;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem.CovalenthqTransactionHistoryItemLog;
import pro.belbix.ethparser.repositories.covalenthq.CovalenthqVaultTransactionRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.service.SharePriceService;
import pro.belbix.ethparser.service.TokenPriceService;
import pro.belbix.ethparser.service.external.CovalenthqService;
import pro.belbix.ethparser.web3.SimpleDecoder;
import pro.belbix.ethparser.web3.Web3Functions;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CovalenthqTransactionTask {
  // max block range in covalenthq
  private static final int MAX_BLOCK_RANGE = 1000000;
  private static final int MINUS_BLOCK = 10;
  private static final int PAGINATION_SIZE = 1000000;
  private static final int PAGINATION_SIZE_FOR_A_LOT_OF_DATA = 1000;
  private static final Map<String, CovalenthqVaultTransactionType> LOG_EVENTS = Map.of(
      "0xf279e6a1f5e320cca91135676d9cb6e44ca8a08c0b88342bcdb1144f6511b568", WITHDRAW_UNI,
      "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364", WITHDRAW,
      "0x90890809c654f11d6e72a28fa60149770a0d11ec6c92319d6ceb2bb0a4ea1a15", DEPOSIT_UNI,
      "0xe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c", DEPOSIT
  );

  SimpleDecoder simpleDecoder;
  VaultRepository vaultRepository;
  CovalenthqService covalenthqService;
  CovalenthqVaultTransactionRepository covalenthqVaultTransactionRepository;
  Web3Functions web3Functions;
  SharePriceService sharePriceService;
  TokenPriceService tokenPriceService;


  // everyday
  @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
  public void start() {
    log.info("Begin parse vault tx");
    var executor = Executors.newFixedThreadPool(15);

    vaultRepository.fetchAllByNetwork(ETH_NETWORK).stream()
        .map(i -> CompletableFuture.runAsync(() -> getVaultTransaction(i), executor))
        .collect(Collectors.toList());

  }

  // TODO add more logs, check on null, catch exceptions
  private void getVaultTransaction(VaultEntity vault) {
   try {
     log.info("Run getVaultTransaction for {}", vault);
     var contract = vault.getContract();
     var lastTx = covalenthqVaultTransactionRepository.findAllByNetworkAndContractAddress(contract.getNetwork(), contract.getAddress(),
         PageRequest.of(0, 1, Sort.by("block").ascending()));

     var startingBlock = 0L;

     if (!lastTx.isEmpty()) {
       startingBlock = Math.max(lastTx.get(0).getBlock() - MINUS_BLOCK, 0);
     }

     var currentBlock = web3Functions.fetchCurrentBlock(contract.getNetwork());
     var endingBlock = startingBlock + MAX_BLOCK_RANGE;

     while (currentBlock.longValue() >= endingBlock) {
       // TODO Maybe need to add one more block, because can find the same tx
       startingBlock = endingBlock;
       endingBlock += MAX_BLOCK_RANGE;
       fetchTransactions(contract.getAddress(), contract.getNetwork(), startingBlock, endingBlock);
     }

   } catch (Exception e) {
     log.error("Get error getVaultTransaction", e);
   }
  }

  private void fetchTransactions(String address, String network, long startingBlock, long endingBlock) {
    fetchTransactions(address, network, startingBlock, endingBlock, PAGINATION_SIZE, false);
  }

  private void fetchTransactions(String address, String network, long startingBlock, long endingBlock, int limit, boolean isAfterException) {
    try {
      var page = 0;
      var transaction = covalenthqService.getTransactionByAddress(address, network, page, limit, startingBlock, endingBlock);
      var result = new LinkedList<CovalenthqTransactionHistoryItem>(transaction.getData().getItems());
      var hasMore = transaction.getData().getPagination().isHasMore();

      while (hasMore) {
        page++;
        transaction = covalenthqService.getTransactionByAddress(address, network, page, limit, startingBlock, endingBlock);
        hasMore = transaction.getData().getPagination().isHasMore();
        result.addAll(transaction.getData().getItems());
      }

      var transactions = result.stream()
          .map(CovalenthqTransactionHistoryItem::getLogs)
          .flatMap(Collection::stream)
          .filter(this::isDepositOrWithdraw)
          .map(log -> toCovalenthqVaultTransaction(log, network, address))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      var transactionWithoutDuplicate = new HashSet<>(transactions);
      var transactionInDb = covalenthqVaultTransactionRepository.findAllByTransactionHashIn(
          transactionWithoutDuplicate.stream()
              .map(CovalenthqVaultTransaction::getTransactionHash)
              .collect(Collectors.toList()));
      transactions = transactionWithoutDuplicate.stream()
          .filter(i1 ->
              transactionInDb.stream().noneMatch(i2 ->
                  i1.getNetwork().equals(i2.getNetwork())
                      && i1.getTransactionHash().equals(i2.getTransactionHash())
                      && i1.getContractAddress().equals(i2.getContractAddress())
                      && i1.getOwnerAddress().equals(i2.getOwnerAddress())
              )
          )
          .map(this::fillAdditionalParams)
          .collect(Collectors.toList());

      log.info("Save list covalenthq tx count - {}", transactions.size());
      covalenthqVaultTransactionRepository.saveAll(transactions);

    } catch (CanNotFetchPriceException e) {
      log.error("Can not fetch price {} {}", address, network);
      throw e;
    } catch (IllegalStateException e) {
      log.error("Get a lot of data {} {} on block {} - {}", address, network, startingBlock, endingBlock);
      if (isAfterException) {
        throw e;
      }
      fetchTransactions(address, network, startingBlock, endingBlock, PAGINATION_SIZE_FOR_A_LOT_OF_DATA, true);
    }
  }

  private boolean isDepositOrWithdraw(CovalenthqTransactionHistoryItemLog log) {
    return log.getTopics() != null && !log.getTopics().isEmpty() && LOG_EVENTS.get(log.getTopics().get(0)) != null;
  }

  private CovalenthqVaultTransaction toCovalenthqVaultTransaction(
      CovalenthqTransactionHistoryItemLog item, String network, String contractAddress) {
    try {
      var transaction = new CovalenthqVaultTransaction();
      var covalenthqType = Optional.ofNullable(toCovalenthqVaultTransactionType(item))
          .orElseThrow(() -> {
            log.error("Can not find log event");
            throw new IllegalStateException();
          });
      var value = getParamValue(item, covalenthqType.value);

      transaction.setNetwork(network);
      transaction.setBlock(item.getBlockHeight());
      transaction.setTransactionHash(item.getTransactionHash());
      transaction.setContractDecimal(item.getContractDecimal());
      transaction.setContractAddress(contractAddress);
      transaction.setType(covalenthqType.type);
      transaction.setOwnerAddress(getParamValue(item, covalenthqType.address));
      transaction.setValue(
          StringUtils.isEmpty(value) ? BigDecimal.ZERO : new BigDecimal(value)
      );
      transaction.setSignedAt(transaction.getSignedAt());

      return transaction;
    } catch (Exception e) {
      log.error("Can not parse covalenthq log: {}", item, e);
      return null;
    }
  }

  private CovalenthqVaultTransactionType toCovalenthqVaultTransactionType(CovalenthqTransactionHistoryItemLog log) {
    if (log.getTopics() != null && !log.getTopics().isEmpty()) {
      return LOG_EVENTS.get(log.getTopics().get(0));
    }

    return null;
  }

  private String getParamValue(CovalenthqTransactionHistoryItemLog item, String param) {
    var ethLog = new Log();
    ethLog.setTopics(item.getTopics());
    ethLog.setData(item.getData());

    var result = simpleDecoder.decodeEthLog(ethLog)
        .orElseThrow();

    var indexParam = -1;
    var castToString = false;

    switch (param) {
      case "account":
      case "user":
      case "dst":
      case "provider":
        indexParam = 0;
        castToString = true;
        break;
      case "wad":
      case "value":
        indexParam = 1;
        break;
      case "amount" :
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

  private CovalenthqVaultTransaction fillAdditionalParams(CovalenthqVaultTransaction covalenthqVaultTransaction) {
    var vaultAddress = covalenthqVaultTransaction.getContractAddress();
    var block = covalenthqVaultTransaction.getBlock();
    var network = covalenthqVaultTransaction.getNetwork();
    covalenthqVaultTransaction.setSharePrice(
        sharePriceService.getSharePrice(vaultAddress, block, network)
    );

    covalenthqVaultTransaction.setTokenPrice(
        tokenPriceService.getTokenPrice(vaultAddress, block, network)
    );

    return covalenthqVaultTransaction;
  }
}
