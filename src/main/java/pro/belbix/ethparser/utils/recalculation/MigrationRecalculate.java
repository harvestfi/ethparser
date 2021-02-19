package pro.belbix.ethparser.utils.recalculation;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;

@Service
@Log4j2
public class MigrationRecalculate {

    private final HarvestRepository harvestRepository;
    private final HarvestVaultParserV2 harvestVaultParserV2;
    private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
    private final HarvestDBService harvestDBService;

    public MigrationRecalculate(HarvestRepository harvestRepository,
                                HarvestVaultParserV2 harvestVaultParserV2,
                                HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator,
                                HarvestDBService harvestDBService) {
        this.harvestRepository = harvestRepository;
        this.harvestVaultParserV2 = harvestVaultParserV2;
        this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
        this.harvestDBService = harvestDBService;
    }

    public void start() {
        for (HarvestDTO dto : harvestRepository.fetchAllMigration()) {

            harvestVaultParserV2.parseMigration(dto);
            HarvestDTO migration = dto.getMigration();
            assert migration != null;
            harvestVaultParserV2.enrichDto(migration);
            harvestOwnerBalanceCalculator.fillBalance(migration);
            boolean success = harvestDBService.saveHarvestDTO(migration);
            log.info("Parse migration " + success + " " + migration.print());
        }
    }
}
