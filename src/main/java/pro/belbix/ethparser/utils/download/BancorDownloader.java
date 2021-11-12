package pro.belbix.ethparser.utils.download;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BANCOR_CONVERSION_ADDRESS;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.BancorDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.BancorRepository;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.bancor.BancorPriceParser;

@Service
@Log4j2
@SuppressWarnings("rawtypes")
public class BancorDownloader {

  private final Web3Functions web3Functions;
  private final BancorRepository bancorRepository;
  private final BancorPriceParser bancorPriceParser;
  private final AppProperties appProperties;

  @Value("${bancor-download.from:}")
  private Integer from;
  @Value("${bancor-download.to:}")
  private Integer to;

  public BancorDownloader(Web3Functions web3Functions,
      BancorRepository bancorRepository,
      BancorPriceParser bancorPriceParser,
      AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.bancorRepository = bancorRepository;
    this.bancorPriceParser = bancorPriceParser;
    this.appProperties = appProperties;
  }

  public void start() {
    log.info("BancorDownloader start");
    new LoopHandler(appProperties.getHandleLoopStep(), this::parse).start(from, to);

  }

  private void parse(Integer start, Integer end) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(BANCOR_CONVERSION_ADDRESS),
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
        BancorDTO dto = bancorPriceParser
            .parse((Log) logResult.get(), appProperties.getUtilNetwork());
        if (dto != null) {
          bancorRepository.save(dto);
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
