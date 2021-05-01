package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.PriceDTO;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceRepositoryTest {
    private final Pageable limitOne = PageRequest.of(0, 1);

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void fetchLastPrice() {
        assertNotNull(priceRepository
            .fetchLastPrice("UNI_LP_ETH_USDT", Long.MAX_VALUE, ETH_NETWORK, limitOne));
    }

    @Test
    public void fetchLastPrices() {
        List<PriceDTO> prices = priceRepository.fetchLastPrices(ETH_NETWORK);
        assertNotNull(prices);
        assertTrue(prices.size() >= 1);
    }
}
