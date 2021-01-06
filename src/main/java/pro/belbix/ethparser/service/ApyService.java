package pro.belbix.ethparser.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.repositories.RewardsRepository;

@Service
public class ApyService {

    private RewardDTO lastReward;
    private final Map<String, Map<Integer, Double>> apyCache = new HashMap<>();
    private final RewardsRepository rewardsRepository;

    public ApyService(RewardsRepository rewardsRepository) {
        this.rewardsRepository = rewardsRepository;
    }

    public Double averageApyForPool(String pool, int days) {
        RewardDTO reward = rewardsRepository.getFirstByVaultOrderByBlockDateDesc(pool);
        if (lastReward != null && reward.getId().equals(lastReward.getId())) {
            Double apy = getApyFromCache(pool, days);
            if (apy != null) {
                return apy;
            }
        }
        lastReward = reward;
        List<RewardDTO> rewards = rewardsRepository.fetchRewardsByVaultAfterBlockDate(pool,
            Instant.now().minus(days, ChronoUnit.DAYS).getEpochSecond());
        double averageApy = calculateAverageApy(rewards.stream()
            .map(RewardDTO::getApy)
            .mapToDouble(Double::doubleValue).toArray());
        saveToCache(pool, days, averageApy);
        return averageApy;
    }

    private double calculateAverageApy(double[] apys) {
        DescriptiveStatistics stats = new DescriptiveStatistics(apys);
        return stats.getMean();
    }

    private void saveToCache(String pool, int days, double averageApy) {
        Map<Integer, Double> poolApys = apyCache.get(pool);
        if (poolApys == null) {
            poolApys = new HashMap<>();
            poolApys.put(days, averageApy);
            apyCache.put(pool, poolApys);
            return;
        }
        poolApys.put(days, averageApy);
    }

    private Double getApyFromCache(String pool, int days) {
        Map<Integer, Double> poolApys = apyCache.get(pool);
        if (poolApys == null) {
            apyCache.put(pool, new HashMap<>());
            return null;
        }
        return poolApys.get(days);
    }


}
