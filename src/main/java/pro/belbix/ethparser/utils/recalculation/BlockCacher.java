package pro.belbix.ethparser.utils.recalculation;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.v0.BlockCacheEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.BlockCacheRepository;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;

@Service
@Log4j2
public class BlockCacher {

  private final HarvestRepository harvestRepository;
  private final BlockCacheRepository blockCacheRepository;
  private final AppProperties appProperties;

  public BlockCacher(HarvestRepository harvestRepository,
      BlockCacheRepository blockCacheRepository,
      AppProperties appProperties) {
    this.harvestRepository = harvestRepository;
    this.blockCacheRepository = blockCacheRepository;
    this.appProperties = appProperties;
  }

  public void cacheBlocks() {
    List<HarvestDTO> harvestDTOS = harvestRepository.findAll();
    List<BlockCacheEntity> blockCacheEntities = new ArrayList<>();
    int count = 0;
    int bulkSize = 100;
    for (HarvestDTO dto : harvestDTOS) {
      count++;
      long block = dto.getBlock();
      if (!blockCacheRepository.existsByBlockAndNetwork(block, appProperties.getNetwork())) {
        BlockCacheEntity blockCacheEntity = new BlockCacheEntity();
        blockCacheEntity.setBlock(block);
        blockCacheEntity.setBlockDate(dto.getBlockDate());
        blockCacheEntity.setNetwork(appProperties.getNetwork());
        blockCacheEntities.add(blockCacheEntity);
      }
      if (blockCacheEntities.size() % bulkSize == 0) {
        log.info("Save block caches " + (count * bulkSize));
        blockCacheRepository.saveAll(blockCacheEntities);
        blockCacheEntities.clear();
      }
    }
    blockCacheRepository.saveAll(blockCacheEntities);
  }

}
