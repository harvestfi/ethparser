package pro.belbix.ethparser.repositories.v0;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HardWorkRepositoryTest {

    private final Pageable limitOne = PageRequest.of(0, 1);

    @Autowired
    private HardWorkRepository hardWorkRepository;

    @Test
    public void findFirstByOrderByBlockDateDesc() {
        assertNotNull(hardWorkRepository.findFirstByOrderByBlockDateDesc());
    }

    @Test
    public void fetchAllInRange() {
        assertNotNull(hardWorkRepository.fetchAllInRange(0L, Long.MAX_VALUE));
    }

    @Test
    public void getSumForVault() {
        assertNotNull(hardWorkRepository.getSumForVault("USDC", Long.MAX_VALUE));
    }

    @Test
    public void fetchPercentForPeriod() {
        assertNotNull(hardWorkRepository.fetchPercentForPeriod("USDC", Long.MAX_VALUE, limitOne));
    }

    @Test
    public void fetchProfitForPeriod() {
        assertNotNull(hardWorkRepository.fetchProfitForPeriod("USDC", 0, Long.MAX_VALUE, limitOne));
    }

    @Test
    public void fetchAllProfitAtDate() {
        assertNotNull(hardWorkRepository.fetchAllProfitAtDate(Long.MAX_VALUE, limitOne));
    }

    @Test
    public void fetchAllProfitForPeriod() {
        assertNotNull(hardWorkRepository.fetchAllProfitForPeriod(0, Long.MAX_VALUE, limitOne));
    }

    @Test
    public void fetchAllBuybacksAtDate() {
        assertNotNull(hardWorkRepository.fetchAllBuybacksAtDate(Long.MAX_VALUE, limitOne));
    }

    @Test
    public void countAtBlockDate() {
        assertNotNull(hardWorkRepository.countAtBlockDate("USDC", Long.MAX_VALUE));
    }

    @Test
    public void sumSavedGasFees() {
        assertNotNull(hardWorkRepository.sumSavedGasFees("USDC", Long.MAX_VALUE));
    }

    @Test
    public void findAllByVaultOrderByBlockDate() {
        assertNotNull(hardWorkRepository.findAllByVaultOrderByBlockDate("USDC", 0, Long.MAX_VALUE));
    }

    @Test
    public void fetchLatest() {
        assertNotNull(hardWorkRepository.fetchLatest());
    }

    @Test
    public void fetchLastGasSaved() {
        assertNotNull(hardWorkRepository.fetchLastGasSaved());
    }

    @Test
    public void fetchPreviousBlockDateByVaultAndDate() {
        assertNotNull(hardWorkRepository.fetchPreviousBlockDateByVaultAndDate("USDC", Long.MAX_VALUE));
    }
}
