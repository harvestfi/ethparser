package pro.belbix.ethparser.properties;

import static pro.belbix.ethparser.web3.harvest.Vaults.*;
import static pro.belbix.ethparser.web3.uniswap.UniswapLpLogDecoder.FARM_USDC_LP_CONTRACT;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "web3")
public class Web3Properties {

    private String web3Url = "";
    private String web3User = "";
    private String web3Password = "";
    private String startBlock = "";
    private String startLogBlock = "";
    private List<String> logSubscriptions = Arrays.asList(
        FARM_USDC_LP_CONTRACT,
//        WETH_V0,
//        USDC_V0,
//        USDT_V0,
//        TUSD,
//        DAI_V0,
//        WBTC_V0,
//        RENBTC_V0,
//        CRVRENWBTC_V0,
//        UNI_ETH_DAI_V0,
//        UNI_ETH_USDC_V0,
//        UNI_ETH_USDT_V0,
//        UNI_ETH_WBTC_V0,
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
        SUSHI_WBTC_TBTC
    );
    private boolean testWs = false;
    private int testWsRate = 1000;
    private boolean parseUniswapLog = true;
    private boolean parseHarvestLog = false;
    private boolean parseTransactions = false;
    private boolean parseHarvest = false;

    public String getWeb3User() {
        return web3User;
    }

    public void setWeb3User(String web3User) {
        this.web3User = web3User;
    }

    public String getWeb3Password() {
        return web3Password;
    }

    public void setWeb3Password(String web3Password) {
        this.web3Password = web3Password;
    }

    public List<String> getLogSubscriptions() {
        return logSubscriptions;
    }

    public void setLogSubscriptions(List<String> logSubscriptions) {
        this.logSubscriptions = logSubscriptions;
    }

    public boolean isParseHarvestLog() {
        return parseHarvestLog;
    }

    public void setParseHarvestLog(boolean parseHarvestLog) {
        this.parseHarvestLog = parseHarvestLog;
    }

    public boolean isParseUniswapLog() {
        return parseUniswapLog;
    }

    public void setParseUniswapLog(boolean parseUniswapLog) {
        this.parseUniswapLog = parseUniswapLog;
    }

    public String getStartLogBlock() {
        return startLogBlock;
    }

    public void setStartLogBlock(String startLogBlock) {
        this.startLogBlock = startLogBlock;
    }

    public String getWeb3Url() {
        return web3Url;
    }

    public void setWeb3Url(String web3Url) {
        this.web3Url = web3Url;
    }

    public String getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(String startBlock) {
        this.startBlock = startBlock;
    }

    public boolean isTestWs() {
        return testWs;
    }

    public void setTestWs(boolean testWs) {
        this.testWs = testWs;
    }

    public int getTestWsRate() {
        return testWsRate;
    }

    public void setTestWsRate(int testWsRate) {
        this.testWsRate = testWsRate;
    }

    public boolean isParseTransactions() {
        return parseTransactions;
    }

    public void setParseTransactions(boolean parseTransactions) {
        this.parseTransactions = parseTransactions;
    }

    public boolean isParseHarvest() {
        return parseHarvest;
    }

    public void setParseHarvest(boolean parseHarvest) {
        this.parseHarvest = parseHarvest;
    }
}
