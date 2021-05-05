package pro.belbix.ethparser.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.repositories.v0.PriceRepository;

@Service
public class LastDbPricesService {

  private static final int MAX_LIFE_TIME_MIN = 5;

  private final PriceRepository priceRepository;

  private final Map<String, Tuple2<Instant, List<PriceDTO>>> cache = new HashMap<>();

  public LastDbPricesService(PriceRepository priceRepository) {
    this.priceRepository = priceRepository;
  }

  public List<PriceDTO> getLastPrices(String network) {

    Tuple2<Instant, List<PriceDTO>> cachedPrices = cache.get(network);
    if (cachedPrices != null
        && Duration.between(cachedPrices.component1(), Instant.now()).toMinutes()
        < MAX_LIFE_TIME_MIN) {
      return cachedPrices.component2();
    }

    List<PriceDTO> lastPrices = getLastPricesWithoutCache(network);
    cache.put(network, new Tuple2<>(Instant.now(), lastPrices));
    return lastPrices;
  }

  List<PriceDTO> getLastPricesWithoutCache(String network) {
    return priceRepository.fetchLastPrices(network);
  }
}
