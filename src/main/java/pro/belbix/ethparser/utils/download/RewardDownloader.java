package pro.belbix.ethparser.utils.download;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;
import pro.belbix.ethparser.web3.harvest.parser.RewardParser;

@Service
@SuppressWarnings("rawtypes")
@Log4j2
public class RewardDownloader {

  private final Web3Functions web3Functions;
  private final RewardParser rewardParser;
  private final RewardsDBService rewardsDBService;
  private final AppProperties appProperties;
  private final ContractDbService contractDbService;

  @Value("${reward-download.contract:}")
  private String contractName;
  @Value("${reward-download.vaults:}")
  private String[] vaultNames;
  @Value("${reward-download.exclude:}")
  private String[] exclude;
  @Value("${reward-download.from:}")
  private Integer from;
  @Value("${reward-download.to:}")
  private Integer to;

  public RewardDownloader(Web3Functions web3Functions,
      RewardParser rewardParser,
      RewardsDBService rewardsDBService,
      AppProperties appProperties,
      ContractDbService contractDbService) {
    this.web3Functions = web3Functions;
    this.rewardParser = rewardParser;
    this.rewardsDBService = rewardsDBService;
    this.appProperties = appProperties;
    this.contractDbService = contractDbService;
  }

  public void start() {
    if (vaultNames != null && vaultNames.length != 0) {
      new LoopHandler(appProperties.getHandleLoopStep(),
          (from, end) -> parseContracts(from, end,
              Arrays.stream(vaultNames)
                  .map(vName -> contractDbService
                      .getAddressByName(vName, ContractType.VAULT, appProperties.getUtilNetwork())
                      .orElseThrow())
                  .map(vName -> contractDbService
                      .getPoolContractByVaultAddress(
                          vName, Long.MAX_VALUE, appProperties.getUtilNetwork())
                      .orElseThrow()
                      .getAddress())
                  .collect(Collectors.toList())
          )
      ).start(from, to);
    } else if (!Strings.isBlank(contractName)) {
      log.info("Start parse rewards for " + contractName);
      String adr = contractDbService
          .getAddressByName(contractName, ContractType.POOL, appProperties.getUtilNetwork())
          .orElseThrow(() -> new IllegalStateException("Not found pool for " + contractName));
      new LoopHandler(appProperties.getHandleLoopStep(),
          (from, end) -> parseContracts(from, end, singletonList(adr)))
          .start(from, to);
    } else {
      Set<String> excludeSet = new HashSet<>();
      if (exclude != null && exclude.length != 0) {
        excludeSet.addAll(new HashSet<>(Arrays.asList(exclude)));
      }
      new LoopHandler(appProperties.getHandleLoopStep(),
          (from, end) -> parseContracts(from, end,
              contractDbService.getAllPools(appProperties.getUtilNetwork()).stream()
                  .map(v -> v.getContract().getAddress())
                  .filter(c -> !excludeSet.contains(
                      contractDbService.getNameByAddress(c, appProperties.getUtilNetwork())
                          .orElseThrow()))
                  .collect(Collectors.toList()))
      ).start(from, to);
    }
  }

  private void parseContracts(Integer start, Integer end, List<String> contracts) {
    if (contracts.isEmpty()) {
      throw new IllegalStateException("Empty contracts");
    }
    List<LogResult> logResults =
        web3Functions.fetchContractLogs(
            contracts, start, end, appProperties.getUtilNetwork());
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    handleLogs(logResults);
  }

  public void handleLogs(List<LogResult> logResults) {
    for (LogResult logResult : logResults) {
      try {
        RewardDTO dto = rewardParser.parse((Log) logResult.get(), appProperties.getUtilNetwork());
        if (dto != null) {
          try {
            rewardsDBService.saveRewardDTO(dto);
          } catch (Exception e) {
            log.error("error with {}", dto, e);
            break;
          }
        }
      } catch (Exception e) {
        log.error("error with " + logResult.get(), e);
        break;
      }
    }
  }

  public void setContractName(String contractName) {
    this.contractName = contractName;
  }

  public void setFrom(Integer from) {
    this.from = from;
  }

  public void setTo(Integer to) {
    this.to = to;
  }

  public void setVaultNames(String[] vaultNames) {
    this.vaultNames = vaultNames;
  }
}
