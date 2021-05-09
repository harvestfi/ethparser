package pro.belbix.ethparser.utils.download;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TransferDownloader {
  private final Web3Functions web3Functions;
  private final PriceProvider priceProvider;
  private final TransferDBService transferDBService;
  private final TransferParser transferParser;
  private final AppProperties appProperties;
  private final ContractDbService contractDbService;

  @Value("${transfer-download.contract:}")
  private String contractName;
  @Value("${transfer-download.from:}")
  private Integer from;
  @Value("${transfer-download.to:}")
  private Integer to;

  public TransferDownloader(Web3Functions web3Functions,
      PriceProvider priceProvider,
      TransferDBService transferDBService,
      TransferParser transferParser, AppProperties appProperties,
      ContractDbService contractDbService) {
    this.web3Functions = web3Functions;
    this.priceProvider = priceProvider;
    this.transferDBService = transferDBService;
    this.transferParser = transferParser;
    this.appProperties = appProperties;
    this.contractDbService = contractDbService;
  }

  public void start() {
    if (contractName == null || contractName.isEmpty()) {
      throw new IllegalStateException("Empty contract");
    }
    new LoopHandler(appProperties.getHandleLoopStep(),
        (from, end) -> parse(from, end,
            contractDbService
                .getAddressByName(contractName,
                    ContractType.TOKEN,
                    appProperties.getUtilNetwork())
                .orElseThrow(() -> new IllegalStateException("Not found adr for " + contractName))
        )).start(from, to);
  }

  private void parse(Integer start, Integer end, String contract) {
    List<LogResult> logResults = web3Functions.fetchContractLogs(singletonList(contract), start, end, ETH_NETWORK);
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    for (LogResult logResult : logResults) {
      try {
        TransferDTO dto = transferParser.parse((Log) logResult.get(), ETH_NETWORK);
        if (dto != null) {
          transferDBService.saveDto(dto);
        }
      } catch (Exception e) {
        log.error("error with " + logResult.get(), e);
        break;
      }
    }
  }

}
