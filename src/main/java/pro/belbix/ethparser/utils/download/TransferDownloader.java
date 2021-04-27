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
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.erc20.db.TransferDBService;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TransferDownloader {
  private final ContractUtils contractUtils = ContractUtils.getInstance(ETH_NETWORK);
  private final Web3Functions web3Functions;
  private final PriceProvider priceProvider;
  private final TransferDBService transferDBService;
  private final TransferParser transferParser;
  private final AppProperties appProperties;

  @Value("${transfer-download.contract:}")
  private String contractName;
  @Value("${transfer-download.from:}")
  private Integer from;
  @Value("${transfer-download.to:}")
  private Integer to;

  public TransferDownloader(Web3Functions web3Functions,
      PriceProvider priceProvider,
      TransferDBService transferDBService,
      TransferParser transferParser, AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.priceProvider = priceProvider;
    this.transferDBService = transferDBService;
    this.transferParser = transferParser;
    this.appProperties = appProperties;
  }

  public void start() {
    if (contractName == null || contractName.isEmpty()) {
      throw new IllegalStateException("Empty contract");
    }
    new LoopHandler(appProperties.getHandleLoopStep(),
        (from, end) -> parse(from, end,
            contractUtils.getAddressByName(contractName, ContractType.TOKEN)
                .orElseThrow(() -> new IllegalStateException("Not found adr for " + contractName))
        )).handleLoop(from, to);
  }

  private void parse(Integer start, Integer end, String contract) {
    List<LogResult> logResults = web3Functions.fetchContractLogs(singletonList(contract), start, end, ETH_NETWORK);
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    for (LogResult logResult : logResults) {
      try {
        TransferDTO dto = transferParser.parseLog((Log) logResult.get());
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
