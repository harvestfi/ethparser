package pro.belbix.ethparser.web3.prices.parser;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
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
import pro.belbix.ethparser.model.PriceTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Subscriber;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.db.PriceDBService;
import pro.belbix.ethparser.web3.prices.decoder.PriceDecoder;

@Service
@Log4j2
public class PriceLogParser implements Web3Parser {

  private static final ContractUtils contractUtils = ContractUtils.getInstance(ETH_NETWORK);
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final PriceDecoder priceDecoder = new PriceDecoder();
  private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final Web3Functions web3Functions;
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final ParserInfo parserInfo;
  private final PriceDBService priceDBService;
  private final AppProperties appProperties;
  private final FunctionsUtils functionsUtils;
  private Instant lastTx = Instant.now();
  private long count = 0;
  private final Map<String, PriceDTO> lastPrices = new HashMap<>();

  public PriceLogParser(Web3Functions web3Functions,
      Web3Subscriber web3Subscriber, EthBlockService ethBlockService,
      ParserInfo parserInfo,
      PriceDBService priceDBService,
      AppProperties appProperties,
      FunctionsUtils functionsUtils) {
    this.web3Functions = web3Functions;
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.parserInfo = parserInfo;
    this.priceDBService = priceDBService;
    this.appProperties = appProperties;
    this.functionsUtils = functionsUtils;
  }

  @Override
  public void startParse() {
    log.info("Start parse Price logs");
    parserInfo.addParser(this);
    web3Subscriber.subscribeOnLogs(logs);
    new Thread(() -> {
      while (run.get()) {
        Log ethLog = null;
        try {
          ethLog = logs.poll(1, TimeUnit.SECONDS);
          count++;
          if (count % 100 == 0) {
            log.info(this.getClass().getSimpleName() + " handled " + count);
          }
          PriceDTO dto = parse(ethLog);
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
  public PriceDTO parse(Log ethLog) {
    PriceTx tx = priceDecoder.decode(ethLog);

    if (tx == null) {
      return null;
    }
    String sourceName = contractUtils.getNameByAddress(tx.getSource())
        .orElseThrow(() -> new IllegalStateException("Not found name for " + tx.getSource()));
    PriceDTO dto = new PriceDTO();

    boolean keyCoinFirst = checkAndFillCoins(tx, dto);
    boolean buy = isBuy(tx, keyCoinFirst);
    dto.setSource(sourceName);
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock().longValue());
    dto.setBuy(buy ? 1 : 0);

    if (!isValidSource(dto)) {
      return null;
    }

    fillAmountsAndPrice(dto, tx, keyCoinFirst, buy);

    if (appProperties.isSkipSimilarPrices() && skipSimilar(dto)) {
      return null;
    }

    // for lpToken price we should know staked amounts
    fillLpStats(dto);

    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(tx.getBlock().longValue(), ETH_NETWORK));
    log.info(dto.print());
    return dto;
  }

  private void fillLpStats(PriceDTO dto) {
    String lpAddress = contractUtils.getAddressByName(dto.getSource(), ContractType.UNI_PAIR)
        .orElseThrow(
            () -> new IllegalStateException("Lp address not found for " + dto.getSource()));
    Tuple2<Double, Double> lpPooled = functionsUtils.callReserves(
        lpAddress, dto.getBlock(), ETH_NETWORK);
    double lpBalance = parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, lpAddress, dto.getBlock(), ETH_NETWORK)
            .orElseThrow(() -> new IllegalStateException("Error get supply from " + lpAddress)),
        lpAddress);
    dto.setLpTotalSupply(lpBalance);
    dto.setLpToken0Pooled(lpPooled.component1());
    dto.setLpToken1Pooled(lpPooled.component2());
  }

  private boolean skipSimilar(PriceDTO dto) {
    PriceDTO lastPrice = lastPrices.get(dto.getToken());
    if (lastPrice != null && lastPrice.getBlock().equals(dto.getBlock())) {
      return true;
    }
    lastPrices.put(dto.getToken(), dto);
    return false;
  }

  private boolean isValidSource(PriceDTO dto) {
    String currentLpName = contractUtils.findUniPairNameForTokenName(
        dto.getToken(), dto.getBlock()).orElse(null);
    if (currentLpName == null) {
      return false;
    }
    boolean result = currentLpName.equals(dto.getSource());
    if (result) {
      return true;
    }
    log.warn("{} price from not actual LP {}, should be {}",
        dto.getToken(), dto.getSource(), currentLpName);
    return false;
  }

  private static boolean checkAndFillCoins(PriceTx tx, PriceDTO dto) {
    String lp = tx.getSource().toLowerCase();

    String keyCoinHash = contractUtils.findKeyTokenForUniPair(lp)
        .orElseThrow(() -> new IllegalStateException("LP key coin not found for " + lp));
    String keyCoinName = contractUtils.getNameByAddress(keyCoinHash)
        .orElseThrow(() -> new IllegalStateException("Not found name for " + keyCoinHash));
    Tuple2<String, String> tokensAdr = contractUtils.tokenAddressesByUniPairAddress(lp);
    Tuple2<String, String> tokensNames = new Tuple2<>(
        contractUtils.getNameByAddress(tokensAdr.component1())
            .orElseThrow(() -> new IllegalStateException(
                "Not found token name for " + tokensAdr.component1())),
        contractUtils.getNameByAddress(tokensAdr.component2())
            .orElseThrow(() -> new IllegalStateException(
                "Not found token name for " + tokensAdr.component2()))
    );

    if (tokensNames.component1().equals(keyCoinName)) {
      dto.setToken(tokensNames.component1());
      dto.setOtherToken(tokensNames.component2());
      return true;
    } else if (tokensNames.component2().equals(keyCoinName)) {
      dto.setToken(tokensNames.component2());
      dto.setOtherToken(tokensNames.component1());
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

  private static void fillAmountsAndPrice(PriceDTO dto, PriceTx tx, boolean keyCoinFirst,
      boolean buy) {
    if (keyCoinFirst) {
      if (buy) {
        dto.setTokenAmount(parseAmountFromTx(tx, 2, dto.getToken()));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 1, dto.getOtherToken()));
      } else {
        dto.setTokenAmount(parseAmountFromTx(tx, 0, dto.getToken()));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 3, dto.getOtherToken()));
      }
    } else {
      if (buy) {
        dto.setTokenAmount(parseAmountFromTx(tx, 3, dto.getToken()));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 0, dto.getOtherToken()));
      } else {
        dto.setTokenAmount(parseAmountFromTx(tx, 1, dto.getToken()));
        dto.setOtherTokenAmount(parseAmountFromTx(tx, 2, dto.getOtherToken()));
      }
    }

    dto.setPrice(dto.getOtherTokenAmount() / dto.getTokenAmount());
  }

  private static double parseAmountFromTx(PriceTx tx, int i, String name) {
    return parseAmount(tx.getIntegers()[i],
        contractUtils.getAddressByName(name, ContractType.TOKEN)
            .orElseThrow(() -> new IllegalStateException("Not found adr for " + name))
    );
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
