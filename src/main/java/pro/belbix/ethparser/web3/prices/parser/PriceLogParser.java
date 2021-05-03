package pro.belbix.ethparser.web3.prices.parser;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.model.tx.PriceTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.prices.db.PriceDBService;
import pro.belbix.ethparser.web3.prices.decoder.PriceDecoder;

@Service
@Log4j2
public class PriceLogParser implements Web3Parser {

  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final PriceDecoder priceDecoder = new PriceDecoder();
  private final BlockingQueue<Web3Model<Log>> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final ParserInfo parserInfo;
  private final PriceDBService priceDBService;
  private final AppProperties appProperties;
  private final NetworkProperties networkProperties;
  private final FunctionsUtils functionsUtils;
  private final ContractDbService contractDbService;
  private Instant lastTx = Instant.now();
  private long count = 0;
  private final Map<String, PriceDTO> lastPrices = new HashMap<>();

  public PriceLogParser(
      Web3Subscriber web3Subscriber, EthBlockService ethBlockService,
      ParserInfo parserInfo,
      PriceDBService priceDBService,
      AppProperties appProperties,
      NetworkProperties networkProperties,
      FunctionsUtils functionsUtils,
      ContractDbService contractDbService) {
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.parserInfo = parserInfo;
    this.priceDBService = priceDBService;
    this.appProperties = appProperties;
    this.networkProperties = networkProperties;
    this.functionsUtils = functionsUtils;
    this.contractDbService = contractDbService;
  }

