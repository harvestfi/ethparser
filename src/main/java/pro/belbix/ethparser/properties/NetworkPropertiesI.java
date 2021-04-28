package pro.belbix.ethparser.properties;

public interface NetworkPropertiesI {

  String getWeb3Url();
  String getAbiProviderKey();
  boolean isParseLog();
  String getStartLogBlock();
  boolean isParseTransactions();
  String getStartTransactionBlock();
  boolean isParseBlocks();
  String getParseBlocksFrom();
  boolean isParseUniswapLog();
  boolean isParseHarvestLog();
  boolean isParseHardWorkLog();
  boolean isParseRewardsLog();
  boolean isParseImportantEvents();
  boolean isConvertUniToHarvest();
  boolean isParseTransfers();
  boolean isParsePrices();
  boolean isParseDeployerTransactions();
  boolean isParseDeployerEvents();
}
