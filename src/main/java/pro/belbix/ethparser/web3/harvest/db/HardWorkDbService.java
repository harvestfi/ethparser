package pro.belbix.ethparser.web3.harvest.db;

import static pro.belbix.ethparser.utils.Caller.silentCall;
import static pro.belbix.ethparser.web3.Functions.SECONDS_IN_WEEK;
import static pro.belbix.ethparser.web3.Functions.SECONDS_OF_YEAR;
import static pro.belbix.ethparser.web3.erc20.Tokens.WETH_NAME;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.utils.Caller;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;

@Service
public class HardWorkDbService {

    private static final Logger log = LoggerFactory.getLogger(HardWorkDbService.class);
    private final Pageable limitOne = PageRequest.of(0, 1);
    private final static long PS_DEPLOYED = 1601389313;
    private final static long PS_OLD_DEPLOYED = 1599258042;
    private final static double HARD_WORK_COST = 0.1;

    private final HardWorkRepository hardWorkRepository;
    private final HarvestRepository harvestRepository;
    private final UniswapRepository uniswapRepository;
    private final AppProperties appProperties;
    private final PriceProvider priceProvider;

    public HardWorkDbService(HardWorkRepository hardWorkRepository,
                             HarvestRepository harvestRepository,
                             UniswapRepository uniswapRepository,
                             AppProperties appProperties, PriceProvider priceProvider) {
        this.hardWorkRepository = hardWorkRepository;
        this.harvestRepository = harvestRepository;
        this.uniswapRepository = uniswapRepository;
        this.appProperties = appProperties;
        this.priceProvider = priceProvider;
    }

    public boolean save(HardWorkDTO dto) {
        if (!appProperties.isOverrideDuplicates() && hardWorkRepository.existsById(dto.getId())) {
            log.info("Duplicate HardWork entry " + dto.getId());
            return false;
        }
        hardWorkRepository.save(dto);
        hardWorkRepository.flush();
        saveTotalProfit(dto);
        return true;
    }

    public void saveTotalProfit(HardWorkDTO dto) {
        Double all = hardWorkRepository.getSumForVault(dto.getVault(), dto.getBlockDate());
        if (all == null) {
            all = 0.0;
        }
        dto.setShareUsdTotal(all);

        calculateVaultProfits(dto);
        calculatePsProfits(dto);
        calculateFarmBuyback(dto);
        fillExtraInfo(dto);

        hardWorkRepository.save(dto);
    }

    public void fillExtraInfo(HardWorkDTO dto) {
        int count = hardWorkRepository.countAtBlockDate(dto.getVault(), dto.getBlockDate());
        dto.setCallsQuantity(count + 1);
        int owners = harvestRepository.fetchActualOwnerQuantity(
            dto.getVault(),
            Vaults.vaultNameToOldVaultName.get(dto.getVault()),
            dto.getBlockDate());
        dto.setPoolUsers(owners);

        double ethPrice = priceProvider.getPriceForCoin(WETH_NAME, dto.getBlock());

        dto.setSavedGasFees(((double) owners) * HARD_WORK_COST * ethPrice);

        Double feesSum = hardWorkRepository.sumSavedGasFees(dto.getVault(), dto.getBlockDate());
        if (feesSum == null) {
            feesSum = 0.0;
        }
        dto.setSavedGasFeesSum(feesSum + dto.getSavedGasFees());
    }

