package pro.belbix.ethparser.web3.prices.parser;

import static pro.belbix.ethparser.web3.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;

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
import pro.belbix.ethparser.dto.PriceDTO;
import pro.belbix.ethparser.model.PriceTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.FunctionsUtils;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.LpContracts;
import pro.belbix.ethparser.web3.contracts.TokenInfo;
import pro.belbix.ethparser.web3.contracts.Tokens;
import pro.belbix.ethparser.web3.prices.db.PriceDBService;
import pro.belbix.ethparser.web3.prices.decoder.PriceDecoder;

@Service
@Log4j2
public class PriceLogParser implements Web3Parser {

    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final PriceDecoder priceDecoder = new PriceDecoder();
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final Web3Service web3Service;
    private final EthBlockService ethBlockService;
    private final ParserInfo parserInfo;
    private final PriceDBService priceDBService;
    private final AppProperties appProperties;
    private final FunctionsUtils functionsUtils;
    private Instant lastTx = Instant.now();
    private long count = 0;
    private final Map<String, PriceDTO> lastPrices = new HashMap<>();

    public PriceLogParser(Web3Service web3Service,
                          EthBlockService ethBlockService,
                          ParserInfo parserInfo,
                          PriceDBService priceDBService,
                          AppProperties appProperties,
                          FunctionsUtils functionsUtils) {
        this.web3Service = web3Service;
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
        web3Service.subscribeOnLogs(logs);
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
        String sourceName = LpContracts.findNameForLpHash(tx.getSource());
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

        dto.setBlockDate(ethBlockService.getTimestampSecForBlock(tx.getBlockHash(), tx.getBlock().longValue()));
        log.info(dto.print());
        return dto;
    }

    private void fillLpStats(PriceDTO dto) {
        String lpAddress = ContractUtils.getAddressByName(dto.getSource())
            .orElseThrow(() -> new IllegalStateException("Lp address not found for " + dto.getSource()));
        Tuple2<Double, Double> lpPooled = functionsUtils.callReserves(lpAddress, dto.getBlock());
        double lpBalance = parseAmount(
            functionsUtils.callIntByName(TOTAL_SUPPLY, lpAddress, dto.getBlock())
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
        TokenInfo tokenInfo = Tokens.getTokenInfo(dto.getToken());
        String currentLpName = tokenInfo.findLp(dto.getBlock()).component1();
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

        String keyCoinHash = LpContracts.keyCoinForLp.get(lp);
        if (keyCoinHash == null) {
            throw new IllegalStateException("LP key coin not found for " + lp);
        }
        String keyCoinName = Tokens.findNameForContract(keyCoinHash);
        Tuple2<String, String> pair = LpContracts.lpHashToCoinNames.get(lp);
        if (pair == null) {
            throw new IllegalStateException("Pair not found for " + lp);
        }

        if (pair.component1().equals(keyCoinName)) {
            dto.setToken(pair.component1());
            dto.setOtherToken(pair.component2());
            return true;
        } else if (pair.component2().equals(keyCoinName)) {
            dto.setToken(pair.component2());
            dto.setOtherToken(pair.component1());
            return false;
        } else {
            throw new IllegalStateException("Swap doesn't contains key coin " + keyCoinName + " " + tx);
        }
    }

    private static boolean isBuy(PriceTx tx, boolean keyCoinFirst) {
        if (keyCoinFirst) {
            if (isZero(tx, 0)) { // first coin "in" is zero, that means coin is "out"
                return true;
            } else if (isZero(tx, 2)) { // first coin "out" is zero, that means coin is "in"
                return false;
            } else {
                throw new IllegalStateException("Swap doesn't contains 0 zero value " + tx);
            }
        } else {
            if (isZero(tx, 1)) { // second coin "in" is zero, that means coin is "out"
                return true;
            } else if (isZero(tx, 3)) { // second coin "out" is zero, that means coin is "in"
                return false;
            } else {
                throw new IllegalStateException("Swap doesn't contains 1 zero value " + tx);
            }
        }
    }

    private static void fillAmountsAndPrice(PriceDTO dto, PriceTx tx, boolean keyCoinFirst, boolean buy) {
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
        return parseAmount(tx.getIntegers()[i], Tokens.findContractForName(name));
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
