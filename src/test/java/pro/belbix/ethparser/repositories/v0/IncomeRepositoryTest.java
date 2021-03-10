package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class IncomeRepositoryTest {

    private final Pageable limitOne = PageRequest.of(0, 1);
    @Autowired
    private IncomeRepository incomeRepository;

    @Test
    public void fetchPercentFroPeriod() {
        assertNotNull(incomeRepository.fetchPercentFroPeriod(0, Long.MAX_VALUE, limitOne));
    }

    @Test
    public void findAllByOrderByTimestamp() {
        assertNotNull(incomeRepository.findAllByOrderByTimestamp());
    }

    @Test
    public void findFirstByOrderByTimestampDesc() {
        assertNotNull(incomeRepository.findFirstByOrderByTimestampDesc());
    }
}
