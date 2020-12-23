package pro.belbix.ethparser.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.OwnerBalanceCalculator;

@Service
@Log4j2
public class OwnerBalanceRecalculate {

    private final OwnerBalanceCalculator ownerBalanceCalculator;
    private final HarvestRepository harvestRepository;

    public OwnerBalanceRecalculate(OwnerBalanceCalculator ownerBalanceCalculator,
                                   HarvestRepository harvestRepository) {
        this.ownerBalanceCalculator = ownerBalanceCalculator;
        this.harvestRepository = harvestRepository;
    }

    public void start() {
        harvestRepository.fetchAllWithoutOwnerBalance().forEach(dto -> {
            boolean success = ownerBalanceCalculator.fillBalance(dto);
            if (success) {
                harvestRepository.save(dto);
                log.info("Balance recalculated for  " + dto.print());
            }
        });
    }
}
