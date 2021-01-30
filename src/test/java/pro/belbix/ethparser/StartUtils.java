package pro.belbix.ethparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.recalculation.UtilsStarter;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
public class StartUtils {

    @Autowired
    private UtilsStarter utilsStarter;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private PriceProvider priceProvider;

    @BeforeEach
    void setUp() {
        priceProvider.setUpdateBlockDifference(1);
    }

    @Test
    public void startUtils() {
        if (appProperties.isDevMod()) {
            utilsStarter.startUtils();
        }
    }

}
