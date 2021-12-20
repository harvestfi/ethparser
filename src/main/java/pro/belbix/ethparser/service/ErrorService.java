package pro.belbix.ethparser.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.entity.ErrorEntity;
import pro.belbix.ethparser.web3.contracts.db.ErrorDbService;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;
import pro.belbix.ethparser.web3.erc20.parser.TransferParser;
import pro.belbix.ethparser.web3.harvest.parser.HardWorkParser;
import pro.belbix.ethparser.web3.harvest.parser.ImportantEventsParser;
import pro.belbix.ethparser.web3.harvest.parser.RewardParser;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;
import pro.belbix.ethparser.web3.harvest.parser.VaultActionsParser;
import pro.belbix.ethparser.web3.prices.parser.PriceLogParser;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;

@Log4j2
@Service
public class ErrorService {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  private final ErrorDbService errorDbService;
  private final DeployerTransactionsParser deployerTransactionsParser;
  private final ImportantEventsParser importantEventsParser;
  private final HardWorkParser hardWorkParser;
  private final PriceLogParser priceLogParser;
  private final RewardParser rewardParser;
  private final TransferParser transferParser;
  private final UniToHarvestConverter uniToHarvestConverter;
  private final UniswapLpLogParser uniswapLpLogParser;
  private final VaultActionsParser vaultActionsParser;

  public ErrorService(
      ErrorDbService errorDbService,
      DeployerTransactionsParser deployerTransactionsParser,
      ImportantEventsParser importantEventsParser,
      HardWorkParser hardWorkParser,
      PriceLogParser priceLogParser,
      RewardParser rewardParser,
      TransferParser transferParser,
      UniToHarvestConverter uniToHarvestConverter,
      UniswapLpLogParser uniswapLpLogParser,
      VaultActionsParser vaultActionsParser) {
    this.errorDbService = errorDbService;
    this.deployerTransactionsParser = deployerTransactionsParser;
    this.importantEventsParser = importantEventsParser;
    this.hardWorkParser = hardWorkParser;
    this.priceLogParser = priceLogParser;
    this.rewardParser = rewardParser;
    this.transferParser = transferParser;
    this.uniToHarvestConverter = uniToHarvestConverter;
    this.uniswapLpLogParser = uniswapLpLogParser;
    this.vaultActionsParser = vaultActionsParser;
  }

  @Scheduled(fixedRate = 3 * 60 * 60 * 1000)
  public void startFixErrorService() {
    List<ErrorEntity> listErrors = errorDbService.getAllErrors();
    log.info("Load errors from db: " + listErrors.size());
    for (ErrorEntity errorEntity : listErrors) {
      try {
        if (errorEntity.getStatus() != null && errorEntity.getStatus() == 1) {
          continue;
        }
        parseObject(errorEntity);
        errorDbService.delete(errorEntity);
      } catch (Exception e) {
        log.info("Can't parse error " + e.getMessage()
            + " model:" + errorEntity.toString());
      }
      log.info("End parse error: " + errorEntity.toString());
    }
  }

  public void parseObject(ErrorEntity errorEntity) {
    if (errorEntity == null || errorEntity.getErrorClass() == null) {
      throw new IllegalStateException("Detected unknown errorClass: " + errorEntity);
    }
    switch (errorEntity.getErrorClass()) {
      case "DeployerTransactionsParser":
        deployerTransactionsParser
            .parse(parseJsonToTransaction(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "HardWorkParser":
        hardWorkParser
            .parse(parseJsonToLog(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "ImportantEventsParser":
        importantEventsParser
            .parse(parseJsonToLog(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "PriceLogParser":
        priceLogParser
            .parse(parseJsonToLog(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "RewardParser":
        rewardParser
            .parse(parseJsonToLog(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "TransferParser":
        transferParser
            .parse(parseJsonToLog(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "UniToHarvestConverter":
        uniToHarvestConverter
            .parse(parseJsonToUniswapDTO(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "UniswapLpLogParser":
        uniswapLpLogParser
            .parse(parseJsonToLog(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      case "VaultActionsParser":
        vaultActionsParser
            .parse(parseJsonToLog(errorEntity.getJson()), errorEntity.getNetwork());
        break;
      default:
        log.error("Can't parse unknown error: " + errorEntity.toString());
        throw new IllegalStateException("Detected unknown errorClass: " + errorEntity.toString());
    }
  }

  @SneakyThrows
  public Log parseJsonToLog(String json) {
    return objectMapper.readValue(json, Log.class);
  }

  @SneakyThrows
  public Transaction parseJsonToTransaction(String json) {
    return objectMapper.readValue(json, Transaction.class);
  }

  @SneakyThrows
  public UniswapDTO parseJsonToUniswapDTO(String json) {
    return objectMapper.readValue(json, UniswapDTO.class);
  }
}

