package pro.belbix.ethparser.web3.harvest.db;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HarvestDBServiceTest {

    @Autowired
    private HarvestDBService harvestDBService;
    @Autowired
    private HarvestRepository harvestRepository;

    @Test
    @Ignore("DB data required")
    // todo create a normal test
    public void testFillProfit() {
        HarvestDTO dto = harvestRepository
            .findById("0xf4cd511e3b61f98faacf2ddf99301689e903afe62246acb7353170b40cb5472d_224").orElseThrow();
        harvestDBService.fillProfit(dto);
        assertEquals("profit", 41.2912731552351, dto.getProfit(), 0.0);
    }
}
