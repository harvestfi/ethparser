package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
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
        assertNotNull(uniswapRepository.findFirstByOrderByBlockDesc());
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
