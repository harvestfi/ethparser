package pro.belbix.ethparser.utils.download;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;

@SuppressWarnings("rawtypes")
@Service
@Log4j2
public class UniswapLpDownloader {
  private final Web3Functions web3Functions;
  private final UniswapDbService saveHarvestDTO;
  private final UniswapLpLogParser uniswapLpLogParser;
  private final AppProperties appProperties;
  private final ContractDbService contractDbService;

  @Value("${uniswap-download.contract:}")
  private String contractName;
  @Value("${uniswap-download.from:}")
  private Integer from;
  @Value("${uniswap-download.to:}")
  private Integer to;

  public UniswapLpDownloader(Web3Functions web3Functions,
      UniswapDbService saveHarvestDTO,
      UniswapLpLogParser uniswapLpLogParser,
      AppProperties appProperties,
      ContractDbService contractDbService) {
    this.web3Functions = web3Functions;
    this.saveHarvestDTO = saveHarvestDTO;
    this.uniswapLpLogParser = uniswapLpLogParser;
    this.appProperties = appProperties;
    this.contractDbService = contractDbService;
  }

  public void start() {
    new LoopHandler(appProperties.getHandleLoopStep(), this::load).start(from, to);
  }

  private void load(Integer from, Integer to) {
    List<LogResult> logResults = web3Functions.fetchContractLogs(
        singletonList(
            contractDbService
                .getAddressByName(contractName, ContractType.UNI_PAIR,
                    appProperties.getUtilNetwork())
                .orElseThrow()),
        from,
        to, ETH_NETWORK
    );
    if (logResults == null) {
      log.error("Log results is null");
      return;
    }
    for (LogResult logResult : logResults) {
      UniswapDTO dto = null;
      try {
        dto = uniswapLpLogParser.parse((Log) logResult.get(), ETH_NETWORK);
        if (dto != null) {
          saveHarvestDTO.saveUniswapDto(dto);
        }
      } catch (Exception e) {
        log.info("Downloader error  " + dto, e);
      }
    }
  }

}
