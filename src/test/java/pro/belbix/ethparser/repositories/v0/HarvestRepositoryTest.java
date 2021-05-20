package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestAddresses.USDC;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestRepositoryTest {

    private final Pageable limitOne = PageRequest.of(0, 1);

    @Autowired
    private HarvestRepository harvestRepository;

    @Test
    public void findAllByOrderByBlockDate() {
        assertNotNull(harvestRepository.findAllByNetworkOrderByBlockDate(ETH_NETWORK));
    }

    @Test
    public void findAllByBlockDateGreaterThanOrderByBlockDate() {
        assertNotNull(harvestRepository.findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(
            0, ETH_NETWORK
        ));
    }

    @Test
    public void fetchAllWithoutCounts() {
        assertNotNull(harvestRepository.fetchAllWithoutCounts(ETH_NETWORK));
    }

    @Test
    public void fetchLastByVaultAndDate() {
        assertNotNull(
            harvestRepository.fetchLastByVaultAndDate(USDC, ETH_NETWORK, Long.MAX_VALUE));
    }

    @Test
    public void fetchLastByVaultAndDateNotZero() {
        assertNotNull(
            harvestRepository.fetchLastByVaultAndDateNotZero(USDC, ETH_NETWORK, Long.MAX_VALUE));
    }

    @Test
    public void fetchActualOwnerQuantity() {
        assertNotNull(harvestRepository.fetchActualOwnerQuantity(
            USDC,
            ETH_NETWORK,
            Long.MAX_VALUE
        ));
    }

    @Test
    public void fetchAllUsersQuantity() {
        assertNotNull(harvestRepository.fetchAllUsersQuantity(Long.MAX_VALUE, ETH_NETWORK));
    }

    @Test
    public void fetchAllPoolsUsersQuantity() {
        assertNotNull(harvestRepository
            .fetchAllPoolsUsersQuantity(List.of(USDC), Long.MAX_VALUE, ETH_NETWORK));
    }

    @Test
    public void findFirstByOrderByBlockDesc() {
        assertNotNull(harvestRepository.findFirstByNetworkOrderByBlockDesc(ETH_NETWORK));
    }

    @Test
    public void fetchPeriodOfWork() {
        List<Long> period = harvestRepository
            .fetchPeriodOfWork(USDC, Long.MAX_VALUE, ETH_NETWORK, limitOne);
        assertNotNull(period, "query result is not null");
        assertEquals(1, period.size(), "result contains elements");
        assertTrue(period.get(0) > 0, "period not equal zero");
    }

    @Test
    public void fetchAverageTvl() {
        List<Double> avgTvl = harvestRepository
            .fetchAverageTvl(USDC, 0, Long.MAX_VALUE, ETH_NETWORK, limitOne);
        assertNotNull(avgTvl);
        assertEquals(1, avgTvl.size());
        assertEquals(2.87036734E7, avgTvl.get(0).doubleValue());
    }

    @Test
    public void fetchAllByOwner() {
        assertNotNull(harvestRepository.fetchAllByOwner(
            "0x51ab3dbf9a5089a85e6e3e8252df7c911078dd84",
            0, Long.MAX_VALUE, ETH_NETWORK));
    }

    @Test
    public void fetchAllWithoutOwnerBalance() {
        assertNotNull(harvestRepository.fetchAllWithoutOwnerBalance(ETH_NETWORK));
    }

    @Test
    public void fetchAllMigration() {
        assertNotNull(harvestRepository.fetchAllMigration());
    }

    @Test
    public void findFirstByVaultAndBlockDateBeforeOrderByBlockDateDesc() {
        assertNotNull(
            harvestRepository.findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
                USDC, Long.MAX_VALUE, ETH_NETWORK
            ));
    }

    @Test
    public void fetchAllFromBlockDate() {
        assertNotNull(harvestRepository.fetchAllFromBlockDate(0, ETH_NETWORK));
    }

    @Test
    public void findAllByVaultOrderByBlockDate() {
        assertNotNull(harvestRepository
            .findAllByVaultOrderByBlockDate(USDC, 0, Long.MAX_VALUE, ETH_NETWORK));
    }

    @Test
    public void fetchLastTvl() {
        assertNotNull(harvestRepository.fetchLatest(ETH_NETWORK));
    }

    @Test
    public void fetchAllByPeriod() {
        assertNotNull(harvestRepository.fetchAllByPeriod(0, Long.MAX_VALUE, ETH_NETWORK));
    }

    @Test
    public void findAllByMethodNameAndBlockDateGreaterThanOrderByBlockDate() {
        assertNotNull(
            harvestRepository.findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
                "Deposit", 0, ETH_NETWORK
            ));
    }

    @Test
    public void fetchLatestSinceLastWithdraw() {
        assertNotNull(harvestRepository.fetchLatestSinceLastWithdraw(
            "0x51ab3dbf9a5089a85e6e3e8252df7c911078dd84", USDC, Long.MAX_VALUE, ETH_NETWORK
        ));
    }

    @Test
    public void fetchOwnerBalances() {
        assertNotNull(harvestRepository.fetchOwnerBalances(ETH_NETWORK));
    }
}
