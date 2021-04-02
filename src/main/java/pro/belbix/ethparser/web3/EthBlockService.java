package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.v0.BlockCacheEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.repositories.v0.BlockCacheRepository;

@Service
public class EthBlockService {

  private final Web3Functions web3;
  private final BlockCacheRepository blockCacheRepository;
  private final EthBlockRepository ethBlockRepository;
  private final AppProperties appProperties;
  private long lastBlock = 0L;

  public EthBlockService(Web3Functions web3, BlockCacheRepository blockCacheRepository,
      EthBlockRepository ethBlockRepository,
      AppProperties appProperties) {
    this.web3 = web3;
    this.blockCacheRepository = blockCacheRepository;
    this.ethBlockRepository = ethBlockRepository;
    this.appProperties = appProperties;
  }

  public synchronized long getTimestampSecForBlock(long blockId) {
    BlockCacheEntity cachedBlock = blockCacheRepository.findById(blockId).orElse(null);
    if (cachedBlock != null) {
      return cachedBlock.getBlockDate();
    }
    web3.setCurrentNetwork(appProperties.getNetwork());
    Block block = web3.findBlockByNumber(blockId, false).getBlock();
    if (block == null) {
      return 0;
    }

    cachedBlock = new BlockCacheEntity();
    cachedBlock.setBlock(blockId);
    cachedBlock.setBlockDate(extractDateFromBlock(block));
    blockCacheRepository.save(cachedBlock);
    if (lastBlock < blockId) {
      lastBlock = blockId;
    }
    return extractDateFromBlock(block);
  }

  private static long extractDateFromBlock(Block block) {
    return block.getTimestamp().longValue();
  }

  public Long getLastBlock() {
    return getLastBlock(ETH_NETWORK);
  }

  public Long getLastBlock(String network) {
    if (BSC_NETWORK.equals(network)) {
      return Optional.ofNullable(ethBlockRepository.findFirstByOrderByNumberDesc())
          .map(EthBlockEntity::getNumber)
          .orElseGet(() -> {
            web3.setCurrentNetwork(BSC_NETWORK);
            return web3.fetchCurrentBlock().longValue();
          });
    } else if (ETH_NETWORK.equals(network)) {
      if (lastBlock == 0) {
        lastBlock = Optional.ofNullable(blockCacheRepository.findFirstByOrderByBlockDateDesc())
            .map(BlockCacheEntity::getBlock)
            .orElseGet(() -> {
              web3.setCurrentNetwork(ETH_NETWORK);
              return web3.fetchCurrentBlock().longValue();
            });
      }
    } else {
      throw new IllegalStateException("Unknown network " + network);
    }
    return lastBlock;
  }


}
