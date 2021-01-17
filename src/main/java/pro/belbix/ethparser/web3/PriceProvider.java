package pro.belbix.ethparser.web3;

import static java.util.Objects.requireNonNullElse;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.erc20.Tokens.BAC_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.BAS_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.BSGS_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.BSG_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.DPI_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.DSD_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.ESD_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.GRAIN_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.MIC_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.MIS_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.WBTC_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.WETH_NAME;
import static pro.belbix.ethparser.web3.erc20.Tokens.isStableCoin;
import static pro.belbix.ethparser.web3.erc20.Tokens.simplifyName;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.isDivisionSequenceSecondDividesFirst;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.model.PricesModel;
import pro.belbix.ethparser.web3.erc20.TokenInfo;
import pro.belbix.ethparser.web3.erc20.Tokens;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
public class PriceProvider {

    private static final Logger log = LoggerFactory.getLogger(PriceProvider.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private long updateBlockDifference = 5;

    private final Map<String, TreeMap<Long, Double>> lastPrices = new HashMap<>();

    private final Functions functions;

    public PriceProvider(Functions functions) {
        this.functions = functions;
    }

    public void setUpdateBlockDifference(long updateBlockDifference) {
        this.updateBlockDifference = updateBlockDifference;
    }

    public String getAllPrices(long block) throws JsonProcessingException {
        //todo refactoring price model
        PricesModel dto = new PricesModel();
        dto.setBtc(getPriceForCoin(WBTC_NAME, block));
        dto.setEth(getPriceForCoin(WETH_NAME, block));
        dto.setDpi(getPriceForCoin(DPI_NAME, block));
        dto.setGrain(getPriceForCoin(GRAIN_NAME, block));
        dto.setBac(getPriceForCoin(BAC_NAME, block));
        dto.setBas(getPriceForCoin(BAS_NAME, block));
        dto.setMic(getPriceForCoin(MIC_NAME, block));
        dto.setMis(getPriceForCoin(MIS_NAME, block));
        dto.setBsg(getPriceForCoin(BSG_NAME, block));
        dto.setBsgs(getPriceForCoin(BSGS_NAME, block));
        dto.setEsd(getPriceForCoin(ESD_NAME, block));
        dto.setDsd(getPriceForCoin(DSD_NAME, block));
        return objectMapper.writeValueAsString(dto);
    }

    public double getLpPositionAmountInUsd(String lpAddress, double amount, long block) {
        Tuple2<String, String> names = LpContracts.lpHashToCoinNames.get(lpAddress);
        if (names == null) {
            throw new IllegalStateException("Not found names for " + lpAddress);
        }
        Tuple2<Double, Double> usdPrices = new Tuple2<>(
            getPriceForCoin(names.component1(), block),
            getPriceForCoin(names.component2(), block)
        );
        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpAddress, block);
        if (lpUnderlyingBalances == null) {
            throw new IllegalStateException("Can't reach reserves for " + lpAddress);
        }
        double lpBalance = parseAmount(functions.callErc20TotalSupply(lpAddress, block), lpAddress);
        double positionFraction = amount / lpBalance;

        double firstCoin = positionFraction * lpUnderlyingBalances.component1();
        double secondCoin = positionFraction * lpUnderlyingBalances.component2();

        double firstVaultUsdAmount = firstCoin * usdPrices.component1();
        double secondVaultUsdAmount = secondCoin * usdPrices.component2();
        return firstVaultUsdAmount + secondVaultUsdAmount;
    }

    public Double getPriceForCoin(String coinName, long block) {
        String coinNameSimple = simplifyName(coinName);
        updateUSDPrice(coinNameSimple, block);
        if (isStableCoin(coinNameSimple)) {
            return 1.0;
        }
        return getLastPrice(coinNameSimple, block);
    }

    public Tuple2<Double, Double> getPairPriceForStrategyHash(String strategyHash, Long block) {
        return getPairPriceForLpHash(Vaults.underlyingToken.get(strategyHash), block);
    }

