package pro.belbix.ethparser.utils.download;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLERS;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;

@Service
@SuppressWarnings("rawtypes")
@Log4j2
public class HardWorkDownloader {

  private final Web3Functions web3Functions;
  private final HardWorkDbService hardWorkDbService;
  private final HardWorkParser hardWorkParser;
  private final AppProperties appProperties;

  @Value("${hardwork-download.from:}")
  private Integer from;
  @Value("${hardwork-download.to:}")
  private Integer to;

  public HardWorkDownloader(Web3Functions web3Functions,
      HardWorkDbService hardWorkDbService,
      HardWorkParser hardWorkParser,
      AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.hardWorkDbService = hardWorkDbService;
    this.hardWorkParser = hardWorkParser;
    this.appProperties = appProperties;
  }

  public void start() {
    log.info("HardWorkDownloader start");
    new LoopHandler(appProperties.getHandleLoopStep(), this::parse).start(from, to);

  }

  private void parse(Integer start, Integer end) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(CONTROLLERS.get(appProperties.getUtilNetwork())),
            start, end, appProperties.getUtilNetwork());
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    handleLogs(logResults);
  }

  public void handleLogs(List<LogResult> logResults) {
    for (LogResult logResult : logResults) {
      try {
        HardWorkDTO dto = hardWorkParser
            .parseLog((Log) logResult.get(), appProperties.getUtilNetwork());
        if (dto != null) {
          hardWorkDbService.save(dto);
        }
      } catch (Exception e) {
        log.error("error with " + logResult.get(), e);
        break;
      }
    }
  }

  public void setFrom(Integer from) {
    this.from = from;
  }

  public void setTo(Integer to) {
    this.to = to;
  }
}
