package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.v0.BlockCacheEntity;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class BlockCacheRepositoryTest {

    @Autowired
    BlockCacheRepository blockCacheRepository;

    @Test
    public void test_findFirstByBlockAndNetwork() {
        // given
        BlockCacheEntity cachedBlock = new BlockCacheEntity();
        cachedBlock.setBlock(1L);
        cachedBlock.setBlockDate(1L);
        cachedBlock.setNetwork("TestNetwork");

        BlockCacheEntity cachedBlock2 = new BlockCacheEntity();
        cachedBlock2.setBlock(2L);
        cachedBlock2.setBlockDate(2L);
        cachedBlock2.setNetwork("TestNetwork");

        BlockCacheEntity cachedBlock3 = new BlockCacheEntity();
        cachedBlock3.setBlock(3L);
        cachedBlock3.setBlockDate(3L);
        cachedBlock3.setNetwork("TestNetwork");

        //when
        blockCacheRepository.save(cachedBlock);
        blockCacheRepository.save(cachedBlock2);
        blockCacheRepository.delete(cachedBlock3);

        BlockCacheEntity extractedCachedBlock = blockCacheRepository
                .findFirstByBlockAndNetwork(1L, "TestNetwork");
        BlockCacheEntity extractedCachedBlock2 = blockCacheRepository
                .findFirstByBlockAndNetwork(2L, "TestNetwork");

        //then
        assertTrue(EqualsBuilder.reflectionEquals(cachedBlock, extractedCachedBlock));
        assertFalse(EqualsBuilder.reflectionEquals(cachedBlock, extractedCachedBlock2));
        assertTrue(EqualsBuilder.reflectionEquals(cachedBlock2, extractedCachedBlock2));
        assertFalse(EqualsBuilder.reflectionEquals(cachedBlock2, extractedCachedBlock));

        assertNotNull(blockCacheRepository.findFirstByBlockAndNetwork(1L, "TestNetwork"));
        assertNull(blockCacheRepository.findFirstByBlockAndNetwork(3L, "TestNetwork"));
        assertNull(blockCacheRepository.findFirstByBlockAndNetwork(4L, "TestNetwork"));
        assertNull(blockCacheRepository.findFirstByBlockAndNetwork(null, "TestNetwork"));
        assertNull(blockCacheRepository.findFirstByBlockAndNetwork(null, null));
        assertNull(blockCacheRepository.findFirstByBlockAndNetwork(1L, null));
    }
}
