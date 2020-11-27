package pro.belbix.ethparser.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.entity.BlockCacheEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.BlockCacheRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.HardWorkDownloader;
import pro.belbix.ethparser.web3.harvest.HarvestVaultDownloader;
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

    public UtilsStarter(AppProperties appProperties,
                        HarvestRepository harvestRepository,
                        BlockCacheRepository blockCacheRepository,
                        UniswapLpDownloader uniswapLpDownloader,
                        HarvestVaultDownloader harvestVaultDownloader,
                        TvlRecalculate tvlRecalculate,
                        DownloadIncome downloadIncome,
                        HardWorkDownloader hardWorkDownloader,
                        HardWorkRecalculate hardWorkRecalculate) {
        this.appProperties = appProperties;
        this.harvestRepository = harvestRepository;
        this.blockCacheRepository = blockCacheRepository;
        this.uniswapLpDownloader = uniswapLpDownloader;
        this.harvestVaultDownloader = harvestVaultDownloader;
        this.tvlRecalculate = tvlRecalculate;
        this.downloadIncome = downloadIncome;
        this.hardWorkDownloader = hardWorkDownloader;
        this.hardWorkRecalculate = hardWorkRecalculate;
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
        }
    }

    private void uniswapDownloader() {
        int step = 10000;
        for (int blockNum = 11195486; blockNum < 11216385; blockNum += step) {
            DefaultBlockParameter from = DefaultBlockParameter.valueOf(new BigInteger(blockNum + ""));
            DefaultBlockParameter to = DefaultBlockParameter.valueOf(new BigInteger((blockNum + step) + ""));
            uniswapLpDownloader.load(from, to);
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
