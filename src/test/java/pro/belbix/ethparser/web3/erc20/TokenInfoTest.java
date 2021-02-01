package pro.belbix.ethparser.web3.erc20;

import static org.junit.Assert.assertEquals;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_TOKEN;
import static pro.belbix.ethparser.web3.erc20.Tokens.USDC_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.WETH_NAME;

import org.junit.Test;

public class TokenInfoTest {

    private final TokenInfo farmTokenInfo = new TokenInfo(FARM_NAME, FARM_TOKEN, 10777201)
        .addLp("UNI_LP_USDC_FARM", 0, USDC_NAME)
        .addLp("UNI_LP_WETH_FARM", 11609000, WETH_NAME);

    @Test
    public void FindActualLpTest() {
        assertEquals("lp name", "UNI_LP_USDC_FARM", farmTokenInfo.findLp(0L).component1());
        assertEquals("lp name", "UNI_LP_USDC_FARM", farmTokenInfo.findLp(11608999L).component1());
        assertEquals("lp name", "UNI_LP_WETH_FARM", farmTokenInfo.findLp(11609000L).component1());
        assertEquals("lp name", "UNI_LP_WETH_FARM", farmTokenInfo.findLp(11609001L).component1());
    }
}