    private void calculateVaultProfits(HardWorkDTO dto) {
        silentCall(() -> harvestRepository.fetchLastByVaultAndDateNotZero(dto.getVault(), dto.getBlockDate()))
            .ifPresentOrElse(harvestDTO -> {
                dto.setTvl(harvestDTO.getLastUsdTvl());
                if (dto.getTvl() != 0.0) {
                    dto.setPerc((dto.getShareChangeUsd() / dto.getTvl()) * 100);
                } else {
                    dto.setPerc(0.0);
                }

                silentCall(() -> hardWorkRepository
                    .fetchPercentForPeriod(dto.getVault(), dto.getBlockDate(), limitOne))
                    .filter(Caller::isFilledList)
                    .ifPresentOrElse(sumOfPercL -> {
                        final double sumOfPerc = sumOfPercL.get(0) + dto.getPerc();

                        silentCall(() -> harvestRepository
                            .fetchPeriodOfWork(dto.getVault(), dto.getBlockDate(), limitOne))
                            .filter(periodL -> !periodL.isEmpty() && periodL.get(0) != null)
                            .ifPresentOrElse(periodL -> {
                                double period = (double) periodL.get(0);
                                dto.setPeriodOfWork(periodL.get(0));
                                if (period != 0.0) {
                                    double apr = (SECONDS_OF_YEAR / period) * sumOfPerc;
                                    dto.setApr(apr);
                                }
                            }, () -> log.warn("Not found period for " + dto.print()));
                    }, () -> log.warn("Not found profit for period for " + dto.print()));

                silentCall(() -> hardWorkRepository
                    .fetchProfitForPeriod(
                        dto.getVault(),
                        dto.getBlockDate() - (long) SECONDS_IN_WEEK,
                        dto.getBlockDate(),
                        limitOne))
                    .filter(Caller::isFilledList)
                    .ifPresentOrElse(sumOfProfitL -> {
                        double sumOfProfit = sumOfProfitL.get(0) + dto.getShareChangeUsd();
                        dto.setWeeklyProfit(sumOfProfit);
                    }, () -> log.warn("Not found profit for period for " + dto.print()));

                silentCall(() -> harvestRepository
                    .fetchAverageTvl(
                        dto.getVault(),
                        dto.getBlockDate() - (long) SECONDS_IN_WEEK,
                        dto.getBlockDate(),
                        limitOne))
                    .filter(Caller::isFilledList)
                    .ifPresentOrElse(avgTvlD -> {
                       dto.setWeeklyAverageTvl(avgTvlD.get(0));
                    }, () -> log.warn("Not found average tvl for period for " + dto.print()));

            }, () -> log.warn("Not found harvest for " + dto.print()));

        silentCall(() -> hardWorkRepository
            .fetchAllProfitForPeriod(dto.getBlockDate() - (long) SECONDS_IN_WEEK, dto.getBlockDate(), limitOne))
            .filter(sumOfProfitL -> !sumOfProfitL.isEmpty() && sumOfProfitL.get(0) != null)
            .ifPresentOrElse(sumOfProfitL -> dto.setWeeklyAllProfit(sumOfProfitL.get(0)),
                () -> log.warn("Not found weekly profits for all vaults for " + dto.print()));

    }

    private void calculatePsProfits(HardWorkDTO dto) {
        HarvestDTO harvestDTO = harvestRepository.fetchLastByVaultAndDateNotZero("PS", dto.getBlockDate());
        if (harvestDTO == null) {
            harvestDTO = harvestRepository.fetchLastByVaultAndDateNotZero("PS_V0", dto.getBlockDate());
        }
        if (harvestDTO != null) {
            dto.setPsTvlUsd(harvestDTO.getLastUsdTvl());

            List<Double> allProfitL = hardWorkRepository.fetchAllProfitAtDate(dto.getBlockDate(), limitOne);
            if (allProfitL != null && !allProfitL.isEmpty() && allProfitL.get(0) != null) {
                double allProfit = allProfitL.get(0);
                dto.setAllProfit(allProfit);
                double allPsProfit = (allProfit / 0.7) * 0.3;

                double psProfitPerc = (allPsProfit / dto.getPsTvlUsd()) * 100;

                double period = 0.0;
                if (dto.getBlockDate() < PS_DEPLOYED) {
                    List<Long> periodOldPsL = harvestRepository
                        .fetchPeriodOfWork("PS_V0", dto.getBlockDate(), limitOne);
                    if (periodOldPsL != null && !periodOldPsL.isEmpty() && periodOldPsL.get(0) != null) {
                        period = (double) periodOldPsL.get(0);
                    }
                } else {
                    List<Long> periodNewPsL = harvestRepository.fetchPeriodOfWork("PS", dto.getBlockDate(), limitOne);
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

    private void calculateFarmBuyback(HardWorkDTO dto) {
        if (dto.getShareChangeUsd() <= 0) {
            dto.setFarmBuyback(0.0);
            return;
        }
        Double farmPrice;
        UniswapDTO uniswapDTO = uniswapRepository
            .findFirstByBlockDateBeforeAndCoinOrderByBlockDesc(dto.getBlockDate(), "FARM");
        if (uniswapDTO != null) {
            farmPrice = uniswapDTO.getLastPrice();
        } else {
            log.warn("FARM price not found at block date" + dto.getBlockDate());
            return;
        }

        dto.setFarmBuyback(((dto.getShareChangeUsd() / 0.7) * 0.3) / farmPrice);
        silentCall(() -> hardWorkRepository.fetchAllBuybacksAtDate(dto.getBlockDate(), limitOne))
            .filter(l -> !l.isEmpty() && l.get(0) != null)
            .ifPresentOrElse(l -> dto.setFarmBuybackSum(l.get(0) + dto.getFarmBuyback()),
                () -> dto.setFarmBuybackSum(dto.getFarmBuyback()));
    }
}
