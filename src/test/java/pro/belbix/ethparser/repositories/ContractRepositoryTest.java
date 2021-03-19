package pro.belbix.ethparser.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.eth.ContractRepository;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class ContractRepositoryTest {
    @Autowired
    private ContractRepository contractRepository;

    @Test
    public void findByIdSmokeTest() {
        assertNotNull(contractRepository.findById(1));
    }
}
