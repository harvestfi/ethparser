package pro.belbix.ethparser.utils.recalculation;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.harvest.parser.VaultActionsParser;

@Service
@Log4j2
public class MigrationRecalculate {

  private final HarvestRepository harvestRepository;
  private final VaultActionsParser vaultActionsParser;
  private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  private final VaultActionsDBService vaultActionsDBService;

  public MigrationRecalculate(HarvestRepository harvestRepository,
      VaultActionsParser vaultActionsParser,
      HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator,
      VaultActionsDBService vaultActionsDBService) {
    this.harvestRepository = harvestRepository;
    this.vaultActionsParser = vaultActionsParser;
    this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
    this.vaultActionsDBService = vaultActionsDBService;
  }

  public void start() {
    for (HarvestDTO dto : harvestRepository.fetchAllMigration()) {

      vaultActionsParser.parseMigration(dto, ETH_NETWORK);
      HarvestDTO migration = dto.getMigration();
      assert migration != null;
      vaultActionsParser.enrichDto(migration, ETH_NETWORK);
      harvestOwnerBalanceCalculator.fillBalance(migration, ETH_NETWORK);
      boolean success = vaultActionsDBService.saveHarvestDTO(migration);
      log.info("Parse migration " + success + " " + migration.print());
    }
  }
}
