package pro.belbix.ethparser.web3.harvest.db;

import static pro.belbix.ethparser.service.ApyService.calculateAverageApy;
import static pro.belbix.ethparser.utils.CommonUtils.aprToApy;
import static pro.belbix.ethparser.utils.CommonUtils.calculateApr;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.properties.AppProperties;
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
  private final AppProperties appProperties;

  public RewardsDBService(RewardsRepository rewardsRepository,
      HarvestRepository harvestRepository, PriceProvider priceProvider,
      AppProperties appProperties) {
    this.rewardsRepository = rewardsRepository;
    this.harvestRepository = harvestRepository;
    this.priceProvider = priceProvider;
    this.appProperties = appProperties;
  }

  public boolean saveRewardDTO(RewardDTO dto) {
    if (rewardsRepository.existsById(dto.getId())
        && !appProperties.isOverrideDuplicates()) {
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
            .findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
                dto.getVaultAddress(), dto.getBlockDate(), dto.getNetwork());
    if (harvest == null) {
      log.warn("Not found harvest for " + dto);
      dto.setApy(0);
      return;
    }
    Double tvl;
    double reward;
    if (ContractUtils.isPsAddress(dto.getVaultAddress())) {
      tvl = harvest.getLastTvl();
      reward = dto.getReward();
    } else {
      tvl = harvest.getLastUsdTvl();
      double price = priceProvider
          .getPriceForCoin(ContractUtils.getFarmAddress(dto.getNetwork()),
              dto.getBlock(), dto.getNetwork());
      reward = dto.getReward() * price;
    }

    if (tvl == null) {
      log.warn("TVL is null for " + dto.getVault() + " at " + dto.getBlockDate());
      return;
    }
    dto.setTvl(tvl);
    double apr = calculateApr(dto.getPeriodFinish() - dto.getBlockDate(), reward, tvl);
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
        dto.getVaultAddress(), weekAgo, dto.getBlockDate(), dto.getNetwork());
    double averageApy = calculateAverageApy(rewards);
    dto.setWeeklyApy(averageApy);
  }
}
