package pro.belbix.ethparser.web3.prices;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.TestUtils.numberFormat;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

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

        double priceBAS = priceProvider.getPriceForCoin("BAS", 12015725, ETH_NETWORK);
        double priceWBTC = priceProvider.getPriceForCoin("WBTC", 12015725, ETH_NETWORK);
        double priceWETH = priceProvider.getPriceForCoin("ETH", 12015725, ETH_NETWORK);
        double priceUSDC = priceProvider.getPriceForCoin("USDC", 12015725, ETH_NETWORK);
        double priceUSDT = priceProvider.getPriceForCoin("USDT", 12015725, ETH_NETWORK);
        double priceSUSHI_LP_ETH_WBTC = priceProvider
            .getLpTokenUsdPrice("0xceff51756c56ceffca006cd410b03ffc46dd3a58", 1, 12015725,
                ETH_NETWORK);
        double priceCRV_EURS = priceProvider.getPriceForCoin("CRV_EURS", 12015725, ETH_NETWORK);
        double priceMGOOGL = priceProvider.getPriceForCoin("MGOOGL", 12015725, ETH_NETWORK);
        double priceFARM = priceProvider.getPriceForCoin("FARM", 12015725, ETH_NETWORK);
        double priceRENBTC = priceProvider.getPriceForCoin("RENBTC", 12015725, ETH_NETWORK);
        double price3CRV = priceProvider.getPriceForCoin("3CRV", 12015725, ETH_NETWORK);

        assertAll(
            () -> assertEquals("priceBAS", numberFormat("0,46139880"),
                String.format("%.8f", priceBAS)),
            () -> assertEquals("priceWBTC", numberFormat("55435,19606653"),
                String.format("%.8f", priceWBTC)),
            () -> assertEquals("priceWETH", numberFormat("1786,09068119"),
                String.format("%.8f", priceWETH)),
            () -> assertEquals("priceUSDC", numberFormat("1,00000000"),
                String.format("%.8f", priceUSDC)),
            () -> assertEquals("priceUSDT", numberFormat("0,99958400"),
                String.format("%.8f", priceUSDT)),
            () -> assertEquals("priceSUSHI_LP_ETH_WBTC", numberFormat("47351069766,33282500"),
                String.format("%.8f", priceSUSHI_LP_ETH_WBTC)),
            () -> assertEquals("priceCRV_EURS", numberFormat("1,19183883"),
                String.format("%.8f", priceCRV_EURS)),
            () -> assertEquals("priceMGOOGL", numberFormat("2079,61790428"),
                String.format("%.8f", priceMGOOGL)),
            () -> assertEquals("priceFARM", numberFormat("229,46677216"),
                String.format("%.8f", priceFARM)),
            () -> assertEquals("priceRENBTC", numberFormat("55435,19606653"),
                String.format("%.8f", priceRENBTC)),
            () -> assertEquals("price3CRV", numberFormat("1,01426724"),
                String.format("%.8f", price3CRV))
        );
    }

    @Test
    public void testCacheResponse () {
        checkTokenPrice("WBTC", 12015725, 55435.19606653);
        checkTokenPrice("WBTC", 12015725, 55435.19606653);
    }
    
    private void checkTokenPrice(String name, long block, double price) {
        double response = priceProvider.getPriceForCoin(name, block, ETH_NETWORK);

        assertAll(
            () -> assertEquals("Price", String.format("%.8f", price), String.format("%.8f", response))
        );
    }
    
}
