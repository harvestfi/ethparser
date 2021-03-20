package pro.belbix.ethparser.web3.prices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceOracleTest {

    @Autowired
    private PriceOracle priceOracle;
    @Autowired
    private ContractLoader contractLoader;

    @Before
    public void load() {
        contractLoader.load();
    }

    @Test
    public void getTokenPrice() {

        double priceBAS = priceOracle.getPriceForCoin("BAS", 12015725);
        double priceWBTC = priceOracle.getPriceForCoin("WBTC", 12015725);
        double priceWETH = priceOracle.getPriceForCoin("WETH", 12015725);
        double priceUSDC = priceOracle.getPriceForCoin("USDC", 12015725);
        double priceUSDT = priceOracle.getPriceForCoin("USDT", 12015725);
        double priceSUSHI_LP_ETH_WBTC = priceOracle.getPriceForCoin("SUSHI_LP_ETH_WBTC", 12015725);
        double priceCRV_EURS = priceOracle.getPriceForCoin("CRV_EURS", 12015725);
        double priceMGOOGL = priceOracle.getPriceForCoin("MGOOGL", 12015725);
        double priceFARM = priceOracle.getPriceForCoin("FARM", 12015725);
        double priceRENBTC = priceOracle.getPriceForCoin("RENBTC", 12015725);
        double price3CRV = priceOracle.getPriceForCoin("3CRV", 12015725);
        
        assertAll(
            () -> assertEquals("Price", "0,46139880", String.format("%.8f", priceBAS)),
            () -> assertEquals("Price", "55435,19606653", String.format("%.8f", priceWBTC)),
            () -> assertEquals("Price", "1786,09068119", String.format("%.8f", priceWETH)),
            () -> assertEquals("Price", "1,00000000", String.format("%.8f", priceUSDC)),
            () -> assertEquals("Price", "0,99958400", String.format("%.8f", priceUSDT)),
            () -> assertEquals("Price", "47351069766,33282500", String.format("%.8f", priceSUSHI_LP_ETH_WBTC)),
            () -> assertEquals("Price", "1,19183883", String.format("%.8f", priceCRV_EURS)),
            () -> assertEquals("Price", "2079,61790428", String.format("%.8f", priceMGOOGL)),
            () -> assertEquals("Price", "229,46677216", String.format("%.8f", priceFARM)),
            () -> assertEquals("Price", "55435,19606653", String.format("%.8f", priceRENBTC)),
            () -> assertEquals("Price", "1,01426724", String.format("%.8f", price3CRV))
        );
    }
    
}
