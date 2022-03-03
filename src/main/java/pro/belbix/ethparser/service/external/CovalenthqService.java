package pro.belbix.ethparser.service.external;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.belbix.ethparser.model.CovalenthqHistoricalPrice;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem.CovalenthqTransactionHistoryItemLog;
import pro.belbix.ethparser.properties.ExternalProperties;
import pro.belbix.ethparser.utils.UrlUtils.CovalenthqUrl;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CovalenthqService {

  private final static int TRANSFER_LIMIT = 3;
  private final static String TRANSFER_LOG_NAME = "Transfer";
  private final static Map<String, String> CHAIN_BY_NETWORK = Map.of(
      MATIC_NETWORK, "137",
      ETH_NETWORK, "1",
      BSC_NETWORK, "56"
  );

  ExternalProperties externalProperties;
  RestTemplate restTemplate;


  public long getCreatedBlockByLastTransaction(String address, String network) {
    try {
      var page = 0;
      var result = getTransactionByAddress(address, network, true, false, page, TRANSFER_LIMIT);

      if (result == null || result.getData() == null || result.getData().getItems() == null) {
        return 0;
      }

      var createdTx = findCovalenthqTransactionItem(result);

      while (createdTx == null) {
        Thread.sleep(100);
        page++;
        result = getTransactionByAddress(address, network, true, true, page, TRANSFER_LIMIT);
        if (result == null || result.getData() == null || result.getData().getItems() == null
            || result.getData().getItems().isEmpty()) {
          return 0;
        }
        createdTx = findCovalenthqTransactionItem(result);
      }

      return createdTx.getBlockHeight();
    } catch (Exception e) {
      log.error("Error during call getCreatedBlockByLastTransaction", e);
      return 0;
    }
  }

  public CovalenthqTransactionHistory getTransactionByAddress(String address, String network,
      boolean isSortAsc, boolean isFullLogs, int page, int limit) {
    var url = String.format(CovalenthqUrl.TRANSACTION_HISTORY,
        externalProperties.getCovalenthq().getUrl(), convertToNetwork(network), address, isSortAsc,
        isFullLogs,
        externalProperties.getCovalenthq().getKey(), page, limit);
    try {
      return restTemplate.getForObject(url, CovalenthqTransactionHistory.class);
    } catch (Exception e) {
      log.error("Error during call {}", url, e);
      throw new IllegalStateException(e);
    }
  }

  // if 507 response code try increase sleep time
  // if 524 response code try to resize page count
  @Retryable(value = Exception.class, maxAttempts = 4, backoff = @Backoff(delay = 1000))
  public CovalenthqTransactionHistory getTransactionByAddress(String address, String network,
      int page, int limit, long startingBlock, long endingBlock) {
    var url = String.format(CovalenthqUrl.TRANSACTION_HISTORY_WITH_BLOCK_RANGE,
        externalProperties.getCovalenthq().getUrl(), convertToNetwork(network), address,
        externalProperties.getCovalenthq().getKey(), page, limit, startingBlock, endingBlock);
    try {
      return restTemplate.getForObject(url, CovalenthqTransactionHistory.class);
    } catch (Exception e) {
      log.error("Error during call {}", url, e);
      throw new IllegalStateException(e);
    }
  }

  public CovalenthqHistoricalPrice getPriceByContractAddress(String contractAddress, String date, String network) {
    var url = String.format(CovalenthqUrl.HISTORICAL_PRICE,
        externalProperties.getCovalenthq().getUrl(), convertToNetwork(network), contractAddress,
        externalProperties.getCovalenthq().getKey(), date, date);

    return restTemplate.getForObject(url, CovalenthqHistoricalPrice.class);
  }

  private String convertToNetwork(String network) {
    return Optional.ofNullable(CHAIN_BY_NETWORK.get(network)).orElse("1");
  }

  private CovalenthqTransactionHistoryItem findCovalenthqTransactionItem(
      CovalenthqTransactionHistory result) {
    return result.getData().getItems().stream()
        .filter(items -> items.getLogs() != null && notHasTransferInLog(items.getLogs()))
        .findFirst()
        .orElse(null);
  }

  private boolean notHasTransferInLog(List<CovalenthqTransactionHistoryItemLog> logs) {
//    for (CovalenthqTransactionHistoryItemLog log : logs) {
//      if (log.getDecoded() != null && TRANSFER_LOG_NAME.equals(log.getDecoded().getName())) {
//        return false;
//      }
//    }
    return true;
  }
}

