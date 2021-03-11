package pro.belbix.ethparser.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.a_layer.EthTxRepository;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
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
