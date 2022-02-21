package pro.belbix.ethparser.service.task;

import java.util.LinkedList;
import java.util.List;
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
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransaction;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem.CovalenthqTransactionHistoryItemLog;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem.CovalenthqTransactionHistoryItemLog.CovalenthqTransactionHistoryItemLogDecode.CovalenthqTransactionHistoryItemLogDecodeParam;
import pro.belbix.ethparser.model.LogEventParam;
import pro.belbix.ethparser.repositories.covalenthq.CovalenthqVaultTransactionRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.service.external.CovalenthqService;
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
  private static final String DEPOSIT_NAME = "Deposit";
  private static final String WITHDRAW_NAME = "Withdraw";

  VaultRepository vaultRepository;
  CovalenthqService covalenthqService;
  CovalenthqVaultTransactionRepository covalenthqVaultTransactionRepository;
  Web3Functions web3Functions;

  // everyday
  @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
  public void start() {
    log.info("Begin parse vault tx");
    var executor = Executors.newFixedThreadPool(10);

    var futures = vaultRepository.findAll().stream()
        .map(i -> CompletableFuture.runAsync(() -> getVaultTransaction(i), executor))
        .collect(Collectors.toList());

  }

  // TODO add more logs, check on null, catch exceptions
  private void getVaultTransaction(VaultEntity vault) {
    log.info("Run getVaultTransaction for {}", vault);
    var contract = vault.getContract();
    var lastTx = covalenthqVaultTransactionRepository.findAllByNetworkAndContractAddress(contract.getNetwork(), contract.getAddress(),
        PageRequest.of(0, 1, Sort.by("blockHeight").ascending()));

    var startingBlock = 0L;

    if (!lastTx.isEmpty()) {
      startingBlock = Math.max(lastTx.get(0).getBlock() - MINUS_BLOCK, 0);
    }

    var currentBlock = web3Functions.fetchCurrentBlock(contract.getNetwork());
    var endingBlock = startingBlock + MAX_BLOCK_RANGE;
    var result = fetchTransactions(contract.getAddress(), contract.getNetwork(), startingBlock, endingBlock);

    while (currentBlock.longValue() >= endingBlock) {
      // TODO Maybe need to add one more block, because can find the same tx
      startingBlock = endingBlock;
      endingBlock += MAX_BLOCK_RANGE;
      result.addAll(
          fetchTransactions(contract.getAddress(), contract.getNetwork(), startingBlock, endingBlock));
    }

    var transactions = result.stream()
        .filter(this::isDepositOrWithdraw)
        .map(tx -> tx.getLogs().stream()
            .map(log -> toCovalenthqVaultTransaction(log, contract.getNetwork(), contract.getAddress()))
            .collect(Collectors.toList())
        )
        .flatMap(List::stream)
        .collect(Collectors.toList());

    covalenthqVaultTransactionRepository.saveAll(transactions);
  }

  private List<CovalenthqTransactionHistoryItem> fetchTransactions(String address, String network, long startingBlock, long endingBlock) {
    var page = 0;
    var transaction = covalenthqService.getTransactionByAddress(address, network, page, PAGINATION_SIZE, startingBlock, endingBlock);
    var result = new LinkedList(transaction.getData().getItems());
    var hasMore = transaction.getData().getPagination().isHasMore();

    while (hasMore) {
      page++;
      transaction = covalenthqService.getTransactionByAddress(address, network, page, PAGINATION_SIZE, startingBlock, endingBlock);
      hasMore = transaction.getData().getPagination().isHasMore();
      result.addAll(transaction.getData().getItems());
    }
    return result;
  }

  private boolean isDepositOrWithdraw(CovalenthqTransactionHistoryItem item) {
    return item.getLogs().stream()
        .anyMatch(i ->
            WITHDRAW_NAME.equalsIgnoreCase(i.getDecoded().getName())
                || DEPOSIT_NAME.equalsIgnoreCase(i.getDecoded().getName()));
  }

  private CovalenthqVaultTransaction toCovalenthqVaultTransaction(
      CovalenthqTransactionHistoryItemLog item, String network, String contractAddress) {
    var transaction = new CovalenthqVaultTransaction();
    var type = toCovalenthqVaultTransactionType(item);

    transaction.setNetwork(network);
    transaction.setBlock(item.getBlockHeight());
    transaction.setTransactionHash(item.getTransactionHash());
    transaction.setContractDecimal(item.getContractDecimal());
    transaction.setContractAddress(contractAddress);
    transaction.setType(type);
    transaction.setOwnerAddress(getParamValue(item, type.equals(CovalenthqVaultTransactionType.DEPOSIT) ? LogEventParam.DEPOSIT_FROM : LogEventParam.WITHDRAW_TO));
    transaction.setValue(Long.parseLong(getParamValue(item, type.equals(CovalenthqVaultTransactionType.DEPOSIT) ? LogEventParam.DEPOSIT_VALUE : LogEventParam.WITHDRAW_VALUE)));
    transaction.setSignedAt(transaction.getSignedAt());

    return transaction;
  }

  private CovalenthqVaultTransactionType toCovalenthqVaultTransactionType(CovalenthqTransactionHistoryItemLog item) {
    return WITHDRAW_NAME.equalsIgnoreCase(item.getDecoded().getName()) ? CovalenthqVaultTransactionType.WITHDRAW : CovalenthqVaultTransactionType.DEPOSIT;
  }

  private String getParamValue(CovalenthqTransactionHistoryItemLog item, LogEventParam param) {
    return item.getDecoded().getParams().stream()
        .filter(i -> param.value.equals(i.getName()))
        .map(CovalenthqTransactionHistoryItemLogDecodeParam::getValue)
        .findFirst()
        .orElse(StringUtils.EMPTY);
  }
}
