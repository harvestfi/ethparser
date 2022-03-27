package pro.belbix.ethparser.service.task;

import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.DEPOSIT;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.DEPOSIT_UNI;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.WITHDRAW;
import static pro.belbix.ethparser.entity.profit.CovalenthqVaultTransactionType.WITHDRAW_UNI;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CovalenthqTransactionTask {
  // max block range in covalenthq
  private static final int MAX_BLOCK_RANGE = 1000000;
  private static final int MINUS_BLOCK = 10;
  private static final int PAGINATION_SIZE = 10000;
  private static final int PAGINATION_SIZE_FOR_A_LOT_OF_DATA = 100;
  private static final Map<String, CovalenthqVaultTransactionType> LOG_EVENTS = Map.of(
      "0xf279e6a1f5e320cca91135676d9cb6e44ca8a08c0b88342bcdb1144f6511b568", WITHDRAW_UNI,
      "0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364", WITHDRAW,
      "0x90890809c654f11d6e72a28fa60149770a0d11ec6c92319d6ceb2bb0a4ea1a15", DEPOSIT_UNI,
      "0xe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c", DEPOSIT
  );

  @Value("${task.transaction.enable}")
  private Boolean enable;

  @Value("${task.transaction.max-thread-size}")
  private Integer maxThreadSize;

  private final SimpleDecoder simpleDecoder;
  private final VaultRepository vaultRepository;
  private final CovalenthqService covalenthqService;
  private final CovalenthqVaultTransactionRepository covalenthqVaultTransactionRepository;
  private final Web3Functions web3Functions;
  private final SharePriceService sharePriceService;
  private final TokenPriceService tokenPriceService;
  private final FunctionsUtils functionsUtils;


  @Scheduled(fixedRateString = "${task.transaction.fixedRate}")
  public void start() {
    if (enable == null || !enable) {
      log.info("Disable CovalenthqTransactionTask");
      return;
    }
    log.info("Begin parse vault tx");
    var executor = Executors.newFixedThreadPool(maxThreadSize);

    vaultRepository.findAllInContractAddress(List.of(
            "0x1c4adff419f6b91e51d0ade953c9bbf5d16a583f",
                "0xcd8fb1302c30fde56bce5b34211e84561bbf0df1",
                "0xe3f309f151746b3c0953e4c0e455bff3dc2176aa",
                "0x0a7d74604b39229d444855ef294f287099774ac8",
                "0x129ccee12a9542ff77e066e6f8d7df49f8cbf89d",
                "0x75071f2653fbc902ebaff908d4c68712a5d1c960",
                "0x63671425ef4d25ec2b12c7d05de855c143f16e3b",
                "0x14cb410659b4a4a7ccea99e6f6c9eac8718160df",
                "0x5da237ad194b8bbb008ac8916df99a92a8a7c8eb",
                "0xe64bfe13aa99335487f1f42a56cddbffaec83bbf",
                "0xc97ddaa8091abaf79a4910b094830cce5cdd78f4",
                "0x6d386490e2367fc31b4acc99ab7c7d4d998a3121",
                "0xe1f9a3ee001a2ecc906e8de637dbf20bb2d44633",
                "0x84646f736795a8bc22ab34e05c8982cd058328c7",
                "0xe604fd5b1317babd0cf2c72f7f5f2ad8c00adbe1",
                "0xffbd102fafbd9e15c9122d9c62ab299afd4d3e4f",
                "0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34",
                "0xf553e1f826f42716cdfe02bde5ee76b2a52fc7eb",
                "0x6a0d7383762962be039c197462bf1df377410853",
                "0x299b00d031ba65ca3a22a8f7e8059dab0b072247",
                "0x299b00d031ba65ca3a22a8f7e8059dab0b072247"
                ),
            BSC_NETWORK).stream()
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
      var decimal = item.getContractDecimal() == 0
          ? functionsUtils.getDecimal(contractAddress, network)
          : item.getContractDecimal();

      transaction.setNetwork(network);
      transaction.setBlock(item.getBlockHeight());
      transaction.setTransactionHash(item.getTransactionHash());
      transaction.setContractDecimal(decimal);
      transaction.setContractAddress(contractAddress);
      transaction.setType(covalenthqType.type);
      transaction.setOwnerAddress(getParamValue(item, covalenthqType.address));
      transaction.setValue(
          StringUtils.isEmpty(value) ? BigDecimal.ZERO : new BigDecimal(value)
      );
      transaction.setSignedAt(item.getSignedAt());

      return transaction;
    } catch (Exception e) {
      log.error("Can not parse covalenthq log: {}", item, e);
      throw new CanNotFetchPriceException();
//      return null;
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
        tokenPriceService.getTokenPrice(vaultAddress, covalenthqVaultTransaction.getValue(), block, network)
    );

    return covalenthqVaultTransaction;
  }
}
