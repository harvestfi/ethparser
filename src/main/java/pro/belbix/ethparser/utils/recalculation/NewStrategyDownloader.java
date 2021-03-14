package pro.belbix.ethparser.utils.recalculation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.downloader.HardWorkDownloader;
import pro.belbix.ethparser.web3.harvest.downloader.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.downloader.RewardDownloader;

@Service
@Log4j2
public class NewStrategyDownloader {

  private final HarvestVaultDownloader harvestVaultDownloader;
  private final RewardDownloader rewardDownloader;
  private final HardWorkDownloader hardWorkDownloader;
  private final HarvestRepository harvestRepository;

  @Value("${new-strategy-download.vaults:}")
  private String[] vaults;

  public NewStrategyDownloader(
      HarvestVaultDownloader harvestVaultDownloader,
      RewardDownloader rewardDownloader,
      HardWorkDownloader hardWorkDownloader, HarvestRepository harvestRepository) {
    this.harvestVaultDownloader = harvestVaultDownloader;
    this.rewardDownloader = rewardDownloader;
    this.hardWorkDownloader = hardWorkDownloader;
    this.harvestRepository = harvestRepository;
  }

  public void start() {
    int minBlock = Integer.MAX_VALUE;
    for (String vaultName : vaults) {
      log.info("Start download " + vaultName);
      harvestVaultDownloader.setVaultName(vaultName);
      harvestVaultDownloader.start();

      HarvestDTO harvest = harvestRepository.findFirstByVaultOrderByBlockDate(vaultName);
      if (harvest == null) {
        log.error("Download zero records for " + vaultName);
        continue;
      }
      if (harvest.getBlock().intValue() < minBlock) {
        minBlock = harvest.getBlock().intValue();
      }
      String stContract = ContractUtils.poolByVaultName(vaultName)
          .map(PoolEntity::getContract)
          .map(ContractEntity::getAddress)
          .orElseThrow(() -> new IllegalStateException("Not found pool by vault name " + vaultName));
      rewardDownloader.setContractName(stContract);
      rewardDownloader.start();
    }

    hardWorkDownloader.setFrom(minBlock);
    hardWorkDownloader.start();
  }
}
