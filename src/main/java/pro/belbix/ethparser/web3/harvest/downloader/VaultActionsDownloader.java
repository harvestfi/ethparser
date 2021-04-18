package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.harvest.parser.VaultActionsParser;

@SuppressWarnings("rawtypes")
@Service
public class VaultActionsDownloader {
  private static final Logger logger = LoggerFactory.getLogger(VaultActionsDownloader.class);
  private final Web3Functions web3Functions;
  private final VaultActionsDBService vaultActionsDBService;
  private final VaultActionsParser vaultActionsParser;
  private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  private final AppProperties appProperties;

  @Value("${harvest-download.contract:}")
  private String vaultName;
  @Value("${harvest-download.from:}")
  private Integer from;
  @Value("${harvest-download.to:}")
  private Integer to;

  public VaultActionsDownloader(Web3Functions web3Functions,
      VaultActionsDBService vaultActionsDBService,
      VaultActionsParser vaultActionsParser,
      HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator,
      AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.vaultActionsDBService = vaultActionsDBService;
    this.vaultActionsParser = vaultActionsParser;
    this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
    this.appProperties = appProperties;
  }

  public void start() {
    String vaultAddress = ContractUtils.getInstance(appProperties.getNetwork())
        .getAddressByName(vaultName, ContractType.VAULT)
        .orElseThrow(() -> new IllegalStateException("Not found address for " + vaultName));
    LoopUtils.handleLoop(from, to, (start, end) -> parse(vaultAddress, start, end));
  }

  private void parse(String vaultHash, Integer start, Integer end) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(vaultHash), start, end, appProperties.getNetwork());
    if (logResults.isEmpty()) {
      logger.info("Empty log {} {} {}", start, end, vaultHash);
      return;
    }
    for (LogResult logResult : logResults) {
      try {
        HarvestDTO dto = vaultActionsParser
            .parseVaultLog((Log) logResult.get(), appProperties.getNetwork());
        if (dto != null) {
          harvestOwnerBalanceCalculator.fillBalance(dto, appProperties.getNetwork());
          vaultActionsDBService.saveHarvestDTO(dto);
        }
      } catch (Exception e) {
        logger.error("error with " + logResult.get(), e);
      }
    }
  }

  public void setVaultName(String vaultName) {
    this.vaultName = vaultName;
  }

  public void setFrom(Integer from) {
    this.from = from;
  }

  public void setTo(Integer to) {
    this.to = to;
  }
}
