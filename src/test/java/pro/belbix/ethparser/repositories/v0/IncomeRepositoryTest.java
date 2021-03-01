package pro.belbix.ethparser.repositories.v0;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
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
