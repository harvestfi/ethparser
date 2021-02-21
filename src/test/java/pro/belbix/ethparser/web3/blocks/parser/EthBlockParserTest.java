package pro.belbix.ethparser.web3.blocks.parser;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public void smokeTest() throws JsonProcessingException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
            "0x5643714128cc4a04c3c555355702dfbf92601dae44f443e9767841c346759254",
            true
        ));
        assertNotNull(ethBlockEntity);
        String entityStr = new ObjectMapper().writeValueAsString(ethBlockEntity);
        assertNotNull(entityStr);
//        System.out.println(new ObjectMapper().writeValueAsString(ethBlockEntity));
                EthBlockEntity persistEntity = ethBlockDbService.save(ethBlockEntity);
        if (persistEntity != null) {
            String persisted = new ObjectMapper().writeValueAsString(persistEntity);
            assertNotNull(persisted);
//            System.out.println(persisted);
            ethBlockRepository.delete(persistEntity);
        }
    }
}
