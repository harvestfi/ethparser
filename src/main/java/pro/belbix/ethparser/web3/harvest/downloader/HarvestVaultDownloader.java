package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;

@SuppressWarnings("rawtypes")
@Service
public class HarvestVaultDownloader {
  private final ContractUtils contractUtils = ContractUtils.getInstance(ETH_NETWORK);
  private static final Logger logger = LoggerFactory.getLogger(HarvestVaultDownloader.class);
  private final Web3Functions web3Functions;
  private final HarvestDBService harvestDBService;
  private final HarvestVaultParserV2 harvestVaultParserV2;
  private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;

  @Value("${harvest-download.contract:}")
  private String vaultName;
  @Value("${harvest-download.from:}")
  private Integer from;
  @Value("${harvest-download.to:}")
  private Integer to;

  public HarvestVaultDownloader(Web3Functions web3Functions,
      HarvestDBService harvestDBService,
      HarvestVaultParserV2 harvestVaultParserV2,
      HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator) {
    this.web3Functions = web3Functions;
    this.harvestDBService = harvestDBService;
    this.harvestVaultParserV2 = harvestVaultParserV2;
    this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
  }

  public void start() {
    String vaultAddress = contractUtils.getAddressByName(vaultName, ContractType.VAULT)
        .orElseThrow(() -> new IllegalStateException("Not found address for " + vaultName));
    LoopUtils.handleLoop(from, to, (start, end) -> parse(vaultAddress, start, end));
  }

  private void parse(String vaultHash, Integer start, Integer end) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(vaultHash), start, end, ETH_NETWORK);
    if (logResults.isEmpty()) {
      logger.info("Empty log {} {} {}", start, end, vaultHash);
      return;
    }
    for (LogResult logResult : logResults) {
      try {
        HarvestDTO dto = harvestVaultParserV2.parseVaultLog((Log) logResult.get(), ETH_NETWORK);
        if (dto != null) {
          harvestOwnerBalanceCalculator.fillBalance(dto);
          harvestDBService.saveHarvestDTO(dto);
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
