package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class BlockCacheRepositoryTest {

    @Autowired
    BlockCacheRepository blockCacheRepository;

    @Test
    public void smokeTest_findFirstByOrderByBlockDateDesc() {
        assertNotNull(blockCacheRepository.findFirstByBlockAndNetwork(10770491L, ETH_NETWORK));
    }
}
