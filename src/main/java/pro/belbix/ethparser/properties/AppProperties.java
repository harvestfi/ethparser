package pro.belbix.ethparser.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ethparser")
@Getter
@Setter
public class AppProperties {

    private String web3Url = "";
    private String web3User = "";
    private String web3Password = "";

    private boolean onlyApi = false;
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
    private boolean parseRewardsLog = true;
    private boolean parseImportantEvents = true;
    private boolean convertUniToHarvest = true;
    private boolean parseTransfers = true;

    private String startBlock = "";
    private boolean parseTransactions = false;
    private boolean parseHarvest = false;

    private boolean parseLog = true;
}
