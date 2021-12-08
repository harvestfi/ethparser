package pro.belbix.ethparser.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ethparser.eth")
@Getter
@Setter
public class EthAppProperties implements NetworkPropertiesI {

  private String web3Url = "";
  private String abiProviderKey = "";
  private int blockStep = 100;
  private int web3Timeout = 60;

  // log parsing
  private boolean parseLog = true;
  private String startLogBlock = "";
  private boolean parseUniswapLog = true;
  private boolean parseBancorLog = true;
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
  private boolean parseDeployerTransactions = true;
  private boolean parseDeployerEvents = true;

  // strat info grabber
  private boolean grabStratInfo = false;

  // block parsing
  private boolean parseBlocks = false;
  private String parseBlocksFrom = "";

}
