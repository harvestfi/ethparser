package pro.belbix.ethparser.web3.prices.parser;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.v0.PriceDTO;
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
public class PriceLogParser extends Web3Parser<PriceDTO, Log> {

  private final PriceDecoder priceDecoder = new PriceDecoder();
  private final Web3Subscriber web3Subscriber;
  private final EthBlockService ethBlockService;
  private final PriceDBService priceDBService;
  private final NetworkProperties networkProperties;
  private final FunctionsUtils functionsUtils;
  private final ContractDbService contractDbService;
  private final Map<String, PriceDTO> lastPrices = new HashMap<>();

  public PriceLogParser(
      Web3Subscriber web3Subscriber, EthBlockService ethBlockService,
      ParserInfo parserInfo,
      PriceDBService priceDBService,
      AppProperties appProperties,
      NetworkProperties networkProperties,
      FunctionsUtils functionsUtils,
      ContractDbService contractDbService) {
    super(parserInfo, appProperties);
    this.web3Subscriber = web3Subscriber;
    this.ethBlockService = ethBlockService;
    this.priceDBService = priceDBService;
    this.networkProperties = networkProperties;
    this.functionsUtils = functionsUtils;
    this.contractDbService = contractDbService;
  }

  @Override
  protected void subscribeToInput() {
    web3Subscriber.subscribeOnLogs(input, this.getClass().getSimpleName());
  }

