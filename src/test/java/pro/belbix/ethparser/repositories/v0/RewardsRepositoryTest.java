package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RewardsRepositoryTest {

    @Autowired
    private RewardsRepository rewardsRepository;

    @Test
    public void getFirstByVaultOrderByBlockDateDesc() {
        assertNotNull(
            rewardsRepository.getFirstByVaultAndNetworkOrderByBlockDateDesc("USDC", ETH_NETWORK));
    }

    @Test
    public void fetchAllByRange() {
        assertNotNull(rewardsRepository.fetchAllByRange(0, Long.MAX_VALUE, ETH_NETWORK));
    }

    @Test
    public void fetchRewardsByVaultAfterBlockDate() {
        assertNotNull(rewardsRepository
            .fetchRewardsByVaultAfterBlockDate("USDC", 0, Long.MAX_VALUE, ETH_NETWORK));
    }

    @Test
    public void fetchLastRewards() {
        assertNotNull(rewardsRepository.fetchLastRewards(ETH_NETWORK));
    }

    @Test
    public void getAllByVaultOrderByBlockDate() {
        assertNotNull(rewardsRepository
            .getAllByVaultOrderByBlockDate("USDC", 0, Long.MAX_VALUE, ETH_NETWORK));
    }
}
