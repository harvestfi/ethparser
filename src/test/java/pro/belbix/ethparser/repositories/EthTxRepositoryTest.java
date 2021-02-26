package pro.belbix.ethparser.repositories;

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
import pro.belbix.ethparser.repositories.a_layer.EthTxRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class EthTxRepositoryTest {

    @Autowired
    private EthTxRepository ethTxRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void smokeTest() {

        ethTxRepository.findAll()
            .forEach(tx -> {
                    try {
                        String json = objectMapper.writeValueAsString(tx);
                        assertNotNull(json);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            );

    }
}
