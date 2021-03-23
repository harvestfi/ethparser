package pro.belbix.ethparser.web3.prices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import static pro.belbix.ethparser.TestUtils.numberFormat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceOracleTest {

    @Autowired
    private PriceProvider priceProvider;
    @Autowired
    private ContractLoader contractLoader;

    @Before
    public void load() {
        contractLoader.load();
    }

    @Test
    public void getTokenPrice() {

        double priceBAS = priceProvider.getPriceForCoin("BAS", 12015725);
        double priceWBTC = priceProvider.getPriceForCoin("WBTC", 12015725);
        double priceWETH = priceProvider.getPriceForCoin("WETH", 12015725);
        double priceUSDC = priceProvider.getPriceForCoin("USDC", 12015725);
        double priceUSDT = priceProvider.getPriceForCoin("USDT", 12015725);
        double priceSUSHI_LP_ETH_WBTC = priceProvider.getLpTokenUsdPrice("0xceff51756c56ceffca006cd410b03ffc46dd3a58", 1, 12015725);
        double priceCRV_EURS = priceProvider.getPriceForCoin("CRV_EURS", 12015725);
        double priceMGOOGL = priceProvider.getPriceForCoin("MGOOGL", 12015725);
        double priceFARM = priceProvider.getPriceForCoin("FARM", 12015725);
        double priceRENBTC = priceProvider.getPriceForCoin("RENBTC", 12015725);
        double price3CRV = priceProvider.getPriceForCoin("3CRV", 12015725);
        
        assertAll(
            () -> assertEquals("Price", numberFormat("0,46139880"), String.format("%.8f", priceBAS)),
            () -> assertEquals("Price", numberFormat("55435,19606653"), String.format("%.8f", priceWBTC)),
            () -> assertEquals("Price", numberFormat("1786,09068119"), String.format("%.8f", priceWETH)),
            () -> assertEquals("Price", numberFormat("1,00000000"), String.format("%.8f", priceUSDC)),
            () -> assertEquals("Price", numberFormat("0,99958400"), String.format("%.8f", priceUSDT)),
            () -> assertEquals("Price", numberFormat("47351069766,33282500"), String.format("%.8f", priceSUSHI_LP_ETH_WBTC)),
            () -> assertEquals("Price", numberFormat("1,19183883"), String.format("%.8f", priceCRV_EURS)),
            () -> assertEquals("Price", numberFormat("2079,61790428"), String.format("%.8f", priceMGOOGL)),
            () -> assertEquals("Price", numberFormat("229,46677216"), String.format("%.8f", priceFARM)),
            () -> assertEquals("Price", numberFormat("55435,19606653"), String.format("%.8f", priceRENBTC)),
            () -> assertEquals("Price", numberFormat("1,01426724"), String.format("%.8f", price3CRV))
        );
    }

    @Test
    public void testDbAndCacheResponse () {
        checkTokenPrice("WBTC", 12015725, 55435.19606653);
        checkTokenPrice("WBTC", 12015725, 55435.19606653);
        priceProvider.setUpdateBlockDifference(1);
        checkTokenPrice("WBTC", 12015725, 55435.19606653);
    }
    
    private void checkTokenPrice(String name, long block, double price) {
        double response = priceProvider.getPriceForCoin(name, block);

        assertAll(
            () -> assertEquals("Price", String.format("%.8f", price), String.format("%.8f", response))
        );
    }
    
}
