package pro.belbix.ethparser.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "web3")
public class Web3Properties {

    private String web3Url = "";
    private String startBlock = "";
    private boolean testWs = false;
    private int testWsRate = 1000;
    private boolean parseTransactions = true;
    private boolean parseHarvest = true;

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
