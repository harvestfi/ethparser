package pro.belbix.ethparser.web3.harvest.db;

import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.abi.FunctionsUtils.SECONDS_IN_WEEK;
import static pro.belbix.ethparser.web3.abi.FunctionsUtils.SECONDS_OF_YEAR;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PS_V0_ADDRESS;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.HardWorkRepository;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.utils.Caller;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.log.IdleTimeService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class HardWorkDbService {

  private final static long PS_DEPLOYED = 1601389313;
  private final static long PS_OLD_DEPLOYED = 1599258042;
  private final Pageable limitOne = PageRequest.of(0, 1);
  private final HardWorkRepository hardWorkRepository;
  private final HarvestRepository harvestRepository;
  private final AppProperties appProperties;
  private final PriceProvider priceProvider;

  public HardWorkDbService(HardWorkRepository hardWorkRepository,
      HarvestRepository harvestRepository,
      AppProperties appProperties, PriceProvider priceProvider, IdleTimeService idleTimeService) {
    this.hardWorkRepository = hardWorkRepository;
    this.harvestRepository = harvestRepository;
    this.appProperties = appProperties;
    this.priceProvider = priceProvider;
  }

  public boolean save(HardWorkDTO dto) {
    if (!appProperties.isOverrideDuplicates() && hardWorkRepository.existsById(dto.getId())) {
      log.info("Duplicate HardWork entry " + dto.getId());
      return false;
    }
    enrich(dto);
    fillExtraInfo(dto);
    hardWorkRepository.saveAndFlush(dto);
    return true;
  }

  public void enrich(HardWorkDTO dto) {
    Double all = hardWorkRepository
        .getSumForVault(dto.getVaultAddress(), dto.getBlockDate(), dto.getNetwork());
    if (all == null) {
      all = 0.0;
    }
    dto.setFullRewardUsdTotal(all);

    calculateVaultProfits(dto);
    calculatePsProfits(dto);
    calculateFarmBuybackSum(dto);
  }

  // todo fetch all info from chain
  private void calculateVaultProfits(HardWorkDTO dto) {
    silentCall(
        () -> harvestRepository
            .fetchLastByVaultAndDateNotZero(dto.getVaultAddress(), dto.getNetwork(), dto.getBlockDate()))
        .ifPresentOrElse(harvestDTO -> {
          dto.setTvl(harvestDTO.getLastUsdTvl());
          silentCall(() -> hardWorkRepository
              .fetchPercentForPeriod(
                  dto.getVaultAddress(), dto.getBlockDate() - 1, dto.getNetwork(), limitOne))
              .filter(Caller::isNotEmptyList)
              .ifPresentOrElse(sumOfPercL -> {
                silentCall(() -> harvestRepository
                    .fetchPeriodOfWork(dto.getVaultAddress(), dto.getBlockDate(), dto.getNetwork(), limitOne))
                    .filter(periodL -> !periodL.isEmpty() && periodL.get(0) != null)
                    .ifPresentOrElse(periodL -> {
                      dto.setPeriodOfWork(periodL.get(0));
                    }, () -> log.warn("Not found period for " + dto.print()));
              }, () -> log.warn("Not found profit for period for " + dto.print()));

          silentCall(() -> hardWorkRepository
              .fetchProfitForPeriod(
                  dto.getVaultAddress(),
                  dto.getBlockDate() - (long) SECONDS_IN_WEEK,
                  dto.getBlockDate() - 1,
                  dto.getNetwork(),
                  limitOne))
              .filter(Caller::isNotEmptyList)
              .ifPresentOrElse(sumOfProfitL -> {
                double sumOfProfit = sumOfProfitL.get(0) + dto.getFullRewardUsd();
                dto.setWeeklyProfit(sumOfProfit);
              }, () -> log.warn("Not found profit for period for " + dto.print()));

          silentCall(() -> harvestRepository
              .fetchAverageTvl(
                  dto.getVaultAddress(),
                  dto.getBlockDate() - (long) SECONDS_IN_WEEK,
                  dto.getBlockDate(),
                  dto.getNetwork(),
                  limitOne))
              .filter(Caller::isNotEmptyList)
              .ifPresentOrElse(avgTvlD -> {
                dto.setWeeklyAverageTvl(avgTvlD.get(0));
              }, () -> log.warn("Not found average tvl for period for " + dto.print()));

        }, () -> log.warn("Not found harvest for " + dto.print()));

    silentCall(() -> hardWorkRepository
        .fetchAllProfitForPeriod(dto.getBlockDate() - (long) SECONDS_IN_WEEK,
            dto.getBlockDate() - 1, dto.getNetwork(), limitOne))
        .filter(sumOfProfitL -> !sumOfProfitL.isEmpty() && sumOfProfitL.get(0) != null)
        .ifPresentOrElse(
            sumOfProfitL -> dto.setWeeklyAllProfit(sumOfProfitL.get(0) + dto.getFullRewardUsd()),
            () -> log.warn("Not found weekly profits for all vaults for " + dto.print()));

  }

  private void calculatePsProfits(HardWorkDTO dto) {
    HarvestDTO harvestDTO = harvestRepository
        .fetchLastByVaultAndDateNotZero(PS_ADDRESS, dto.getNetwork(), dto.getBlockDate());
    if (harvestDTO == null) {
      harvestDTO = harvestRepository
          .fetchLastByVaultAndDateNotZero(PS_V0_ADDRESS, dto.getNetwork(), dto.getBlockDate());
    }
    if (harvestDTO != null) {
      dto.setPsTvlUsd(harvestDTO.getLastUsdTvl());

      List<Double> allProfitL = hardWorkRepository
          .fetchAllProfitAtDate(dto.getBlockDate(), dto.getNetwork(), limitOne);
      if (allProfitL != null && !allProfitL.isEmpty() && allProfitL.get(0) != null) {
        double allProfit = allProfitL.get(0);
        dto.setAllProfit(allProfit);
        double allPsProfit = allProfit * 0.3;

        double psProfitPerc = (allPsProfit / dto.getPsTvlUsd()) * 100;

        double period = 0.0;
        if (dto.getBlockDate() < PS_DEPLOYED) {
          List<Long> periodOldPsL = harvestRepository
              .fetchPeriodOfWork(PS_V0_ADDRESS, dto.getBlockDate(),dto.getNetwork(), limitOne);
          if (periodOldPsL != null && !periodOldPsL.isEmpty() && periodOldPsL.get(0) != null) {
            period = (double) periodOldPsL.get(0);
          }
        } else {
          List<Long> periodNewPsL = harvestRepository
              .fetchPeriodOfWork(PS_ADDRESS, dto.getBlockDate(),dto.getNetwork(), limitOne);
          if (periodNewPsL != null && !periodNewPsL.isEmpty() && periodNewPsL.get(0) != null) {
            period = (double) periodNewPsL.get(0);
          }
          period += PS_DEPLOYED - PS_OLD_DEPLOYED;
        }

        dto.setPsPeriodOfWork((long) period);

        if (period != 0.0) {
          double apr = (SECONDS_OF_YEAR / period) * psProfitPerc;
          dto.setPsApr(apr);
        }

      }

    } else {
      log.warn("Not found PS for " + dto.print());
    }
  }

  public void calculateFarmBuybackSum(HardWorkDTO dto) {
    silentCall(() -> hardWorkRepository
        .fetchAllBuybacksAtDate(dto.getBlockDate() - 1, dto.getNetwork(), limitOne))
        .filter(Caller::isNotEmptyList)
        .ifPresentOrElse(l -> dto.setFarmBuybackSum(l.get(0) + dto.getFarmBuyback()),
            () -> dto.setFarmBuybackSum(dto.getFarmBuyback()));
  }

  public void fillExtraInfo(HardWorkDTO dto) {
    int count = hardWorkRepository
        .countAtBlockDate(dto.getVaultAddress(), dto.getNetwork(), dto.getBlockDate() - 1);
    dto.setCallsQuantity(count + 1);
    int owners = harvestRepository.fetchActualOwnerQuantity(
        dto.getVaultAddress(),
        dto.getNetwork(),
        dto.getBlockDate());
    dto.setPoolUsers(owners);

    double baseTokenPrice = priceProvider.getPriceForCoin(
        ContractUtils.getBaseNetworkWrappedTokenAddress(dto.getNetwork()),
        dto.getBlock(), dto.getNetwork());

    dto.setSavedGasFees(((double) owners) * dto.getFeeEth() * baseTokenPrice);

    Double feesSum = hardWorkRepository
        .sumSavedGasFees(dto.getVaultAddress(), dto.getNetwork(), dto.getBlockDate());
    if (feesSum == null) {
      feesSum = 0.0;
    }
    dto.setSavedGasFeesSum(feesSum + dto.getSavedGasFees());

    Long lastHardWorkBlockDate = hardWorkRepository
        .fetchPreviousBlockDateByVaultAndDate(dto.getVaultAddress(), dto.getNetwork(), dto.getBlockDate());
    if (lastHardWorkBlockDate != null) {
      dto.setIdleTime(dto.getBlockDate() - lastHardWorkBlockDate);
    }
  }
}
