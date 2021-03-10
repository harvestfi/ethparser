package pro.belbix.ethparser.repositories.v0;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.AppConfig;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class BlockCacheRepositoryTest {

    @Autowired
    BlockCacheRepository blockCacheRepository;

    @Test
    public void smokeTest_findFirstByOrderByBlockDateDesc() {
        assertNotNull(blockCacheRepository.findFirstByOrderByBlockDateDesc());
    }
}
