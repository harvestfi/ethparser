package pro.belbix.ethparser.properties;

import static pro.belbix.ethparser.web3.uniswap.UniswapLpLogDecoder.FARM_USDC_LP_CONTRACT;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "web3")
public class Web3Properties {

    private String web3Url = "";
    private String startBlock = "";
    private String startLogBlock = "";
    private String startLogHash = FARM_USDC_LP_CONTRACT;
    private boolean testWs = false;
    private int testWsRate = 1000;
    private boolean parseUniswapLog = true;
    private boolean parseTransactions = false;
    private boolean parseHarvest = false;

    public boolean isParseUniswapLog() {
        return parseUniswapLog;
    }

    public void setParseUniswapLog(boolean parseUniswapLog) {
        this.parseUniswapLog = parseUniswapLog;
    }

    public String getStartLogHash() {
        return startLogHash;
    }

    public void setStartLogHash(String startLogHash) {
        this.startLogHash = startLogHash;
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
