package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.utils.LoopUtils.handleLoop;

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
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
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

  @Value("${reward-download.contract:}")
  private String contractName;
  @Value("${reward-download.exclude:}")
  private String[] exclude;
  @Value("${reward-download.from:}")
  private Integer from;
  @Value("${reward-download.to:}")
  private Integer to;

  public RewardDownloader(Web3Functions web3Functions,
      RewardParser rewardParser,
      RewardsDBService rewardsDBService,
      AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.rewardParser = rewardParser;
    this.rewardsDBService = rewardsDBService;
    this.appProperties = appProperties;
  }

  public void start() {
    if (!Strings.isBlank(contractName)) {
      String adr = ContractUtils.getInstance(appProperties.getNetwork())
          .getAddressByName(contractName, ContractType.POOL)
          .orElseThrow();
      handleLoop(from, to, (from, end) -> parseContracts(from, end, singletonList(adr)));
    } else {
      Set<String> excludeSet = new HashSet<>();
      if (exclude != null && exclude.length != 0) {
        excludeSet.addAll(new HashSet<>(Arrays.asList(exclude)));
      }

      log.info("Start parse rewards for " + contractName);
      handleLoop(from, to, (from, end) -> parseContracts(from, end,
          ContractUtils.getInstance(appProperties.getNetwork())
              .getAllPools().stream()
              .map(v -> v.getContract().getAddress())
              .filter(c -> !excludeSet.contains(
                  ContractUtils.getInstance(appProperties.getNetwork()).getNameByAddress(c)
                      .orElseThrow()))
              .collect(Collectors.toList())));
    }
  }

  private void parseContracts(Integer start, Integer end, List<String> contracts) {
    if (contracts.isEmpty()) {
      throw new IllegalStateException("Empty contracts");
    }
    List<LogResult> logResults =
        web3Functions.fetchContractLogs(
            contracts, start, end, appProperties.getNetwork());
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    for (LogResult logResult : logResults) {
      try {
        RewardDTO dto = rewardParser.parseLog((Log) logResult.get(), appProperties.getNetwork());
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
}
