package pro.belbix.ethparser.service;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.profit.TokenPrice;
import pro.belbix.ethparser.error.exceptions.CanNotFetchPriceException;
import pro.belbix.ethparser.repositories.TokenPriceRepository;
import pro.belbix.ethparser.utils.ProfitUtils;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TokenPriceService {
  private final static Double DEFAULT_RETURN_VALUE = 0.0;

  TokenPriceRepository tokenPriceRepository;
  PriceProvider priceProvider;
  FunctionsUtils functionsUtils;


  @Cacheable("token_price")
  public Double getTokenPrice(String vaultAddress, BigDecimal amount, long block, String network) {
    try {

      var id = ProfitUtils.toId(vaultAddress, String.valueOf(block), network);
      var tokenPrice = tokenPriceRepository.findById(id);
      var price = DEFAULT_RETURN_VALUE;

      if (tokenPrice.isPresent()) {
        return tokenPrice.get().getValue();
      }

      if (ContractUtils.isPsAddress(vaultAddress)) {
        return priceProvider.getPriceForCoin(ContractUtils.getFarmAddress(network), block, network);
      }

      var underlyingAddress = functionsUtils.callAddressByName(UNDERLYING, vaultAddress, block, network)
          .orElseThrow(() -> {
            log.error("Can not fetch underlying address for {} {}", vaultAddress, network);
            throw new IllegalStateException();
          });

      if (isToken(underlyingAddress, block, network)) {
        price = priceProvider.getPriceForCoin(underlyingAddress, block, network);
      } else {
        price = priceProvider.getLpTokenUsdPrice(underlyingAddress, amount.doubleValue(), block, network);
      }

      tokenPriceRepository.save(new TokenPrice(id, price));
      return price;
    } catch (Exception e) {
      log.error("Can not get token price - {}", vaultAddress, e);
      throw new CanNotFetchPriceException();
    }
  }

  private boolean isToken(String underlyingAddress, long block, String network) {
    return functionsUtils.callReserves(underlyingAddress, block, network) == null;
  }
}
