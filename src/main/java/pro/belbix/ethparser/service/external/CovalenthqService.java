package pro.belbix.ethparser.service.external;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.belbix.ethparser.model.CovalenthqTransaction;
import pro.belbix.ethparser.properties.ExternalProperties;
import pro.belbix.ethparser.utils.UrlUtils.CovalenthqUrl;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CovalenthqService {
  ExternalProperties externalProperties;
  RestTemplate restTemplate;


  public long getCreatedBlockByLastTransaction(String address, String network) {
    var result = getTransactionByAddress(address, network, true, false, 0, 1);

    if (result == null || result.getData() == null || result.getData().getItems() == null || result.getData().getItems().size() != 1) {
      return 0L;
    }

    return result.getData().getItems().get(0).getBlockHeight();
  }

  public CovalenthqTransaction getTransactionByAddress(String address, String network, boolean isSortAsc, boolean isFullLogs, int page, int limit) {
    var url = String.format(CovalenthqUrl.TRANSACTION, externalProperties.getCovalenthq().getUrl(), convertToNetwork(network), address, isSortAsc, isFullLogs,
        externalProperties.getCovalenthq().getKey(), page, limit);
    try {
      return restTemplate.getForObject(url, CovalenthqTransaction.class);
    } catch (Exception e) {
      log.error("Error during call {}", url, e);
      throw new IllegalStateException(e);
    }
  }

  private String convertToNetwork(String network) {
    switch (network) {
      case MATIC_NETWORK:
        return "137";
      case BSC_NETWORK:
        return "56";
      case ETH_NETWORK:
      default:
        return "1";
    }
  }
}
