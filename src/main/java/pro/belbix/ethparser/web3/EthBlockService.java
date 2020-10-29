package pro.belbix.ethparser.web3;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

@Service
public class EthBlockService {

    private final Web3Service web3;
    private final Map<String, Block> cache = new LinkedHashMap<>();

    public EthBlockService(Web3Service web3) {
        this.web3 = web3;
    }

    public long getTimestampSecForBlock(String blockHash) {
        Block cachedBlock = cache.get(blockHash);
        if (cachedBlock != null) {
            return extractDateFromBlock(cachedBlock);
        }
        Block block = web3.findBlock(blockHash);
        if (block == null) {
            return 0;
        }
        cache.put(blockHash, block);
        if (cache.size() > 10) {
            cache.remove(cache.entrySet().iterator().next().getKey()); //remove first
        }
        return extractDateFromBlock(block);
    }

    private static long extractDateFromBlock(Block block) {
        return block.getTimestamp().longValue();
    }


}
