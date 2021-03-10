package pro.belbix.ethparser.repositories.v0;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestRepositoryTest {

    private final Pageable limitOne = PageRequest.of(0, 1);

    @Autowired
    private HarvestRepository harvestRepository;

    @Test
    public void findAllByOrderByBlockDate() {
        assertNotNull(harvestRepository.findAllByOrderByBlockDate());
    }

    @Test
    public void findAllByBlockDateGreaterThanOrderByBlockDate() {
        assertNotNull(harvestRepository.findAllByBlockDateGreaterThanOrderByBlockDate(
            0
        ));
    }

    @Test
    public void fetchAllWithoutCounts() {
        assertNotNull(harvestRepository.fetchAllWithoutCounts());
    }

    @Test
    public void fetchTVL() {
        assertNotNull(harvestRepository.fetchTVL("USDC", Long.MAX_VALUE));
    }

    @Test
    public void fetchLastByVaultAndDate() {
        assertNotNull(harvestRepository.fetchLastByVaultAndDate("USDC", Long.MAX_VALUE));
    }

    @Test
    public void fetchLastByVaultAndDateNotZero() {
        assertNotNull(harvestRepository.fetchLastByVaultAndDateNotZero("USDC", Long.MAX_VALUE));
    }

    @Test
    public void fetchOwnerCount() {
        assertNotNull(harvestRepository.fetchOwnerCount("USDC", Long.MAX_VALUE));
    }

    @Test
    public void fetchActualOwnerQuantity() {
        assertNotNull(harvestRepository.fetchActualOwnerQuantity(
            "USDC",
            "asdada",
            Long.MAX_VALUE
        ));
    }

    @Test
    public void fetchAllUsersQuantity() {
        assertNotNull(harvestRepository.fetchAllUsersQuantity(Long.MAX_VALUE));
    }

    @Test
    public void fetchAllPoolsUsersQuantity() {
        assertNotNull(harvestRepository.fetchAllPoolsUsersQuantity(List.of("USDC"), Long.MAX_VALUE));
    }

    @Test
    public void findFirstByOrderByBlockDesc() {
        assertNotNull(harvestRepository.findFirstByOrderByBlockDesc());
    }

    @Test
    public void findFirstByVaultAndBlockDateLessThanEqualAndIdNotOrderByBlockDateDesc() {
        assertNotNull(harvestRepository.findFirstByVaultAndBlockDateLessThanEqualAndIdNotOrderByBlockDateDesc(
            "USDC", Long.MAX_VALUE, ""
        ));
    }

    @Test
    public void fetchTvlFrom() {
        assertNotNull(harvestRepository.fetchTvlFrom(0, "USDC", limitOne));
    }

    @Test
    public void fetchUsdTvlFrom() {
        assertNotNull(harvestRepository.fetchUsdTvlFrom(0, "USDC", limitOne));
    }

    @Test
    public void fetchPeriodOfWork() {
        assertNotNull(harvestRepository.fetchPeriodOfWork("USDC", Long.MAX_VALUE, limitOne));
    }

    @Test
    public void fetchAverageTvl() {
        assertNotNull(harvestRepository.fetchAverageTvl("USDC", 0, Long.MAX_VALUE, limitOne));
    }

    @Test
    public void fetchAllByOwner() {
        assertNotNull(harvestRepository.fetchAllByOwner(
            "0x51ab3dbf9a5089a85e6e3e8252df7c911078dd84",
            0, Long.MAX_VALUE));
    }

    @Test
    public void fetchAllWithoutOwnerBalance() {
        assertNotNull(harvestRepository.fetchAllWithoutOwnerBalance());
    }

    @Test
    public void fetchAllMigration() {
        assertNotNull(harvestRepository.fetchAllMigration());
    }

    @Test
    public void findFirstByVaultAndBlockDateBeforeOrderByBlockDateDesc() {
        assertNotNull(harvestRepository.findFirstByVaultAndBlockDateBeforeOrderByBlockDateDesc(
            "USDC", Long.MAX_VALUE
        ));
    }

    @Test
    public void findFirstByVaultOrderByBlockDate() {
        assertNotNull(harvestRepository.findFirstByVaultOrderByBlockDate("USDC"));
    }

    @Test
    public void fetchAllFromBlockDate() {
        assertNotNull(harvestRepository.fetchAllFromBlockDate(0));
    }

    @Test
    public void findAllByVaultOrderByBlockDate() {
        assertNotNull(harvestRepository.findAllByVaultOrderByBlockDate("USDC", 0, Long.MAX_VALUE));
    }

    @Test
    public void fetchLastTvl() {
        assertNotNull(harvestRepository.fetchLastTvl());
    }

    @Test
    public void fetchAllByPeriod() {
        assertNotNull(harvestRepository.fetchAllByPeriod(0, Long.MAX_VALUE));
    }

    @Test
    public void findAllByMethodNameAndBlockDateGreaterThanOrderByBlockDate() {
        assertNotNull(harvestRepository.findAllByMethodNameAndBlockDateGreaterThanOrderByBlockDate(
            "Deposit", 0
        ));
    }

    @Test
    public void fetchLatestSinceLastWithdraw() {
        assertNotNull(harvestRepository.fetchLatestSinceLastWithdraw(
            "0x51ab3dbf9a5089a85e6e3e8252df7c911078dd84", "USDC", Long.MAX_VALUE
        ));
    }

    @Test
    public void fetchOwnerBalances() {
        assertNotNull(harvestRepository.fetchOwnerBalances());
    }
}
