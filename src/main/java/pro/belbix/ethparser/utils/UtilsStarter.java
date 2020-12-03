package pro.belbix.ethparser.utils;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.entity.BlockCacheEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.BlockCacheRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.utils.HardWorkDownloader;
import pro.belbix.ethparser.web3.harvest.utils.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.utils.RewardDownloader;
import pro.belbix.ethparser.web3.uniswap.DownloadIncome;
import pro.belbix.ethparser.web3.uniswap.UniswapLpDownloader;

@Service
public class UtilsStarter {

    private static final Logger log = LoggerFactory.getLogger(UtilsStarter.class);
    private final AppProperties appProperties;
    private final HarvestRepository harvestRepository;
    private final BlockCacheRepository blockCacheRepository;
    private final UniswapLpDownloader uniswapLpDownloader;
    private final HarvestVaultDownloader harvestVaultDownloader;
    private final TvlRecalculate tvlRecalculate;
    private final DownloadIncome downloadIncome;
    private final HardWorkDownloader hardWorkDownloader;
    private final HardWorkRecalculate hardWorkRecalculate;
    private final RewardDownloader rewardDownloader;

    public UtilsStarter(AppProperties appProperties,
                        HarvestRepository harvestRepository,
                        BlockCacheRepository blockCacheRepository,
                        UniswapLpDownloader uniswapLpDownloader,
                        HarvestVaultDownloader harvestVaultDownloader,
                        TvlRecalculate tvlRecalculate,
                        DownloadIncome downloadIncome,
                        HardWorkDownloader hardWorkDownloader,
                        HardWorkRecalculate hardWorkRecalculate,
                        RewardDownloader rewardDownloader) {
        this.appProperties = appProperties;
        this.harvestRepository = harvestRepository;
        this.blockCacheRepository = blockCacheRepository;
        this.uniswapLpDownloader = uniswapLpDownloader;
        this.harvestVaultDownloader = harvestVaultDownloader;
        this.tvlRecalculate = tvlRecalculate;
        this.downloadIncome = downloadIncome;
        this.hardWorkDownloader = hardWorkDownloader;
        this.hardWorkRecalculate = hardWorkRecalculate;
        this.rewardDownloader = rewardDownloader;
    }

    public void startUtils() {
        if ("cache_blocks".equals(appProperties.getStartUtil())) {
            cacheBlocks();
        } else if ("uni_download".equals(appProperties.getStartUtil())) {
            uniswapDownloader();
        } else if ("harvest_download".equals(appProperties.getStartUtil())) {
            harvestVaultDownloader.start();
        } else if ("tvl_recalculate".equals(appProperties.getStartUtil())) {
            tvlRecalculate.start();
        } else if ("income_download".equals(appProperties.getStartUtil())) {
            downloadIncome.start();
        } else if ("hardwork_download".equals(appProperties.getStartUtil())) {
            hardWorkDownloader.start();
        } else if ("hardwork_recalculate".equals(appProperties.getStartUtil())) {
            hardWorkRecalculate.start();
        } else if ("reward_download".equals(appProperties.getStartUtil())) {
            rewardDownloader.start();
        }
    }

    private void uniswapDownloader() {
        int step = 100000;
        for (int blockNum = 10765094; blockNum < 11380000; blockNum += step) {
            uniswapLpDownloader.load(blockNum, blockNum + step);
        }
    }

    private void cacheBlocks() {

        List<HarvestDTO> harvestDTOS = harvestRepository.findAll();
        List<BlockCacheEntity> blockCacheEntities = new ArrayList<>();
        int count = 0;
        int bulkSize = 100;
        for (HarvestDTO dto : harvestDTOS) {
            count++;
            long block = dto.getBlock().longValue();
            if (!blockCacheRepository.existsById(block)) {
                BlockCacheEntity blockCacheEntity = new BlockCacheEntity();
                blockCacheEntity.setBlock(block);
                blockCacheEntity.setBlockDate(dto.getBlockDate());
                blockCacheEntities.add(blockCacheEntity);
            }
            if (blockCacheEntities.size() % bulkSize == 0) {
                log.info("Save block caches " + (count * bulkSize));
                blockCacheRepository.saveAll(blockCacheEntities);
                blockCacheEntities.clear();
            }
        }

    }

}
