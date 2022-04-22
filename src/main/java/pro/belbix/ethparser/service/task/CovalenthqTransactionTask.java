package pro.belbix.ethparser.service.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransaction;
import pro.belbix.ethparser.error.exceptions.CanNotFetchPriceException;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem;
import pro.belbix.ethparser.repositories.covalenthq.CovalenthqVaultTransactionRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.service.SharePriceService;
import pro.belbix.ethparser.service.TokenPriceService;
import pro.belbix.ethparser.service.external.CovalenthqService;
import pro.belbix.ethparser.utils.profit.ParseLogUtils;
import pro.belbix.ethparser.web3.Web3Functions;

@Service
@RequiredArgsConstructor
@Slf4j
public class CovalenthqTransactionTask {
  // max block range in covalenthq
  private static final int MAX_BLOCK_RANGE = 1000000;
  private static final int MINUS_BLOCK = 10;
  private static final int PAGINATION_SIZE = 10000;
  private static final int PAGINATION_SIZE_FOR_A_LOT_OF_DATA = 100;
  @Value("${task.transaction.enable}")
  private Boolean enable;

  @Value("${task.transaction.max-thread-size}")
  private Integer maxThreadSize;

  private final VaultRepository vaultRepository;
  private final CovalenthqService covalenthqService;
  private final CovalenthqVaultTransactionRepository covalenthqVaultTransactionRepository;
  private final Web3Functions web3Functions;
  private final SharePriceService sharePriceService;
  private final TokenPriceService tokenPriceService;
  private final ParseLogUtils parseLogUtils;

  @Scheduled(fixedRateString = "${task.transaction.fixedRate}")
  public void start() {
    if (enable == null || !enable) {
      log.info("Disable CovalenthqTransactionTask");
      return;
    }
    log.info("Begin parse vault tx");
    var executor = Executors.newFixedThreadPool(maxThreadSize);

    vaultRepository.findAll()
        .stream()
        .map(i -> CompletableFuture.runAsync(() -> getVaultTransaction(i), executor))
        .collect(Collectors.toList());
  }

  private void getVaultTransaction(VaultEntity vault) {
   try {
     log.info("Run getVaultTransaction for {}", vault);
     var contract = vault.getContract();
     var lastTx = covalenthqVaultTransactionRepository.findAllByNetworkAndContractAddress(contract.getNetwork(), contract.getAddress(),
         PageRequest.of(0, 1, Sort.by("block").ascending()));
     log.info("Got response: {}", lastTx);
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
      log.info("Covalnethq result: {}", result.size());
      while (hasMore) {
        page++;
        transaction = covalenthqService.getTransactionByAddress(address, network, page, limit, startingBlock, endingBlock);
        hasMore = transaction.getData().getPagination().isHasMore();
        result.addAll(transaction.getData().getItems());
        log.info("Covalnethq result: {}", result.size());
      }

      var transactions = result.stream()
          .map(CovalenthqTransactionHistoryItem::getLogs)
          .flatMap(Collection::stream)
          .filter(i -> parseLogUtils.isCorrectLog(i, address))
          .map(log -> parseLogUtils.toCovalenthqVaultTransactionFromTransfer(log, network, address))
          .flatMap(Collection::stream)
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

  private CovalenthqVaultTransaction fillAdditionalParams(CovalenthqVaultTransaction covalenthqVaultTransaction) {
    var vaultAddress = covalenthqVaultTransaction.getContractAddress();
    var block = covalenthqVaultTransaction.getBlock();
    var network = covalenthqVaultTransaction.getNetwork();
    covalenthqVaultTransaction.setSharePrice(
        sharePriceService.getSharePrice(vaultAddress, block, network)
    );

    covalenthqVaultTransaction.setTokenPrice(
        tokenPriceService.getTokenPrice(vaultAddress, covalenthqVaultTransaction.getValue(), block, network)
    );

    return covalenthqVaultTransaction;
  }
}
