package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static pro.belbix.ethparser.TestUtils.assertModel;

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
    long id = 0;
    BlockCacheEntity cachedBlock = createDTO(id);
    BlockCacheEntity cachedBlock2 = createDTO(id);
    BlockCacheEntity cachedBlock3 = createDTO(id);

    //when
    blockCacheRepository.save(cachedBlock);
    blockCacheRepository.save(cachedBlock2);

    BlockCacheEntity extractedCachedBlock = blockCacheRepository
        .findFirstByBlockAndNetwork(1L, "TestNetwork");
    BlockCacheEntity extractedCachedBlock2 = blockCacheRepository
        .findFirstByBlockAndNetwork(2L, "TestNetwork");

    //then
    assertAll(
        () -> assertModel(cachedBlock, extractedCachedBlock),
        () -> assertModel(cachedBlock2, extractedCachedBlock2),

        () -> assertNotNull(blockCacheRepository.findFirstByBlockAndNetwork(1L, "TestNetwork")),
        () -> assertNull(blockCacheRepository.findFirstByBlockAndNetwork(3L, "TestNetwork")),
        () -> assertNull(blockCacheRepository.findFirstByBlockAndNetwork(4L, "TestNetwork")),
        () -> assertNull(blockCacheRepository.findFirstByBlockAndNetwork(null, "TestNetwork")),
        () -> assertNull(blockCacheRepository.findFirstByBlockAndNetwork(null, null)),
        () -> assertNull(blockCacheRepository.findFirstByBlockAndNetwork(1L, null))
    );

    //after
    blockCacheRepository.delete(cachedBlock);
    blockCacheRepository.delete(cachedBlock2);
    blockCacheRepository.delete(cachedBlock3);
  }

  private BlockCacheEntity createDTO(long id) {
    id++;
    BlockCacheEntity cachedBlock = new BlockCacheEntity();
    cachedBlock.setBlock(id);
    cachedBlock.setBlockDate(id);
    cachedBlock.setNetwork("TestNetwork");
    return cachedBlock;
  }
}
