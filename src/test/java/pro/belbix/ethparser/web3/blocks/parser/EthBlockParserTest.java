package pro.belbix.ethparser.web3.blocks.parser;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.blocks.db.EthBlockDbService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class EthBlockParserTest {

    @Autowired
    private Web3Service web3Service;
    @Autowired
    private EthBlockParser ethBlockParser;
    @Autowired
    private EthBlockDbService ethBlockDbService;
    @Autowired
    private EthBlockRepository ethBlockRepository;

    @Test
    public void smokeTest() throws JsonProcessingException, ExecutionException, InterruptedException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
            "0xaa20f7bde5be60603f11a45fc4923aab7552be775403fc00c2e6b805e6297dbe",
            true
        ));
        assertNotNull(ethBlockEntity);
        String entityStr = new ObjectMapper().writeValueAsString(ethBlockEntity);
        assertNotNull(entityStr);
//        System.out.println(new ObjectMapper().writeValueAsString(ethBlockEntity));
        CompletableFuture<EthBlockEntity> result = ethBlockDbService.save(ethBlockEntity);
        if(result != null) {
            EthBlockEntity persistedEntity = result.join();
            if (persistedEntity != null) {
                String persisted = new ObjectMapper().writeValueAsString(persistedEntity);
                assertNotNull(persisted);
                ethBlockRepository.flush();
                System.out.println(persisted);
            }
        }
        System.out.println("load entity");
        EthBlockEntity saved = ethBlockRepository.findById(ethBlockEntity.getNumber()).orElseThrow();
        System.out.println("delete entity");
//        ethBlockRepository.delete(saved);
    }
}
