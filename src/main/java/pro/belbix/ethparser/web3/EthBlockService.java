package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
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
  private Map<String, Long> blockCache = new HashMap<>();

  public EthBlockService(Web3Functions web3,
      BlockCacheRepository blockCacheRepository,
      EthBlockRepository ethBlockRepository,
      AppProperties appProperties) {
    this.web3 = web3;
    this.blockCacheRepository = blockCacheRepository;
    this.ethBlockRepository = ethBlockRepository;
    this.appProperties = appProperties;
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

  public Long getLastBlock() {
    return getLastBlock(ETH_NETWORK);
  }

  public Long getLastBlock(String network) {
    Long block = blockCache.get(network);
    if (block != null) {
      return block;
    }
    block = web3.fetchCurrentBlock(network).longValue();
    blockCache.put(network, block);
    return block;
  }


}
