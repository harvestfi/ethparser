package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.STRATEGY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.STRATEGY_TIME_LOCK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.ImportantEventsDTO;
import pro.belbix.ethparser.model.ImportantEventsInfo;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.model.tx.ImportantEventsTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.db.ImportantEventsDbService;
import pro.belbix.ethparser.web3.harvest.decoder.ImportantEventsLogDecoder;

@Service
@Log4j2
public class ImportantEventsParser extends Web3Parser<ImportantEventsDTO, Log> {

  public static final String TOKEN_MINTED = "TokenMinted";
  private final ImportantEventsLogDecoder importantEventsLogDecoder = new ImportantEventsLogDecoder();
  private final Web3Subscriber web3Subscriber;
  private final ImportantEventsDbService importantEventsDbService;
  private final EthBlockService ethBlockService;
  private final FunctionsUtils functionsUtils;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;

  public ImportantEventsParser(
      Web3Subscriber web3Subscriber,
      ImportantEventsDbService importantEventsDbService,
      ParserInfo parserInfo,
      EthBlockService ethBlockService,
      FunctionsUtils functionsUtils, AppProperties appProperties,
      NetworkProperties networkProperties,
      ContractDbService contractDbService) {
    super(parserInfo, appProperties);
    this.web3Subscriber = web3Subscriber;
    this.importantEventsDbService = importantEventsDbService;
    this.ethBlockService = ethBlockService;
    this.functionsUtils = functionsUtils;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
  }

  @Override
  protected void subscribeToInput() {
    web3Subscriber.subscribeOnLogs(input, this.getClass().getSimpleName());
  }

  @Override
  protected boolean save(ImportantEventsDTO dto) {
    return importantEventsDbService.save(dto);
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network).isParseImportantEvents();
  }

  @Override
  public ImportantEventsDTO parse(Log ethLog, String network) {
    if (ethLog == null ||
        (!ContractConstants.FARM_TOKEN.equals(ethLog.getAddress())
            && contractDbService
            .getNameByAddress(ethLog.getAddress(), network).isEmpty())
    ) {
      return null;
    }

    ImportantEventsTx tx = importantEventsLogDecoder.decode(ethLog);
    if (tx == null) {
      return null;
    }

    ImportantEventsDTO dto = new ImportantEventsDTO();
    dto.setNetwork(network);
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock());
    dto.setOldStrategy(tx.getOldStrategy());
    dto.setNewStrategy(tx.getNewStrategy());
    dto.setHash(tx.getHash());

    //enrich date
    dto.setBlockDate(
        ethBlockService
            .getTimestampSecForBlock(ethLog.getBlockNumber().longValue(), network));

    parseEvent(dto, tx.getMethodName());
    parseVault(dto, tx.getVault(), network);
    parseMintAmount(dto, tx.getMintAmount());
    updateInfo(dto, tx, network);
    log.info(dto.print());
    return dto;
  }

  private void parseEvent(ImportantEventsDTO dto, String methodName) {
    // ERC20 token Mint function emits Transfer event
    if ("Transfer".equals(methodName)) {
      dto.setEvent(TOKEN_MINTED);
    } else {
      dto.setEvent(methodName);
    }
  }

  private void parseVault(ImportantEventsDTO dto, String vault, String network) {
    dto.setVault(contractDbService
        .getNameByAddress(vault, network)
        .orElseThrow(() -> new IllegalStateException("Not found name for " + vault))
    );
  }

  private void parseMintAmount(ImportantEventsDTO dto, BigInteger mintAmount) {
    if (!mintAmount.equals(BigInteger.ZERO)) {
      dto.setMintAmount(mintAmount.doubleValue() / D18);
    }
  }

  private void updateInfo(ImportantEventsDTO dto, ImportantEventsTx tx, String network) {
    ImportantEventsInfo info = new ImportantEventsInfo();
    info.setVaultAddress(tx.getVault());

    if ("StrategyAnnounced".equals(dto.getEvent())) {
      info.setStrategyTimeLock(
          functionsUtils
              .callIntByName(STRATEGY_TIME_LOCK, tx.getVault(), tx.getBlock(), network)
              .orElse(BigInteger.ZERO).longValue());
      dto.setOldStrategy(
          functionsUtils.callAddressByName(STRATEGY, tx.getVault(), tx.getBlock(), network)
              .orElse(""));
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      dto.setInfo(mapper.writeValueAsString(info));
    } catch (JsonProcessingException e) {
      log.error("Error converting to json " + info, e);
    }

  }
}
