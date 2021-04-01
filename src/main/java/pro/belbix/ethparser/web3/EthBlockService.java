package pro.belbix.ethparser.web3;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import pro.belbix.ethparser.entity.v0.BlockCacheEntity;
import pro.belbix.ethparser.repositories.v0.BlockCacheRepository;

@Service
public class EthBlockService {

  private final Web3Functions web3;
  private final BlockCacheRepository blockCacheRepository;
  private long lastBlock = 0L;

  public EthBlockService(Web3Functions web3, BlockCacheRepository blockCacheRepository) {
    this.web3 = web3;
    this.blockCacheRepository = blockCacheRepository;
  }

  public synchronized long getTimestampSecForBlock(long blockId) {
    BlockCacheEntity cachedBlock = blockCacheRepository.findById(blockId).orElse(null);
    if (cachedBlock != null) {
      return cachedBlock.getBlockDate();
    }
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

  public long getLastBlock() {
    if (lastBlock == 0) {
      lastBlock = Optional.ofNullable(blockCacheRepository.findFirstByOrderByBlockDateDesc())
          .map(BlockCacheEntity::getBlock)
          .orElseGet(() -> web3.fetchCurrentBlock().longValue());
    }
    return lastBlock;
  }


}