    public Tuple2<Double, Double> getPairPriceForLpHash(String lpHash, Long block) {
        Tuple2<String, String> names = LpContracts.lpHashToCoinNames.get(lpHash);
        if (names == null) {
            throw new IllegalStateException("Not found names for " + lpHash);
        }
        return new Tuple2<>(
            getPriceForCoin(names.component1(), block),
            getPriceForCoin(names.component2(), block)
        );
    }

    private void updateUSDPrice(String coinName, long block) {
        if (!Tokens.isCreated(coinName, (int) block)) {
            savePrice(0.0, coinName, block);
            return;
        }
        if (isStableCoin(coinName)) {
            // todo parse stablecoin prices
            return;
        }

        if (hasFreshPrice(coinName, block)) {
            return;
        }

        double price = getPriceForCoinWithoutCache(coinName, block);

        savePrice(price, coinName, block);
        log.info("Price {} updated {} on block {}", coinName, price, block);
    }

    private double getPriceForCoinWithoutCache(String name, Long block) {
        TokenInfo tokenInfo = Tokens.getTokenInfo(name);
        String lpName = tokenInfo.findLp(block).component1();
        String otherTokenName = tokenInfo.findLp(block).component2();
        String lpHash = LpContracts.lpNameToHash.get(lpName);
        if (lpHash == null) {
            throw new IllegalStateException("Not found hash for " + tokenInfo);
        }

        Tuple2<Double, Double> reserves = functions.callReserves(lpHash, block);
        if (reserves == null) {
            throw new IllegalStateException("Can't reach reserves for " + tokenInfo);
        }
        double price;
        if (isDivisionSequenceSecondDividesFirst(lpHash)) {
            price = reserves.component2() / reserves.component1();
        } else {
            price = reserves.component1() / reserves.component2();
        }

        price *= getPriceForCoin(otherTokenName, block);
        return price;
    }

    public static double readPrice(PricesModel pricesModel, String coinName) {
        coinName = simplifyName(coinName);
        if (isStableCoin(coinName)) {
            return 1.0;
        }
        //todo refactoring price model
        switch (coinName) {
            case WBTC_NAME:
                return pricesModel.getBtc();
            case WETH_NAME:
                return pricesModel.getEth();
            case DPI_NAME:
                return pricesModel.getDpi();
            case GRAIN_NAME:
                return pricesModel.getGrain();
            case BAC_NAME:
                return pricesModel.getBac();
            case BAS_NAME:
                return pricesModel.getBas();
            case MIC_NAME:
                return pricesModel.getMic();
            case MIS_NAME:
                return pricesModel.getMis();
            case BSG_NAME:
                return pricesModel.getBsg();
            case BSGS_NAME:
                return pricesModel.getBsgs();
            case ESD_NAME:
                return pricesModel.getEsd();
            case DSD_NAME:
                return pricesModel.getDsd();
            default:
                log.warn("Not found price for {}", coinName);
                return 0;
        }
    }

    private boolean hasFreshPrice(String name, long block) {
        TreeMap<Long, Double> lastPriceByBlock = lastPrices.get(name);
        if (lastPriceByBlock == null) {
            return false;
        }

        Entry<Long, Double> entry = lastPriceByBlock.floorEntry(block);
        if (entry == null || Math.abs(entry.getKey() - block) >= updateBlockDifference) {
            return false;
        }
        return entry.getValue() != null && entry.getValue() != 0;
    }

    private void savePrice(double price, String name, long block) {
        TreeMap<Long, Double> lastPriceByBlock = lastPrices.computeIfAbsent(name, k -> new TreeMap<>());
        lastPriceByBlock.put(block, price);
    }

    private double getLastPrice(String name, long block) {
        TreeMap<Long, Double> lastPriceByBlocks = lastPrices.get(name);
        if (lastPriceByBlocks == null) {
            return 0.0;
        }
        Entry<Long, Double> entry = lastPriceByBlocks.floorEntry(requireNonNullElse(block, 0L));
        if (entry != null && entry.getValue() != null) {
            return entry.getValue();
        }
        return 0.0;
    }

}
