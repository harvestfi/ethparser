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
import pro.belbix.ethparser.dto.v0.ErrorWeb3Dto;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.repositories.ErrorsRepository;
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
  private final ErrorsRepository errorsRepository;
  private final DeployerTransactionsParser deployerTransactionsParser;
  private final ImportantEventsParser importantEventsParser;
  private final HardWorkParser hardWorkParser;
  private final PriceLogParser priceLogParser;
  private final RewardParser rewardParser;
  private final TransferParser transferParser;
  private final UniToHarvestConverter uniToHarvestConverter;
  private final UniswapLpLogParser uniswapLpLogParser;
  private final VaultActionsParser vaultActionsParser;

  public ErrorService(ErrorsRepository errorsRepository,
      DeployerTransactionsParser deployerTransactionsParser,
      ImportantEventsParser importantEventsParser,
      HardWorkParser hardWorkParser,
      PriceLogParser priceLogParser,
      RewardParser rewardParser,
      TransferParser transferParser,
      UniToHarvestConverter uniToHarvestConverter,
      UniswapLpLogParser uniswapLpLogParser,
      VaultActionsParser vaultActionsParser) {
    this.errorsRepository = errorsRepository;
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
    List<ErrorWeb3Dto> listErrors = getErrorsFromDb();
    log.info("Load errors from db: " + listErrors.size());
    for (ErrorWeb3Dto errorWeb3Dto : listErrors) {
      try {
        parseObject(errorWeb3Dto);
        deleteFromBase(errorWeb3Dto);
      } catch (Exception e) {
        log.info("Can't parse error " + e.getMessage()
            + " model:" + errorWeb3Dto.toString());
      }
      log.info("End parse error: " + errorWeb3Dto.toString());
    }
  }

  public void parseObject(ErrorWeb3Dto errorWeb3Dto) {
    switch (errorWeb3Dto.getErrorClass()) {
      case "DeployerTransactionsParser":
        deployerTransactionsParser
            .parse(parseJsonToTransaction(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "HardWorkParser":
        hardWorkParser
            .parse(parseJsonToLog(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "ImportantEventsParser":
        importantEventsParser
            .parse(parseJsonToLog(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "PriceLogParser":
        priceLogParser
            .parse(parseJsonToLog(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "RewardParser":
        rewardParser
            .parse(parseJsonToLog(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "TransferParser":
        transferParser
            .parse(parseJsonToLog(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "UniToHarvestConverter":
        uniToHarvestConverter
            .parse(parseJsonToUniswapDTO(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "UniswapLpLogParser":
        uniswapLpLogParser
            .parse(parseJsonToLog(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      case "VaultActionsParser":
        vaultActionsParser
            .parse(parseJsonToLog(errorWeb3Dto.getJson()), errorWeb3Dto.getNetwork());
        break;
      default:
        log.info("Can't parse unknown error: " + errorWeb3Dto.toString());
    }
  }

  private void deleteFromBase(ErrorWeb3Dto errorWeb3Dto) {
    errorsRepository.delete(errorWeb3Dto);
  }

  public List<ErrorWeb3Dto> getErrorsFromDb() {
    return errorsRepository.findAll();
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

