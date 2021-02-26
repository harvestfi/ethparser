package pro.belbix.ethparser.utils.recalculation;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.uniswap.UniOwnerBalanceCalculator;

@Service
@Log4j2
public class OwnerBalanceRecalculate {

    private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
    private final UniOwnerBalanceCalculator uniOwnerBalanceCalculator;
    private final HarvestRepository harvestRepository;
    private final UniswapRepository uniswapRepository;

    public OwnerBalanceRecalculate(HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator,
                                   UniOwnerBalanceCalculator uniOwnerBalanceCalculator,
                                   HarvestRepository harvestRepository,
                                   UniswapRepository uniswapRepository) {
        this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
        this.uniOwnerBalanceCalculator = uniOwnerBalanceCalculator;
        this.harvestRepository = harvestRepository;
        this.uniswapRepository = uniswapRepository;
    }

    public void start() {
        harvestRepository.fetchAllWithoutOwnerBalance().forEach(dto -> {
            boolean success = harvestOwnerBalanceCalculator.fillBalance(dto);
            if (success) {
                harvestRepository.save(dto);
                log.info("HARVEST Balance recalculated for  " + dto.print());
            }
        });

        uniswapRepository.fetchAllWithoutOwnerBalance().forEach(dto -> {
            boolean success = uniOwnerBalanceCalculator.fillBalance(dto);
            if (success) {
                uniswapRepository.save(dto);
                log.info("UNI Balance recalculated for  " + dto.print());
            }
        });
    }
}
