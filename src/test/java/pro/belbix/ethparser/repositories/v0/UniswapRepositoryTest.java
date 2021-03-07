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
public class UniswapRepositoryTest {
    private final Pageable limitOne = PageRequest.of(0, 1);

    @Autowired
    private UniswapRepository uniswapRepository;

    @Test
    public void fetchOwnerCount() {
        assertNotNull(uniswapRepository.fetchOwnerCount(Long.MAX_VALUE));
    }

    @Test
    public void findFirstByCoinOrderByBlockDesc() {
        assertNotNull(uniswapRepository.findFirstByCoinOrderByBlockDesc("FARM"));
    }

    @Test
    public void findFirstByBlockDateBeforeAndCoinOrderByBlockDesc() {
        assertNotNull(uniswapRepository.findFirstByBlockDateBeforeAndCoinOrderByBlockDesc(Long.MAX_VALUE, "FARM"));
    }

    @Test
    public void fetchAmountSum() {
        assertNotNull(uniswapRepository.fetchAmountSum(
            0, "0x0d089508d5fcdc92363fe84c84a44738863d9201", limitOne));
    }

    @Test
    public void fetchAmountSumUsd() {
        assertNotNull(uniswapRepository.fetchAmountSumUsd(
            0, "0x0d089508d5fcdc92363fe84c84a44738863d9201", limitOne));
    }

    @Test
    public void findAllByOwnerAndCoinOrderByBlockDate() {
        assertNotNull(uniswapRepository.findAllByOwnerAndCoinOrderByBlockDate(
            "0x0d089508d5fcdc92363fe84c84a44738863d9201", "FARM"));
    }

    @Test
    public void fetchAllByOwner() {
        assertNotNull(uniswapRepository.fetchAllByOwner(
            "0x0d089508d5fcdc92363fe84c84a44738863d9201", 0, Long.MAX_VALUE
        ));
    }

    @Test
    public void findAllByOrderByBlockDate() {
        assertNotNull(uniswapRepository.findAllByOrderByBlockDate());
    }

    @Test
    public void findAllByBlockDateGreaterThanOrderByBlockDate() {
        assertNotNull(uniswapRepository.findAllByBlockDateGreaterThanOrderByBlockDate(0));
    }

    @Test
    public void fetchAllWithoutOwnerBalance() {
        assertNotNull(uniswapRepository.fetchAllWithoutOwnerBalance());
    }

    @Test
    public void fetchAllFromBlockDate() {
        assertNotNull(uniswapRepository.fetchAllFromBlockDate(0));
    }

    @Test
    public void fetchAllByPeriod() {
        assertNotNull(uniswapRepository.fetchAllByPeriod(0, Long.MAX_VALUE));
    }

    @Test
    public void fetchOHLCTransactions() {
        assertNotNull(uniswapRepository.fetchOHLCTransactions(
            "FARM", 0, Long.MAX_VALUE, 36000));
    }
}
