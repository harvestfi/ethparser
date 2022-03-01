package pro.belbix.ethparser.service;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE_PER_FULL_SHARE;

import java.math.BigInteger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.profit.SharePrice;
import pro.belbix.ethparser.repositories.SharePriceRepository;
import pro.belbix.ethparser.utils.ProfitUtils;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SharePriceService {
  SharePriceRepository sharePriceRepository;
  FunctionsUtils functionsUtils;

  @Cacheable("share_price")
  public BigInteger getSharePrice(String vaultAddress, long block, String network) {
    var id = ProfitUtils.toId(vaultAddress, String.valueOf(block), network);
    var sharePrice = sharePriceRepository.findById(id);

    if (sharePrice.isPresent()) {
      return sharePrice.get().getValue();
    }


    var sharePriceInt = functionsUtils.callIntByName(GET_PRICE_PER_FULL_SHARE, vaultAddress, block, network)
        .orElse(BigInteger.ZERO);

    if (sharePriceInt.compareTo(BigInteger.ZERO) == 0) {
      log.info("Share price is zero {}", id);
    }
    sharePriceRepository.save(new SharePrice(id, sharePriceInt));

    return sharePriceInt;
  }

}
