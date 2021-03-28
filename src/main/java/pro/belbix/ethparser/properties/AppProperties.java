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
    private String etherscanApiKey = "YourApiKeyToken";

    private boolean onlyApi = false;
    private boolean onlyParse = false;
    private boolean testWs = false;
    private int testWsRate = 1000;
    private String startUtil = "";

    private boolean overrideDuplicates = false;
    private boolean stubPrice = false;

    // log parsing
    private boolean parseLog = true;
    private String startLogBlock = "";
    private boolean parseUniswapLog = true;
    private boolean parseHarvestLog = true;
    private boolean parseHardWorkLog = true;
    private boolean parseRewardsLog = true;
    private boolean parseImportantEvents = true;
    private boolean convertUniToHarvest = true;
    private boolean parseTransfers = true;
    private boolean parsePrices = true;

    // transaction parsing
    private boolean parseTransactions = true;
    private String startTransactionBlock = "";
    private boolean parseHarvest = false;
    private boolean parseDeployerTransactions = true;

    // block parsing
    private boolean parseBlocks = false;
    private String parseBlocksFrom = "";

    private boolean updateContracts = false;
    private boolean stopOnParseError = false;
    private boolean skipSimilarPrices = true;
}
