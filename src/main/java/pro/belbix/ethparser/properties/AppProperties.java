package pro.belbix.ethparser.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "web3")
public class AppProperties {


    private String web3Url = "";
    private String web3User = "";
    private String web3Password = "";

    private boolean testWs = false;
    private int testWsRate = 1000;
    private boolean devMod = false;
    private String startUtil = "";

    private boolean overrideDuplicates = false;
    private boolean stubPrice = false;

    private String startLogBlock = "";
    private boolean parseUniswapLog = true;
    private boolean parseHarvestLog = true;
    private boolean parseHardWorkLog = true;

    private String startBlock = "";
    private boolean parseTransactions = false;
    private boolean parseHarvest = false;

    public String getStartUtil() {
        return startUtil;
    }

    public void setStartUtil(String startUtil) {
        this.startUtil = startUtil;
    }

    public boolean isDevMod() {
        return devMod;
    }

    public void setDevMod(boolean devMod) {
        this.devMod = devMod;
    }

    public boolean isParseHardWorkLog() {
        return parseHardWorkLog;
    }

    public void setParseHardWorkLog(boolean parseHardWorkLog) {
        this.parseHardWorkLog = parseHardWorkLog;
    }

    public boolean isStubPrice() {
        return stubPrice;
    }

    public void setStubPrice(boolean stubPrice) {
        this.stubPrice = stubPrice;
    }

    public boolean isOverrideDuplicates() {
        return overrideDuplicates;
    }

    public void setOverrideDuplicates(boolean overrideDuplicates) {
        this.overrideDuplicates = overrideDuplicates;
    }

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
