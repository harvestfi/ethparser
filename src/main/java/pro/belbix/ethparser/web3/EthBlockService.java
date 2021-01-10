package pro.belbix.ethparser.web3;

import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import pro.belbix.ethparser.entity.BlockCacheEntity;
import pro.belbix.ethparser.repositories.BlockCacheRepository;

@Service
public class EthBlockService {

    private final Web3Service web3;
    private final BlockCacheRepository blockCacheRepository;
    private long lastBlock = 0L;

    public EthBlockService(Web3Service web3, BlockCacheRepository blockCacheRepository) {
        this.web3 = web3;
        this.blockCacheRepository = blockCacheRepository;
    }

    public synchronized long getTimestampSecForBlock(String blockHash, long blockId) {
        BlockCacheEntity cachedBlock = blockCacheRepository.findById(blockId).orElse(null);
        if (cachedBlock != null) {
            return cachedBlock.getBlockDate();
        }
        Block block = web3.findBlock(blockHash);
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

    public long getLastBlock() {
        return lastBlock;
    }

    private static long extractDateFromBlock(Block block) {
        return block.getTimestamp().longValue();
    }


}
