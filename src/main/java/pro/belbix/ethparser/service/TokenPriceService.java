package pro.belbix.ethparser.service;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.profit.TokenPrice;
import pro.belbix.ethparser.error.exceptions.CanNotFetchPriceException;
import pro.belbix.ethparser.repositories.TokenPriceRepository;
import pro.belbix.ethparser.service.external.CovalenthqService;
import pro.belbix.ethparser.utils.ProfitUtils;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionService;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.PriceOracle;
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
  FunctionService functionService;
  CovalenthqService covalenthqService;
  EthBlockService ethBlockService;
  PriceOracle priceOracle;


  @Cacheable("token_price")
  public Double getTokenPrice(String vaultAddress, long block, String network) {
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
          .orElseThrow(IllegalStateException::new);

      if (PriceOracle.isAvailable(block, network)) {
        price = priceOracle.getPriceForCoinOnChain(underlyingAddress, block, network);

        tokenPriceRepository.save(new TokenPrice(id, price));
        return price;
      }

      price = priceProvider.getPriceForCoin(underlyingAddress, block, network);

      tokenPriceRepository.save(new TokenPrice(id, price));
      return price;
    } catch (IllegalStateException e) {
      log.error("Can't fetch underlying token for {}", vaultAddress);
      return DEFAULT_RETURN_VALUE;
    } catch (Exception e) {
      log.error("Can not get token price - {}", vaultAddress);
      throw new CanNotFetchPriceException();
    }
  }

  private String getDateByBlockNumber(long block, String network) {
    long ts = ethBlockService.getTimestampSecForBlock(block, network);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(new Date(ts * 1000));
  }
}
