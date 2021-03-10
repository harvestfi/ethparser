package pro.belbix.ethparser.repositories;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.ContractTypeRepository;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class ContractRepositoryTest {
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ContractTypeRepository contractTypeRepository;

    @Test
    public void test1() {
        contractRepository.findAll();
    }
}
