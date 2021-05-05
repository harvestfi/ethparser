package pro.belbix.ethparser.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.repositories.v0.RewardsRepository;

@Service
public class ApyService {

    private final Map<String, RewardDTO> lastRewards = new HashMap<>();
    private final Map<String, Map<Integer, Double>> apyCache = new HashMap<>();
    private final RewardsRepository rewardsRepository;

    public ApyService(RewardsRepository rewardsRepository) {
        this.rewardsRepository = rewardsRepository;
    }

    public Double averageApyForPool(String vaultAddress, int days, String network) {
        RewardDTO reward = rewardsRepository
            .getFirstByVaultAddressAndNetworkOrderByBlockDateDesc(vaultAddress, network);
        RewardDTO cachedReward = lastRewards.get(vaultAddress);
        if (cachedReward != null && reward.getId().equals(cachedReward.getId())) {
            Double apy = getApyFromCache(vaultAddress, days);
            if (apy != null) {
                return apy;
            }
        }
        lastRewards.put(vaultAddress, reward);
        List<RewardDTO> rewards = rewardsRepository.fetchRewardsByVaultAfterBlockDate(
            vaultAddress,
            Instant.now().minus(days, ChronoUnit.DAYS).getEpochSecond(),
            Long.MAX_VALUE,
            network);
        double averageApy = calculateAverageApy(rewards);
        saveToCache(vaultAddress, days, averageApy);
        return averageApy;
    }

    private Double getApyFromCache(String address, int days) {
        Map<Integer, Double> vaultApys = apyCache.get(address);
        if (vaultApys == null) {
            apyCache.put(address, new HashMap<>());
            return null;
        }
        return vaultApys.get(days);
    }

    public static double calculateAverageApy(List<RewardDTO> rewards) {
        double[] apys = rewards.stream()
            .map(RewardDTO::getApy)
            .mapToDouble(Double::doubleValue).toArray();
        DescriptiveStatistics stats = new DescriptiveStatistics(apys);
        double averageApy = stats.getMean();
        if (Double.isNaN(averageApy) || Double.isInfinite(averageApy)) {
            averageApy = 0;
        }
        return averageApy;
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


}
