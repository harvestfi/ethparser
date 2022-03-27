package pro.belbix.ethparser.service.external;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory;
import pro.belbix.ethparser.model.CovalenthqTransactionHistory.CovalenthqTransactionHistoryItems.CovalenthqTransactionHistoryItem;
import pro.belbix.ethparser.properties.ExternalProperties;
import pro.belbix.ethparser.utils.UrlUtils.Covalenthq.Params;
import pro.belbix.ethparser.utils.UrlUtils.Covalenthq.Url;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CovalenthqService {

  private final static String DEFAULT_CHAIN = "1";
  private final static int TRANSFER_LIMIT = 3;
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
      log.info("Got tx from covalenthq: {}", result.getData().getItems().size());

      var createdTx = findCovalenthqTransactionItem(result);

      while (createdTx == null) {
        Thread.sleep(100);
        page++;
        result = getTransactionByAddress(address, network, true, true, page, TRANSFER_LIMIT);
        if (result == null || result.getData() == null || result.getData().getItems() == null
            || result.getData().getItems().isEmpty()) {
          return 0;
        }
        log.info("Got tx from covalenthq: {}", result.getData().getItems().size());
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
    var params = String.join(StringUtils.EMPTY,
        Params.QUOTE_CURRENCY,
        Params.FORMAT,
        String.format(Params.BLOCK_SIGNED_AT_ASC, isSortAsc),
        String.format(Params.NO_LOGS, isFullLogs),
        String.format(Params.KEY, externalProperties.getCovalenthq().getKey()),
        String.format(Params.PAGE_NUMBER, page),
        String.format(Params.PAGE_SIZE, limit)
    );

    var url = String.format(Url.TRANSACTION_HISTORY, externalProperties.getCovalenthq().getUrl(), convertToNetwork(network), address, params);

    return getTransactionByAddress(url);
  }

  public CovalenthqTransactionHistory getTransactionByAddress(String address, String network,
      int page, int limit, long startingBlock, long endingBlock) {
    var params = String.join(StringUtils.EMPTY,
        Params.QUOTE_CURRENCY,
        Params.FORMAT,
        String.format(Params.BLOCK_SIGNED_AT_ASC, true),
        String.format(Params.NO_LOGS, false),
        String.format(Params.KEY, externalProperties.getCovalenthq().getKey()),
        String.format(Params.PAGE_NUMBER, page),
        String.format(Params.PAGE_SIZE, limit),
        String.format(Params.START_BLOCK, startingBlock),
        String.format(Params.END_BLOCK, endingBlock)
    );

    var url = String.format(Url.TRANSACTION_HISTORY, externalProperties.getCovalenthq().getUrl(), convertToNetwork(network), address, params);
    return getTransactionByAddress(url);
  }

  // if 507 response code try increase sleep time
  // if 524 response code try to resize page count
  @Retryable(value = Exception.class, maxAttempts = 4, backoff = @Backoff(delay = 1000))
  public CovalenthqTransactionHistory getTransactionByAddress(String url) {
    try {
      return restTemplate.getForObject(url, CovalenthqTransactionHistory.class);
    } catch (Exception e) {
      log.error("Error during call {}", url, e);
      throw new IllegalStateException(e);
    }
  }

  private String convertToNetwork(String network) {
    return Optional.ofNullable(CHAIN_BY_NETWORK.get(network)).orElse(DEFAULT_CHAIN);
  }

  private CovalenthqTransactionHistoryItem findCovalenthqTransactionItem(
      CovalenthqTransactionHistory result) {
    return result.getData().getItems().stream()
        .filter(items -> items.getLogs() != null)
        .findFirst()
        .orElse(null);
  }
}