  @Override
  public void startParse() {
    log.info("Start parse Price logs");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnLogs(logs);
    new Thread(() -> {
      while (run.get()) {
        Web3Model<Log> ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          count++;
          if (count % 100 == 0) {
            log.info(this.getClass().getSimpleName() + " handled " + count);
          }
          if (ethLog == null
              || !networkProperties.get(ethLog.getNetwork())
              .isParsePrices()) {
            continue;
          }
          PriceDTO dto = parse(ethLog.getValue(), ethLog.getNetwork());
          if (dto != null) {
            lastTx = Instant.now();
            boolean success = priceDBService.savePriceDto(dto);
            if (success) {
              output.put(dto);
            }
          }
        } catch (Exception e) {
          log.error("Error price parser loop " + ethLog, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  // keep this parsing lightweight as more as possible
  public PriceDTO parse(Log ethLog, String network) {
    if (!isValidLog(ethLog, network)) {
      return null;
    }
    PriceTx tx = priceDecoder.decode(ethLog);

    if (tx == null) {
      return null;
    }
    String sourceName = contractDbService.getNameByAddress(tx.getSource(), network)
        .orElseThrow(() -> new IllegalStateException("Not found name for " + tx.getSource()));
    PriceDTO dto = new PriceDTO();

    boolean keyCoinFirst = checkAndFillCoins(tx, dto, network);
    boolean buy = isBuy(tx, keyCoinFirst);
    dto.setSource(sourceName);
    dto.setSourceAddress(tx.getSource());
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock().longValue());
    dto.setBuy(buy ? 1 : 0);
    dto.setNetwork(network);

    if (!isValidSource(dto, network)) {
      return null;
    }

    fillAmountsAndPrice(dto, tx, keyCoinFirst, buy, network);

    if (appProperties.isSkipSimilarPrices() && skipSimilar(dto)) {
      return null;
    }

    // for lpToken price we should know staked amounts
    fillLpStats(dto, network);

    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(tx.getBlock().longValue(), network));
    log.info(dto.print());
    return dto;
  }

  private boolean isValidLog(Log log, String network) {
    if (log == null || log.getTopics() == null || log.getTopics().isEmpty()) {
      return false;
    }

    return contractDbService.findLpByAddress(log.getAddress(), network)
        .filter(u -> u.getKeyToken() != null)
        .isPresent();
  }

  private void fillLpStats(PriceDTO dto, String network) {
    Tuple2<Double, Double> lpPooled = functionsUtils.callReserves(
        dto.getSourceAddress(), dto.getBlock(), network);
    double lpBalance = contractDbService.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, dto.getSourceAddress(), dto.getBlock(), network)
            .orElseThrow(() -> new IllegalStateException(
                "Error get supply from " + dto.getSourceAddress())),
        dto.getSourceAddress(), network);
    dto.setLpTotalSupply(lpBalance);
    dto.setLpToken0Pooled(lpPooled.component1());
    dto.setLpToken1Pooled(lpPooled.component2());
  }

  private boolean skipSimilar(PriceDTO dto) {
    if (ContractUtils.isParsableLp(dto.getTokenAddress(), dto.getNetwork())) {
      return true;
    }
    PriceDTO lastPrice = lastPrices.get(dto.getToken());
    if (lastPrice != null && lastPrice.getBlock().equals(dto.getBlock())) {
      return true;
    }
    lastPrices.put(dto.getToken(), dto);
    return false;
  }

  private boolean isValidSource(PriceDTO dto, String network) {
    var pair = contractDbService
        .findPairByToken(dto.getTokenAddress(), dto.getBlock(), network);
    if (pair.isEmpty()) {
      return false;
    }

    if (pair.filter(p -> p.getUniPair().getContract().getAddress()
        .equalsIgnoreCase(dto.getSourceAddress()))
        .isPresent()) {
      return true;
    }
    log.warn("{} price from not actual LP {}", dto.getToken(), dto.getSource());
    return false;
  }

  private boolean checkAndFillCoins(PriceTx tx, PriceDTO dto, String network) {
    String lp = tx.getSource().toLowerCase();

    String keyCoinName = contractDbService.findLpByAddress(lp, network)
        .map(UniPairEntity::getKeyToken)
        .map(TokenEntity::getContract)
        .map(ContractEntity::getName)
        .orElse("");

    Tuple2<String, String> tokensAdr = contractDbService
        .tokenAddressesByUniPairAddress(lp, network);
    Tuple2<String, String> tokensNames = new Tuple2<>(
        contractDbService.getNameByAddress(tokensAdr.component1(), network)
            .orElseThrow(() -> new IllegalStateException(
                "Not found token name for " + tokensAdr.component1())),
        contractDbService.getNameByAddress(tokensAdr.component2(), network)
            .orElseThrow(() -> new IllegalStateException(
                "Not found token name for " + tokensAdr.component2()))
    );

    if (tokensNames.component1().equals(keyCoinName)) {
      dto.setToken(tokensNames.component1());
      dto.setTokenAddress(tokensAdr.component1());
      dto.setOtherToken(tokensNames.component2());
      dto.setOtherTokenAddress(tokensAdr.component2());
      return true;
    } else if (tokensNames.component2().equals(keyCoinName)) {
      dto.setToken(tokensNames.component2());
      dto.setTokenAddress(tokensAdr.component2());
      dto.setOtherToken(tokensNames.component1());
      dto.setOtherTokenAddress(tokensAdr.component1());
      return false;
    } else {
      throw new IllegalStateException("Swap doesn't contains key coin " + keyCoinName + " " + tx);
    }
  }

  private static boolean isBuy(PriceTx tx, boolean keyCoinFirst) {
    if (keyCoinFirst) {
      if (isZero(tx, 3)) { // amount1Out
        return true;
      } else if (isZero(tx, 2)) { // amount0Out
        return false;
      } else {
        throw new IllegalStateException("Swap doesn't contains zero value " + tx);
      }
    } else {
      if (isZero(tx, 2)) { // amount0Out
        return true;
      } else if (isZero(tx, 3)) { // amount1Out
        return false;
      } else {
        throw new IllegalStateException("Swap doesn't contains zero value " + tx);
      }
    }
  }

  private void fillAmountsAndPrice(PriceDTO dto, PriceTx tx, boolean keyCoinFirst,
      boolean buy, String network) {
    if (keyCoinFirst) {
      if (buy) {
        dto.setTokenAmount(parseAmountFromTx(tx, 2, dto.getTokenAddress(), network));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 1, dto.getOtherTokenAddress(), network));
      } else {
        dto.setTokenAmount(parseAmountFromTx(tx, 0, dto.getTokenAddress(), network));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 3, dto.getOtherTokenAddress(), network));
      }
    } else {
      if (buy) {
        dto.setTokenAmount(parseAmountFromTx(tx, 3, dto.getTokenAddress(), network));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 0, dto.getOtherTokenAddress(), network));
      } else {
        dto.setTokenAmount(parseAmountFromTx(tx, 1, dto.getTokenAddress(), network));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 2, dto.getOtherTokenAddress(), network));
      }
    }

    dto.setPrice(dto.getOtherTokenAmount() / dto.getTokenAmount());
  }

  private double parseAmountFromTx(PriceTx tx, int i, String address, String network) {
    return contractDbService.parseAmount(tx.getIntegers()[i], address, network);
  }

  private static boolean isZero(PriceTx tx, int i) {
    return BigInteger.ZERO.equals(tx.getIntegers()[i]);
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
