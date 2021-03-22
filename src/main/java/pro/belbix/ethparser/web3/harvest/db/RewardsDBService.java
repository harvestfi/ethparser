package pro.belbix.ethparser.web3.harvest.db;

import static pro.belbix.ethparser.service.ApyService.calculateAverageApy;
import static pro.belbix.ethparser.web3.abi.FunctionsUtils.SECONDS_OF_YEAR;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.RewardsRepository;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class RewardsDBService {

  private final RewardsRepository rewardsRepository;
  private final HarvestRepository harvestRepository;
  private final PriceProvider priceProvider;

  public RewardsDBService(RewardsRepository rewardsRepository,
      HarvestRepository harvestRepository, PriceProvider priceProvider) {
    this.rewardsRepository = rewardsRepository;
    this.harvestRepository = harvestRepository;
    this.priceProvider = priceProvider;
  }

  public boolean saveRewardDTO(RewardDTO dto) {
    if (rewardsRepository.existsById(dto.getId())) {
      log.warn("Duplicate reward " + dto);
    }
    fillApy(dto);
    fillWeeklyApy(dto);
    rewardsRepository.save(dto);
    return true;
  }

  public void fillApy(RewardDTO dto) {
    HarvestDTO harvest =
        harvestRepository
            .findFirstByVaultAndBlockDateBeforeOrderByBlockDateDesc(dto.getVault(),
                dto.getBlockDate());
    if (harvest == null) {
      log.warn("Not found harvest for " + dto);
      dto.setApy(0);
      return;
    }
    Double tvl;
    double reward;
    if (ContractUtils.isPsName(dto.getVault())) {
      tvl = harvest.getLastTvl();
      reward = dto.getReward();
    } else {
      tvl = harvest.getLastUsdTvl();
      double price = priceProvider.getPriceForCoin("FARM", dto.getBlock());
      reward = dto.getReward() * price;
    }

    if (tvl == null) {
      log.warn("TVL is null for " + dto.getVault() + " at " + dto.getBlockDate());
      return;
    }
    dto.setTvl(tvl);
    double apr = calculateApr(dto.getPeriodFinish(), dto.getBlockDate(), reward, tvl);
    if (Double.isInfinite(apr) || Double.isNaN(apr)) {
      log.warn("Wrong apr " + dto);
      apr = 0;
    }
    double apy = aprToApy(apr, 365);
    if (Double.isInfinite(apy) || Double.isNaN(apy)) {
      log.warn("Wrong apy " + dto);
      apy = 0;
    }
    dto.setApy(apy);
  }

  public void fillWeeklyApy(RewardDTO dto) {
    Instant blockDate = Instant.ofEpochSecond(dto.getBlockDate());
    long weekAgo = blockDate.minus(7, ChronoUnit.DAYS).getEpochSecond();
    List<RewardDTO> rewards = rewardsRepository.fetchRewardsByVaultAfterBlockDate(
        dto.getVault(), weekAgo, dto.getBlockDate());
    double averageApy = calculateAverageApy(rewards);
    dto.setWeeklyApy(averageApy);
  }

  private static double calculateApr(double periodFinish, double blockDate, double reward,
      double tvl) {
    if (tvl == 0) {
      return 0;
    }
    double period = SECONDS_OF_YEAR / (periodFinish - blockDate);
    return (reward / tvl) * period * 100;
  }

  public static double aprToApy(double apr, double period) {
    if (period == 0) {
      return 0;
    }
    return (Math.pow(1.0 + ((apr / 100) / period), period) - 1.0) * 100;
  }
}
