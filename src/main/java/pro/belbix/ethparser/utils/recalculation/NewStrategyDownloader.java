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

    @Value("${new-strategy-download.contracts:}")
    private String[] contractNames;

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
        for (String poolName : contractNames) {
            log.info("Start download " + poolName);
            harvestVaultDownloader.setContractName(poolName);
            harvestVaultDownloader.start();

            HarvestDTO harvest = harvestRepository.findFirstByVaultOrderByBlockDate(poolName);
            if (harvest == null) {
                log.error("Download zero records for " + poolName);
                continue;
            }
            if (harvest.getBlock().intValue() < minBlock) {
                minBlock = harvest.getBlock().intValue();
            }
            String stContract = ContractUtils.poolByVaultName(poolName)
                .map(PoolEntity::getContract)
                .map(ContractEntity::getAddress)
                .orElseThrow(() -> new IllegalStateException("Not found vault by pool name " + poolName));
            rewardDownloader.setContractName(stContract);
            rewardDownloader.start();
        }

        hardWorkDownloader.setFrom(minBlock);
        hardWorkDownloader.start();
    }
}
