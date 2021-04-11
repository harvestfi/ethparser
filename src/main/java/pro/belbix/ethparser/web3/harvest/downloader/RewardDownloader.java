package pro.belbix.ethparser.web3.harvest.downloader;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.utils.LoopUtils.handleLoop;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RewardDownloader {

  private static final Logger logger = LoggerFactory.getLogger(HardWorkDownloader.class);
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
      handleLoop(from, to, (from, end) -> parse(from, end, adr));
    } else {
      Set<String> excludeSet = new HashSet<>();
      if (exclude != null && exclude.length != 0) {
        excludeSet = new HashSet<>(Arrays.asList(exclude));
      }
      for (String contractAddress : ContractUtils.getInstance(appProperties.getNetwork())
          .getAllPools().stream()
          .map(v -> v.getContract().getAddress())
          .collect(Collectors.toList())) {
        if (excludeSet.contains(
            ContractUtils.getInstance(appProperties.getNetwork()).getNameByAddress(contractAddress)
                .orElseThrow())) {
          continue;
        }
        logger.info("Start parse rewards for " + contractName);
        handleLoop(from, to, (from, end) -> parse(from, end, contractAddress));
      }
    }
  }

  private void parse(Integer start, Integer end, String contract) {
    List<LogResult> logResults =
        web3Functions.fetchContractLogs(
            singletonList(contract), start, end, appProperties.getNetwork());
    if (logResults.isEmpty()) {
      logger.info("Empty log {} {}", start, end);
      return;
    }
    for (LogResult logResult : logResults) {
      try {
        RewardDTO dto = rewardParser.parseLog((Log) logResult.get());
        if (dto != null) {
          try {
            rewardsDBService.saveRewardDTO(dto);
          } catch (Exception e) {
            logger.error("error with {}", dto, e);
            break;
          }
        }
      } catch (Exception e) {
        logger.error("error with " + logResult.get(), e);
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
