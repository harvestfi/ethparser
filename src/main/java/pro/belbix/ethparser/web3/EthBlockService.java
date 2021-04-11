package pro.belbix.ethparser.web3;

import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import pro.belbix.ethparser.entity.v0.BlockCacheEntity;
import pro.belbix.ethparser.repositories.v0.BlockCacheRepository;

@Service
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
      return 0;
    }

    cachedBlock = new BlockCacheEntity();
    cachedBlock.setBlock(blockNumber);
    cachedBlock.setBlockDate(extractDateFromBlock(block));
    cachedBlock.setNetwork(network);
    blockCacheRepository.save(cachedBlock);
    return extractDateFromBlock(block);
  }

  private static long extractDateFromBlock(Block block) {
    return block.getTimestamp().longValue();
  }

  public Long getLastBlock(String network) {
    return web3.fetchCurrentBlock(network).longValue();
  }


}