  @Override
  protected boolean save(PriceDTO dto) {
    return priceDBService.savePriceDto(dto);
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network).isParsePrices();
  }

  // keep this parsing lightweight as more as possible
  @Override
  public PriceDTO parse(Log ethLog, String network) {
    if (!isValidLog(ethLog, network)) {
      return null;
    }
    PriceTx tx = priceDecoder.decode(ethLog);

    if (tx == null) {
      return null;
    }

    PriceDTO dto = new PriceDTO();
    dto.setOwner(tx.getAddresses()[0]);
    dto.setRecipient(tx.getAddresses()[1]);
    dto.setSourceAddress(tx.getSource());
    dto.setId(tx.getHash() + "_" + tx.getLogId());
    dto.setBlock(tx.getBlock().longValue());
    dto.setNetwork(network);

    boolean keyCoinFirst = checkAndFillCoins(tx, dto, network);

    if (!isValidSource(dto, network)) {
      return null;
    }

    String sourceName = getSourceName(tx.getSource(), network);

    Boolean buy = isBuy(tx, keyCoinFirst);
    if (buy == null) {
      log.error("Both amountOut values not zero, can't determinate swap direction {}", tx);
      return null;
    }
    dto.setSource(sourceName);
    dto.setBuy(buy ? 1 : 0);

    boolean successParseAmount =
        fillAmountsAndPrice(dto, tx, keyCoinFirst, buy, network);
    if (!successParseAmount) {
      return null;
    }

    if (appProperties.isSkipSimilarPrices() && skipSimilar(dto)) {
      return null;
    }

    fillLpStats(dto, network);

    dto.setBlockDate(
        ethBlockService.getTimestampSecForBlock(tx.getBlock().longValue(), network));
    log.info(dto.print());
    return dto;
  }

  private String getSourceName(String sourceAddress, String network) {
    Instant start = now();
    String name = contractDbService.getNameByAddress(sourceAddress, network)
        .orElseThrow(() -> new IllegalStateException("Not found name for " + sourceAddress));
    log.trace("Price name fetched {} {}", name, between(start, now()).toMillis());
    return name;
  }

  private boolean isValidLog(Log log, String network) {
    if (log == null || log.getTopics() == null || log.getTopics().isEmpty()) {
      return false;
    }

    return contractDbService.findTokenByPair(
        log.getAddress(), log.getBlockNumber().longValue(), network)
        .isPresent();
  }

  private void fillLpStats(PriceDTO dto, String network) {
    // reduce web3 calls
    if (!ContractUtils.isFullParsableLp(dto.getTokenAddress(), dto.getNetwork())) {
      return;
    }
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
    Instant start = now();
    boolean isSimilar;
    if (ContractUtils.isFullParsableLp(dto.getTokenAddress(), dto.getNetwork())) {
      isSimilar = false;
    } else {
      PriceDTO lastPrice = lastPrices.get(dto.getTokenAddress());
      if (lastPrice != null && lastPrice.getBlock().equals(dto.getBlock())) {
        log.debug("Skip similar price for {}", dto.getToken());
        isSimilar = true;
      } else {
        lastPrices.put(dto.getTokenAddress(), dto);
        isSimilar = false;
      }
    }
    log.trace("Price checked similar {}", between(start, now()).toMillis());
    return isSimilar;
  }

  private boolean isValidSource(PriceDTO dto, String network) {
    Instant start = now();
    boolean isValid;
    String tokenAddress = dto.getTokenAddress();
    String sourceAddress = dto.getSourceAddress();
    long block = dto.getBlock();

    var pair = contractDbService
        .findPairByToken(tokenAddress, block, network);
    if (pair.isEmpty()) {
      log.trace("{} doesn't have valid LP pair {} {}",
          tokenAddress, block, network);
      isValid = false;
    } else if (pair.filter(p -> p.getUniPair().getContract().getAddress()
        .equalsIgnoreCase(sourceAddress))
        .isPresent()) {
      isValid = true;
    } else {
      log.debug("{} price from not actual LP {}", tokenAddress, sourceAddress);
      isValid = false;
    }

    log.trace("Price validated {}", between(start, now()).toMillis());
    return isValid;
  }

  private boolean checkAndFillCoins(PriceTx tx, PriceDTO dto, String network) {
    Instant start = now();

    String lp = tx.getSource().toLowerCase();

    String keyCoinAddress = contractDbService
        .findKeyTokenViaLinkForLp(lp, dto.getBlock(), network)
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

    boolean keyCoinFirst;
    if (tokensAdr.component1().equals(keyCoinAddress)) {
      dto.setToken(tokensNames.component1());
      dto.setTokenAddress(tokensAdr.component1());
      dto.setOtherToken(tokensNames.component2());
      dto.setOtherTokenAddress(tokensAdr.component2());
      keyCoinFirst = true;
    } else if (tokensAdr.component2().equals(keyCoinAddress)) {
      dto.setToken(tokensNames.component2());
      dto.setTokenAddress(tokensAdr.component2());
      dto.setOtherToken(tokensNames.component1());
      dto.setOtherTokenAddress(tokensAdr.component1());
      keyCoinFirst = false;
    } else {
      throw new IllegalStateException(
          "Swap doesn't contains key coin " + keyCoinAddress + " " + tx);
    }

    log.trace("Price checked and filled {}", between(start, now()).toMillis());
    return keyCoinFirst;
  }

  private static Boolean isBuy(PriceTx tx, boolean keyCoinFirst) {
    if (keyCoinFirst) {
      if (isZero(tx, 3)) { // amount1Out
        return true;
      } else if (isZero(tx, 2)) { // amount0Out
        return false;
      } else {
        return null;
      }
    } else {
      if (isZero(tx, 2)) { // amount0Out
        return true;
      } else if (isZero(tx, 3)) { // amount1Out
        return false;
      } else {
        return null;
      }
    }
  }

  private boolean fillAmountsAndPrice(PriceDTO dto, PriceTx tx, boolean keyCoinFirst,
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
    if (dto.getTokenAmount() == 0.0 || dto.getOtherTokenAmount() == 0.0) {
      log.info("Zero amount in price DTO, skip {}", dto);
      return false;
    }

    dto.setPrice(dto.getOtherTokenAmount() / dto.getTokenAmount());
    return true;
  }

  private double parseAmountFromTx(PriceTx tx, int i, String address, String network) {
    return contractDbService.parseAmount(tx.getIntegers()[i], address, network);
  }

  private static boolean isZero(PriceTx tx, int i) {
    return BigInteger.ZERO.equals(tx.getIntegers()[i]);
  }
}
