package pro.belbix.ethparser.web3;

import static org.junit.Assert.assertEquals;
import static pro.belbix.ethparser.TestUtils.numberFormat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceProviderTest {

  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private ContractLoader contractLoader;

  @Before
  public void setUp() throws Exception {
    contractLoader.load();
    priceProvider.setUpdateBlockDifference(1);
  }

  @Test
  public void getLpPositionAmountInUsdWithNonNullBlockTest() {
    double amountUsd =
            priceProvider.getLpTokenUsdPrice("0xcd7989894bc033581532d2cd88da5db0a4b12859", 0.00000630081174343, 11387098L);
    assertEquals(numberFormat("437,96"), String.format("%.2f", amountUsd));
    }

    @Test
    public void priceForBAS() {
        double price = priceProvider.getPriceForCoin("BAS", 11619379L);
      assertEquals(numberFormat("143,06"), String.format("%.2f", price));
    }
}
