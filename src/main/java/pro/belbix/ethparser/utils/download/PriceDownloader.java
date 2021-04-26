package pro.belbix.ethparser.utils.download;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_UNI_PAIRS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.utils.LoopUtils;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.parser.PriceLogParser;

@Service
@Log4j2
@SuppressWarnings("rawtypes")
public class PriceDownloader {
  private final Web3Functions web3Functions;
  private final PriceRepository priceRepository;
  private final PriceLogParser priceLogParser;
  private final AppProperties appProperties;

  @Value("${price-download.contracts:}")
  private String[] contractNames;
  @Value("${price-download.from:}")
  private Integer from;
  @Value("${price-download.to:}")
  private Integer to;

  public PriceDownloader(Web3Functions web3Functions,
      PriceRepository priceRepository,
      PriceLogParser priceLogParser, AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.priceRepository = priceRepository;
    this.priceLogParser = priceLogParser;
    this.appProperties = appProperties;
  }

  public void start() {
    if (contractNames.length == 0) {
      contractNames = PARSABLE_UNI_PAIRS.get(appProperties.getUtilNetwork()).stream()
          .map(c -> ContractUtils.getInstance(appProperties.getUtilNetwork())
              .getNameByAddress(c)
              .orElseThrow(() -> new IllegalStateException("Not found name for " + c)))
          .collect(Collectors.toSet())
          .toArray(contractNames);
    }
    for (String contractName : contractNames) {
      String contractHash = ContractUtils.getInstance(appProperties.getUtilNetwork())
          .getAddressByName(contractName, ContractType.UNI_PAIR)
          .orElseThrow(() -> new IllegalStateException("Not found hash for " + contractName));
      LoopUtils.handleLoop(from, to, (start, end) -> parse(start, end, contractHash));
    }
  }

  private void parse(Integer start, Integer end, String contractName) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(contractName), start, end, appProperties.getUtilNetwork());
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    List<PriceDTO> result = new ArrayList<>();
    for (LogResult logResult : logResults) {
      try {
        PriceDTO dto = priceLogParser.parse((Log) logResult.get(), appProperties.getUtilNetwork());
        if (dto != null) {
          result.add(dto);
        }
        if (result.size() > 100) {
          priceRepository.saveAll(result);
          result.clear();
          log.info("Saved a bunch, last " + dto);
        }
      } catch (Exception e) {
        log.error("error with " + logResult.get(), e);
        break;
      }
    }
    priceRepository.saveAll(result);
  }
}
