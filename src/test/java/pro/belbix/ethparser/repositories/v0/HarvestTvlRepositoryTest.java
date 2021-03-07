package pro.belbix.ethparser.repositories.v0;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HarvestTvlRepositoryTest {

    @Autowired
    private HarvestTvlRepository harvestTvlRepository;

    @Test
    public void findAllByOrderByCalculateTime() {
        assertNotNull(harvestTvlRepository.findAllByOrderByCalculateTime());
    }

    @Test
    public void getHistoryOfAllTvl() {
        assertNotNull(harvestTvlRepository.getHistoryOfAllTvl(0, Long.MAX_VALUE));
    }
}
