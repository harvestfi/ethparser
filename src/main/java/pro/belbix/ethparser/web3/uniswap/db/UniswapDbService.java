package pro.belbix.ethparser.web3.uniswap.db;

import java.math.BigInteger;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.entity.IncomeEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.IncomeRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;

@Service
public class UniswapDbService {

    private static final Logger log = LoggerFactory.getLogger(UniswapDbService.class);
    public static final String DO_HARD_WORK = "0xbed04c43e74150794f2ff5b62b4f73820edaf661";
    private final UniswapRepository uniswapRepository;
    private final HarvestRepository harvestRepository;
    private final IncomeRepository incomeRepository;
    private final AppProperties appProperties;
    private final Pageable limitOne = PageRequest.of(0, 1);

    public UniswapDbService(
        UniswapRepository uniswapRepository, HarvestRepository harvestRepository,
        IncomeRepository incomeRepository, AppProperties appProperties) {
        this.uniswapRepository = uniswapRepository;
        this.harvestRepository = harvestRepository;
        this.incomeRepository = incomeRepository;
        this.appProperties = appProperties;
    }

    public boolean saveUniswapDto(UniswapDTO dto) {
        Integer ownerCount = uniswapRepository.fetchOwnerCount();
        if (ownerCount == null) {
            ownerCount = 0;
        }
        dto.setOwnerCount(ownerCount);
        if (!appProperties.isOverrideDuplicates() && uniswapRepository.existsById(dto.getId())) {
            log.warn("Duplicate tx " + dto.getId());
            return false;
        }
        uniswapRepository.save(dto);
        uniswapRepository.flush();
        if (saveIncome(dto)) {
            uniswapRepository.save(dto);
        }
        return true;
    }

    public boolean saveIncome(UniswapDTO dto) {
        if (!DO_HARD_WORK.equals(dto.getOwner())) {
            return false;
        }

        double amountSum = 0.0;
        List<Double> amountSumL = uniswapRepository.fetchAmountSum(dto.getBlockDate(), DO_HARD_WORK, limitOne);
        if (amountSumL != null && !amountSumL.isEmpty()) {
            amountSum = amountSumL.get(0);
        }

        double amountSumUsd = 0.0;
        List<Double> amountSumUsdL = uniswapRepository.fetchAmountSumUsd(dto.getBlockDate(), DO_HARD_WORK, limitOne);
        if (amountSumUsdL != null && !amountSumUsdL.isEmpty()) {
            amountSumUsd = amountSumUsdL.get(0);
        }

        double tvl = 0.0;
        List<Double> tvlL = harvestRepository.fetchTvlFrom(dto.getBlockDate(), "PS", limitOne);
        if (tvlL != null && !tvlL.isEmpty()) {
            tvl = tvlL.get(0);
        } else {
            tvlL = harvestRepository.fetchTvlFrom(dto.getBlockDate(), "PS_V0", limitOne);
            if (tvlL != null && !tvlL.isEmpty()) {
                tvl = tvlL.get(0);
            }
        }

        double tvlUsd = 0.0;
        List<Double> tvlUsdL = harvestRepository.fetchUsdTvlFrom(dto.getBlockDate(), "PS", limitOne);
        if (tvlUsdL != null && !tvlUsdL.isEmpty()) {
            tvlUsd = tvlUsdL.get(0);
        } else {
            tvlUsdL = harvestRepository.fetchUsdTvlFrom(dto.getBlockDate(), "PS_V0", limitOne);
            if (tvlUsdL != null && !tvlUsdL.isEmpty()) {
                tvlUsd = tvlUsdL.get(0);
            }
        }

        double weekPerc = 0.0;
        List<Double> weekPercL = incomeRepository
            .fetchPercentFroPeriod(dto.getBlockDate() - 604800, dto.getBlockDate(), limitOne);
        if (weekPercL != null && !weekPercL.isEmpty() && weekPercL.get(0) != null) {
            weekPerc = weekPercL.get(0);
        }

        IncomeEntity incomeEntity = new IncomeEntity();
        incomeEntity.setId(dto.getId());
        incomeEntity.setTimestamp(dto.getBlockDate());
        incomeEntity.setAmount(dto.getAmount());
        incomeEntity.setAmountUsd(dto.getOtherAmount());
        incomeEntity.setAmountSum(amountSum);
        incomeEntity.setAmountSumUsd(amountSumUsd);
        incomeEntity.setPsTvl(tvl);
        incomeEntity.setPsTvlUsd(tvlUsd);
        incomeEntity.setPerc((dto.getAmount() / tvl) * 100.0);
        incomeEntity.setWeekPerc(weekPerc);

        if (incomeRepository.findById(incomeEntity.getId()).isPresent()) {
            log.warn("Duplicate income " + dto.getId());
        }

        incomeRepository.save(incomeEntity);
        dto.setPsWeekApy(weekPerc);
        dto.setPsIncomeUsd(amountSumUsd);
        return true;
    }

    public BigInteger lastBlock() {
        UniswapDTO dto = uniswapRepository.findFirstByCoinOrderByBlockDesc("FARM");
        if (dto == null) {
            return BigInteger.ONE;
        }
        return dto.getBlock();
    }

}
