package pro.belbix.ethparser.properties;

import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fUNI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fUNI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fUNI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fUNI_ETH_WBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fWETH;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fUSDC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fUSDT;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fDAI;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fWBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fRENBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fCRVRENWBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fSUSHI_WBTC_TBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fYCRV;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.f_3CRV;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fTUSD;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fCRV_TBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fPS;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fPS_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fCRV_CMPND;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fCRV_BUSD;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fCRV_USDN;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fSUSHI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fSUSHI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fSUSHI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.contracts.RewardVaults.fSUSHI_ETH_WBTC;
import static pro.belbix.ethparser.web3.harvest.parser.HardWorkParser.CONTROLLER;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.CRVRENWBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.CRVRENWBTC_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.CRV_BUSD;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.CRV_CMPND;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.CRV_TBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.CRV_USDN;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.DAI;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.DAI_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.PS;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.PS_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.RENBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.RENBTC_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.SUSHI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.SUSHI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.SUSHI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.SUSHI_ETH_WBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.SUSHI_WBTC_TBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_DAI;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_DAI_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_USDC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_USDC_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_USDT;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_USDT_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_WBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.UNI_ETH_WBTC_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.USDC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.USDC_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.USDT;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.USDT_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.WBTC;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.WBTC_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.WETH;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.WETH_V0;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults.YCRV;
import static pro.belbix.ethparser.web3.harvest.contracts.Vaults._3CRV;
import static pro.belbix.ethparser.web3.uniswap.UniswapLpLogDecoder.FARM_USDC_LP_CONTRACT;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionsProperties {
    private List<String> logSubscriptions = Arrays.asList(
        FARM_USDC_LP_CONTRACT,
        CONTROLLER,
        WETH_V0,
        USDC_V0,
        USDT_V0,
        DAI_V0,
        WBTC_V0,
        RENBTC_V0,
        CRVRENWBTC_V0,
        UNI_ETH_DAI_V0,
        UNI_ETH_USDC_V0,
        UNI_ETH_USDT_V0,
        UNI_ETH_WBTC_V0,
        UNI_ETH_DAI,
        UNI_ETH_USDC,
        UNI_ETH_USDT,
        UNI_ETH_WBTC,
        WETH,
        USDC,
        USDT,
        DAI,
        WBTC,
        RENBTC,
        CRVRENWBTC,
        SUSHI_WBTC_TBTC,
        YCRV,
        _3CRV,
        CRV_TBTC,
        PS,
        PS_V0,
        CRV_CMPND,
        CRV_BUSD,
        CRV_USDN,
        SUSHI_ETH_DAI,
        SUSHI_ETH_USDC,
        SUSHI_ETH_USDT,
        SUSHI_ETH_WBTC,
        fUNI_ETH_DAI,
        fUNI_ETH_USDC,
        fUNI_ETH_USDT,
        fUNI_ETH_WBTC,
        fWETH,
        fUSDC,
        fUSDT,
        fDAI,
        fWBTC,
        fRENBTC,
        fCRVRENWBTC,
        fSUSHI_WBTC_TBTC,
        fYCRV,
        f_3CRV,
        fTUSD,
        fCRV_TBTC,
        fCRV_CMPND,
        fCRV_BUSD,
        fCRV_USDN,
        fSUSHI_ETH_DAI,
        fSUSHI_ETH_USDC,
        fSUSHI_ETH_USDT,
        fSUSHI_ETH_WBTC
    );

    public List<String> getLogSubscriptions() {
        return logSubscriptions;
    }

    public void setLogSubscriptions(List<String> logSubscriptions) {
        this.logSubscriptions = logSubscriptions;
    }

}
