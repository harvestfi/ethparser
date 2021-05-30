package pro.belbix.ethparser.web3;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import pro.belbix.ethparser.entity.v0.BlockCacheEntity;
import pro.belbix.ethparser.repositories.v0.BlockCacheRepository;

@Service
@Log4j2
public class EthBlockService {

  private final Web3Functions web3;
  private final BlockCacheRepository blockCacheRepository;

  public EthBlockService(Web3Functions web3,
      BlockCacheRepository blockCacheRepository) {
    this.web3 = web3;
    this.blockCacheRepository = blockCacheRepository;
  }

  public synchronized long getTimestampSecForBlock(long blockNumber, String network) {
    BlockCacheEntity cachedBlock =
        blockCacheRepository.findFirstByBlockAndNetwork(blockNumber, network);
    if (cachedBlock != null) {
      return cachedBlock.getBlockDate();
    }
    Block block = web3.findBlockByNumber(blockNumber, false, network).getBlock();
    if (block == null) {
      throw new IllegalStateException("Can't fetch block for " + blockNumber);
    }

    cachedBlock = new BlockCacheEntity();
    cachedBlock.setBlock(blockNumber);
    cachedBlock.setBlockDate(extractDateFromBlock(block));
    cachedBlock.setNetwork(network);
    try {
      blockCacheRepository.save(cachedBlock);
    } catch (Exception e) {
      // we can have multiply trying to save the same block
      log.info("Error save block {}", e.getMessage());
    }

    return extractDateFromBlock(block);
  }

  private static long extractDateFromBlock(Block block) {
    return block.getTimestamp().longValue();
  }

  public Long getLastBlock(String network) {
    return web3.fetchCurrentBlock(network).longValue();
  }


}
