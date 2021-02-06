package pro.belbix.ethparser.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.ContractTypeRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
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
