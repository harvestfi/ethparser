package pro.belbix.ethparser.web3;

import static org.junit.Assert.assertEquals;
import static pro.belbix.ethparser.TestUtils.numberFormat;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceProviderTest {

  @Autowired
  private PriceProvider priceProvider;

  @Test
  public void getLpPositionAmountInUsdWithNonNullBlockTest() {
    double amountUsd =
            priceProvider.getLpTokenUsdPrice(
                "0xc3d03e4f041fd4cd388c549ee2a29a9e5075882f",
                1, 12100000L, ETH_NETWORK);
    assertEquals(numberFormat("100.89"), String.format("%.2f", amountUsd));
    }

    @Test
    public void priceForBAS() {
        double price = priceProvider.getPriceForCoin("BAS", 12100000L, ETH_NETWORK);
      assertEquals(numberFormat("0.79"), String.format("%.2f", price));
    }

  @Test
  public void priceForFARM() {
    double price = priceProvider.getPriceForCoin("FARM", 12113876, ETH_NETWORK);
    assertEquals(numberFormat("243.24"), String.format("%.2f", price));
  }

  @Test
  public void priceForBNB() {
    double price = priceProvider.getPriceForCoin("WBNB", 6905123, BSC_NETWORK);
    assertEquals(numberFormat("536.67"), String.format("%.2f", price));
  }

  @Test
  public void priceForBalancer_80_BAL_20_WETH() {
    // B-80BAL-20WETH
    double price = priceProvider.getBalancerPrice("0x5c6ee304399dbdb9c8ef030ab642b10820db8f56", 14294907L, ETH_NETWORK);
    assertEquals(numberFormat("28.25"), String.format("%.2f", price));
  }

  @Test
  public void priceForCurve_Curvefi_USD_BTC_ETH() {
    //Curve.fi USD-BTC-ETH
    double price = priceProvider.getCurvePrice("0xc4AD29ba4B3c580e6D59105FFf484999997675Ff", 14354885L, ETH_NETWORK);
    assertEquals(numberFormat("1459.31"), String.format("%.2f", price));
  }
}
