package pro.belbix.ethparser.utils;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.entity.BlockCacheEntity;
import pro.belbix.ethparser.repositories.BlockCacheRepository;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.HarvestVaultDownloader;
import pro.belbix.ethparser.web3.harvest.Vaults;
import pro.belbix.ethparser.web3.uniswap.DownloadIncome;
import pro.belbix.ethparser.web3.uniswap.UniswapLpDownloader;

@Service
public class UtilsStarter {

    private static final Logger log = LoggerFactory.getLogger(UtilsStarter.class);
    @Autowired
    private HarvestRepository harvestRepository;
    @Autowired
    private BlockCacheRepository blockCacheRepository;
    @Autowired
    private UniswapLpDownloader uniswapLpDownloader;
    @Autowired
    private HarvestVaultDownloader harvestVaultDownloader;
    @Autowired
    private TvlRecalculate tvlRecalculate;
    @Autowired
    private DownloadIncome downloadIncome;

    public void startUtils() {
//        cacheBlocks();
//        uniswapDownloader();
//        harvestVaultDownloader.start();
        tvlRecalculate.start();
//        downloadIncome.start();
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
