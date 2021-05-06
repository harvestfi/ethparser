package pro.belbix.ethparser.utils.download;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.harvest.parser.VaultActionsParser;

@SuppressWarnings("rawtypes")
@Service
@Log4j2
public class VaultActionsDownloader {

  private final Web3Functions web3Functions;
  private final VaultActionsDBService vaultActionsDBService;
  private final VaultActionsParser vaultActionsParser;
  private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  private final AppProperties appProperties;
  private final ContractDbService contractDbService;

  @Value("${harvest-download.contract:}")
  private String vaultName;
  @Value("${harvest-download.contracts:}")
  private String[] contracts;
  @Value("${harvest-download.from:}")
  private Integer from;
  @Value("${harvest-download.to:}")
  private Integer to;

  public VaultActionsDownloader(Web3Functions web3Functions,
      VaultActionsDBService vaultActionsDBService,
      VaultActionsParser vaultActionsParser,
      HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator,
      AppProperties appProperties,
      ContractDbService contractDbService) {
    this.web3Functions = web3Functions;
    this.vaultActionsDBService = vaultActionsDBService;
    this.vaultActionsParser = vaultActionsParser;
    this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
    this.appProperties = appProperties;
    this.contractDbService = contractDbService;
  }

  public void start() {
    if (contracts != null) {
      new LoopHandler(appProperties.getHandleLoopStep(),
          (start, end) ->
              parse(Arrays.stream(contracts)
                      .map(this::nameToAddress)
                      .collect(Collectors.toList()),
                  start, end)).start(from, to);
    } else {
      log.info("Start load vault actions for {} on network {} from {} to {}",
          vaultName, appProperties.getUtilNetwork(), from, to);
      String vaultAddress = nameToAddress(vaultName);
      new LoopHandler(appProperties.getHandleLoopStep(),
          (start, end) -> parse(singletonList(vaultAddress), start, end))
          .start(from, to);
    }
  }

  private String nameToAddress(String name) {
    return contractDbService
        .getAddressByName(name, ContractType.VAULT, appProperties.getUtilNetwork())
        .orElseThrow(() -> new IllegalStateException("Not found address for " + name));
  }

  private void parse(List<String> addresses, Integer start, Integer end) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(addresses, start, end, appProperties.getUtilNetwork());
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    handleLogs(logResults);
  }

  public void handleLogs(List<LogResult> logResults) {
    for (LogResult logResult : logResults) {
      try {
        HarvestDTO dto = vaultActionsParser
            .parse((Log) logResult.get(), appProperties.getUtilNetwork());
        if (dto != null) {
          harvestOwnerBalanceCalculator.fillBalance(dto, appProperties.getUtilNetwork());
          vaultActionsDBService.saveHarvestDTO(dto);
        }
      } catch (Exception e) {
        log.error("error with " + logResult.get(), e);
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

  public void setContracts(String[] contracts) {
    this.contracts = contracts;
  }
}
