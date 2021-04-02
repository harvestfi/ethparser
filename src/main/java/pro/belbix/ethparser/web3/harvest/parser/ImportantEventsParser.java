package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.STRATEGY;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.STRATEGY_TIME_LOCK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.ImportantEventsDTO;
import pro.belbix.ethparser.model.ImportantEventsInfo;
import pro.belbix.ethparser.model.ImportantEventsTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.db.ImportantEventsDbService;
import pro.belbix.ethparser.web3.harvest.decoder.ImportantEventsLogDecoder;

@Service
@Log4j2
public class ImportantEventsParser implements Web3Parser {
  private final ContractUtils contractUtils = new ContractUtils(ETH_NETWORK);
  public static final String TOKEN_MINTED = "TokenMinted";
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final ImportantEventsLogDecoder importantEventsLogDecoder = new ImportantEventsLogDecoder();
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final ImportantEventsDbService importantEventsDbService;
  private final ParserInfo parserInfo;
  private final EthBlockService ethBlockService;
  private final FunctionsUtils functionsUtils;
  private final AppProperties appProperties;
  private Instant lastTx = Instant.now();

  public ImportantEventsParser(
      Web3Functions web3Functions,
      Web3Subscriber web3Subscriber,
      ImportantEventsDbService importantEventsDbService,
      ParserInfo parserInfo,
      EthBlockService ethBlockService,
      FunctionsUtils functionsUtils, AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.importantEventsDbService = importantEventsDbService;
    this.parserInfo = parserInfo;
    this.ethBlockService = ethBlockService;
    this.functionsUtils = functionsUtils;
    this.appProperties = appProperties;
  }

  @Override
  public void startParse() {
    log.info("Start parse Important Events logs");
    web3Subscriber.subscribeOnLogs(logs);
    parserInfo.addParser(this);
    new Thread(() -> {
      while (run.get()) {
        Log ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          ImportantEventsDTO dto = parseLog(ethLog);
          if (dto != null) {
            lastTx = Instant.now();
            boolean saved = importantEventsDbService.save(dto);
            if (saved) {
              output.put(dto);
            }
          }
        } catch (Exception e) {
          log.error("Can't save " + ethLog, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  public ImportantEventsDTO parseLog(Log ethLog) {
    if (ethLog == null ||
        (!ContractConstants.FARM_TOKEN.equals(ethLog.getAddress())
            && contractUtils.getNameByAddress(ethLog.getAddress()).isEmpty())
    ) {
      return null;
    }

    ImportantEventsTx tx = importantEventsLogDecoder.decode(ethLog);
    if (tx == null) {
      return null;
    }

    ImportantEventsDTO dto = new ImportantEventsDTO();
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock());
    dto.setOldStrategy(tx.getOldStrategy());
    dto.setNewStrategy(tx.getNewStrategy());
    dto.setHash(tx.getHash());

    //enrich date
    dto.setBlockDate(
        ethBlockService
            .getTimestampSecForBlock(ethLog.getBlockNumber().longValue()));

    parseEvent(dto, tx.getMethodName());
    parseVault(dto, tx.getVault());
    parseMintAmount(dto, tx.getMintAmount());
    updateInfo(dto, tx);
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

  private void parseVault(ImportantEventsDTO dto, String vault) {
    dto.setVault(
        contractUtils.getNameByAddress(vault)
            .orElseThrow(() -> new IllegalStateException("Not found name for " + vault))
    );
  }

  private void parseMintAmount(ImportantEventsDTO dto, BigInteger mintAmount) {
    if (!mintAmount.equals(BigInteger.ZERO)) {
      dto.setMintAmount(mintAmount.doubleValue() / D18);
    }
  }

  private void updateInfo(ImportantEventsDTO dto, ImportantEventsTx tx) {
    ImportantEventsInfo info = new ImportantEventsInfo();
    info.setVaultAddress(tx.getVault());

    if ("StrategyAnnounced".equals(dto.getEvent())) {
      info.setStrategyTimeLock(
          functionsUtils.callIntByName(STRATEGY_TIME_LOCK, tx.getVault(), tx.getBlock())
              .orElse(BigInteger.ZERO).longValue());
      dto.setOldStrategy(
          functionsUtils.callAddressByName(STRATEGY, tx.getVault(), tx.getBlock()).orElse(""));
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      dto.setInfo(mapper.writeValueAsString(info));
    } catch (JsonProcessingException e) {
      log.error("Error converting to json " + info, e);
    }

  }

  @Override
  public BlockingQueue<DtoI> getOutput() {
    return output;
  }

  @Override
  public Instant getLastTx() {
    return lastTx;
  }
}
