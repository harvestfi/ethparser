package pro.belbix.ethparser.web3.harvest;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.properties.Web3Properties;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;

@Service
public class HardWorkDbService {

    private static final Logger log = LoggerFactory.getLogger(HardWorkDbService.class);
    private final Pageable limitOne = PageRequest.of(0, 1);
    private final static double SECONDS_OF_YEAR = 31557600.0;
    private final static long PS_DEPLOYED = 1601389313;
    private final static long PS_OLD_DEPLOYED = 1599258042;

    private final HardWorkRepository hardWorkRepository;
    private final HarvestRepository harvestRepository;
    private final Web3Properties web3Properties;

    public HardWorkDbService(HardWorkRepository hardWorkRepository,
                             HarvestRepository harvestRepository,
                             Web3Properties web3Properties) {
        this.hardWorkRepository = hardWorkRepository;
        this.harvestRepository = harvestRepository;
        this.web3Properties = web3Properties;
    }

    public boolean save(HardWorkDTO dto) {
        if (!web3Properties.isOverrideDuplicates() && hardWorkRepository.existsById(dto.getId())) {
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

        calculateVaultApr(dto);
        calculatePsApr(dto);

        hardWorkRepository.save(dto);
    }

    private void calculateVaultApr(HardWorkDTO dto) {
        HarvestDTO harvestDTO = harvestRepository.fetchLastByVaultAndDateNotZero(dto.getVault(), dto.getBlockDate());
        if (harvestDTO != null) {
            dto.setTvl(harvestDTO.getLastUsdTvl());
            if (dto.getTvl() != 0.0) {
                dto.setPerc((dto.getShareChangeUsd() / dto.getTvl()) * 100);
            } else {
                dto.setPerc(0.0);
            }

            List<Double> sumOfPercL = hardWorkRepository
                .fetchPercentForPeriod(dto.getVault(), dto.getBlockDate(), limitOne);
            if (sumOfPercL != null && !sumOfPercL.isEmpty() && sumOfPercL.get(0) != null) {
                double sumOfPerc = sumOfPercL.get(0);
                sumOfPerc += dto.getPerc();
                List<Long> periodL = harvestRepository
                    .fetchPeriodOfWork(dto.getVault(), dto.getBlockDate(), limitOne);
                if (periodL != null && !periodL.isEmpty() && periodL.get(0) != null) {
                    double period = (double) periodL.get(0);
                    dto.setPeriodOfWork(periodL.get(0));
                    if (period != 0.0) {
                        double apr = (SECONDS_OF_YEAR / period) * sumOfPerc;
                        dto.setApr(apr);
                    }
                }
            }

        } else {
            log.warn("Not found harvest for " + dto.print());
        }
    }

    private void calculatePsApr(HardWorkDTO dto) {
        HarvestDTO harvestDTO = harvestRepository.fetchLastByVaultAndDateNotZero("PS", dto.getBlockDate());
        if (harvestDTO == null) {
            harvestDTO = harvestRepository.fetchLastByVaultAndDateNotZero("PS_V0", dto.getBlockDate());
        }
        if (harvestDTO != null) {
            dto.setPsTvlUsd(harvestDTO.getLastUsdTvl());

            List<Double> allProfitL = hardWorkRepository.fetchAllProfitForPeriod(dto.getBlockDate(), limitOne);
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
}
