package pro.belbix.ethparser.web3.harvest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParser;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceParsingTest {

    @Autowired
    private HarvestVaultParser harvestVaultParser;

    @Test
    public void shouldParsePrices() {
        HarvestDTO dto = new HarvestDTO();
        dto.setBlock(new BigInteger("11203037"));
        harvestVaultParser.enrichDto(dto);
        assertNotNull(dto.getPrices());
        assertEquals("{\"btc\":15388.4822454613,\"eth\":429.5548615484777}", dto.getPrices());
    }
}
