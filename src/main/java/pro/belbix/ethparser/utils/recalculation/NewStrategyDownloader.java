package pro.belbix.ethparser.utils.recalculation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.downloader.HardWorkDownloader;
import pro.belbix.ethparser.web3.harvest.downloader.VaultActionsDownloader;
import pro.belbix.ethparser.web3.harvest.downloader.RewardDownloader;

@Service
@Log4j2
public class NewStrategyDownloader {
  private final VaultActionsDownloader vaultActionsDownloader;
  private final RewardDownloader rewardDownloader;
  private final HardWorkDownloader hardWorkDownloader;
  private final HarvestRepository harvestRepository;
  private final AppProperties appProperties;

  @Value("${new-strategy-download.vaults:}")
  private String[] vaults;

  public NewStrategyDownloader(
      VaultActionsDownloader vaultActionsDownloader,
      RewardDownloader rewardDownloader,
      HardWorkDownloader hardWorkDownloader, HarvestRepository harvestRepository,
      AppProperties appProperties) {
    this.vaultActionsDownloader = vaultActionsDownloader;
    this.rewardDownloader = rewardDownloader;
    this.hardWorkDownloader = hardWorkDownloader;
    this.harvestRepository = harvestRepository;
    this.appProperties = appProperties;
  }

  public void start() {
    int minBlock = Integer.MAX_VALUE;
    for (String vaultName : vaults) {
      log.info("Start download " + vaultName);
      vaultActionsDownloader.setVaultName(vaultName);
      vaultActionsDownloader.start();

      HarvestDTO harvest = harvestRepository
          .findFirstByVaultAndNetworkOrderByBlockDate(vaultName, appProperties.getNetwork());
      if (harvest == null) {
        log.error("Download zero records for " + vaultName);
        continue;
      }
      if (harvest.getBlock().intValue() < minBlock) {
        minBlock = harvest.getBlock().intValue();
      }
      String stContract = ContractUtils.getInstance(appProperties.getNetwork())
          .poolByVaultName(vaultName)
          .map(PoolEntity::getContract)
          .map(ContractEntity::getAddress)
          .orElseThrow(
              () -> new IllegalStateException("Not found pool by vault name " + vaultName));
      rewardDownloader.setContractName(stContract);
      rewardDownloader.start();
    }

    hardWorkDownloader.setFrom(minBlock);
    hardWorkDownloader.start();
  }
}
