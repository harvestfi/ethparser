package pro.belbix.ethparser.repositories.v0;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RewardsRepositoryTest {

    @Autowired
    private RewardsRepository rewardsRepository;

    @Test
    public void getAllByOrderByBlockDate() {
        assertNotNull(rewardsRepository.getAllByOrderByBlockDate());
    }

    @Test
    public void getFirstByVaultOrderByBlockDateDesc() {
        assertNotNull(rewardsRepository.getFirstByVaultOrderByBlockDateDesc("USDC"));
    }

    @Test
    public void fetchAllByRange() {
        assertNotNull(rewardsRepository.fetchAllByRange(0, Long.MAX_VALUE));
    }

    @Test
    public void fetchRewardsByVaultAfterBlockDate() {
        assertNotNull(rewardsRepository.fetchRewardsByVaultAfterBlockDate("USDC", 0, Long.MAX_VALUE));
    }

    @Test
    public void fetchLastRewards() {
        assertNotNull(rewardsRepository.fetchLastRewards());
    }

    @Test
    public void getAllByVaultOrderByBlockDate() {
        assertNotNull(rewardsRepository.getAllByVaultOrderByBlockDate("USDC", 0, Long.MAX_VALUE));
    }
}
