package pro.belbix.ethparser.web3.harvest;

import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.model.HarvestTx.parseAmount;
import static pro.belbix.ethparser.web3.harvest.HarvestFunctions.ERC_20_TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.harvest.HarvestFunctions.GET_PRICE_PER_FULL_SHARE;
import static pro.belbix.ethparser.web3.harvest.HarvestFunctions.GET_RESERVES;
import static pro.belbix.ethparser.web3.harvest.HarvestFunctions.UNDERLYING_UNIT;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.PriceProvider;
import pro.belbix.ethparser.model.DtoI;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.uniswap.LpContracts;

@Service
public class HarvestVaultParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(HarvestVaultParser.class);
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private static final Set<String> allowedMethods = new HashSet<>(Arrays.asList("withdraw", "deposit"));
    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();
    private final Web3Service web3Service;
    private long parsedTxCount = 0;
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(10_000);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(10_000);
    private final HarvestDBService harvestDBService;
    private final EthBlockService ethBlockService;
    private PriceProvider priceProvider = new PriceProvider();

    public HarvestVaultParser(Web3Service web3Service,
                              HarvestDBService harvestDBService,
                              EthBlockService ethBlockService) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.ethBlockService = ethBlockService;
    }

    @Override
    public void startParse() {
        log.info("Start parse Harvest vaults logs");
        web3Service.subscribeOnLogs(logs);
        new Thread(() -> {
            while (run.get()) {
                Log ethLog = null;
                try {
                    ethLog = logs.poll(1, TimeUnit.SECONDS);
                    HarvestDTO dto = parseVaultLog(ethLog);
                    if (dto != null) {
                        enrichDto(dto);
                        boolean success = harvestDBService.saveHarvestDTO(dto);
                        if (success) {
                            output.put(dto);
                        }
                    }
                } catch (Exception e) {
                    log.error("Can't save " + ethLog, e);
                }
            }
        }).start();
    }

    public HarvestDTO parseVaultLog(Log ethLog) {
        HarvestTx harvestTx;
        try {
            harvestTx = harvestVaultLogDecoder.decode(ethLog);
        } catch (Exception e) {
            log.error("Error decode " + ethLog, e);
            return null;
        }
        if (harvestTx == null) {
            return null;
        }

        if (!allowedMethods.contains(harvestTx.getMethodName().toLowerCase())) {
            return null;
        }
        TransactionReceipt receipt = web3Service.fetchTransactionReceipt(harvestTx.getHash());
        replaceInputCoinValueOnFarmWrap(harvestTx, receipt);

        HarvestDTO dto = harvestTx.toDto();

        //enrich date
        dto.setBlockDate(ethBlockService.getTimestampSecForBlock(harvestTx.getBlockHash()));

        //enrich owner
        dto.setOwner(receipt.getFrom());

        //share price
        dto.setSharePrice(
            parseAmount(
                fetchLastSharePrice(harvestTx.getVault().getValue())
                , harvestTx.getVault().getValue())
        );

        fillUsdPrice(dto, harvestTx.getVault().getValue());

        log.info(dto.print());
        return dto;
    }

    private void replaceInputCoinValueOnFarmWrap(HarvestTx harvestTx, TransactionReceipt receipt) {
        harvestTx.setAmountIn(harvestTx.getAmount());

        List<Log> logs = receipt.getLogs();

        for (Log ethLog : logs) {
            HarvestTx logTx = harvestVaultLogDecoder.decode(ethLog);
            if (logTx == null) {
                continue;
            }
            if (!"Transfer".equals(logTx.getMethodName())) {
                continue;
            }

            if (ZERO_ADDRESS.equals(logTx.getAddressFromArgs1().getValue())
                && harvestTx.getMethodName().toLowerCase().equals("deposit")) {
                harvestTx.setAmount(logTx.getAmount());
                harvestTx.setfToken(new Address(ethLog.getAddress()));
                return;
            } else if (ZERO_ADDRESS.equals(logTx.getAddressFromArgs2().getValue())
                && harvestTx.getMethodName().toLowerCase().equals("withdraw")) {
                harvestTx.setAmount(logTx.getAmount());
                harvestTx.setfToken(new Address(ethLog.getAddress()));
                return;
            }
        }
        throw new IllegalStateException("Not found transfer value");
    }

    public void enrichDto(HarvestDTO dto) {
        dto.setLastGas(web3Service.fetchAverageGasPrice());
    }

    private BigInteger fetchLastSharePrice(String contractAddress) {
        List<Type> types = web3Service
            .callMethod(GET_PRICE_PER_FULL_SHARE, contractAddress, LATEST); //TODO archive data required for not LATEST
        if (types == null || types.isEmpty()) {
            return BigInteger.ONE;
        }
        return (BigInteger) types.get(0).getValue();
    }

    private void fillUsdPrice(HarvestDTO dto, String strategyHash) {
        if (Vaults.lpTokens.contains(dto.getVault())) {
            fillUsdValuesForLP(dto, strategyHash);
        } else {
            Double price = priceProvider.getPriceForCoin(dto.getVault());
            if (price == null) {
                throw new IllegalStateException("Unknown coin " + dto.getVault());
            }
            dto.setUsdAmount((long) (price * dto.getAmount()));
        }
    }

    private Tuple2<Double, Double> fetchUniBalance(String contractAddress) {
        List<Type> types = web3Service
            .callMethod(GET_RESERVES, LpContracts.harvestStrategyToLp.get(contractAddress),
                LATEST); //TODO archive data required for not LATEST
        if (types == null || types.size() < 3) {
            log.error("Wrong values for " + contractAddress);
            return new Tuple2<>(0.0, 0.0);
        }
        String contractName = Vaults.vaultNames.get(contractAddress);
        if (contractName == null) {
            throw new IllegalStateException("Not found name for " + contractAddress);
        }
        Tuple2<Long, Long> dividers = Vaults.lpDividers.get(contractName);
        if (dividers == null) {
            throw new IllegalStateException("Not found divider for " + contractName);
        }
        double v1 = ((BigInteger) types.get(0).getValue()).doubleValue();
        double v2 = ((BigInteger) types.get(1).getValue()).doubleValue();
        return new Tuple2<>(
            v1 / dividers.component1(),
            v2 / dividers.component2()
        );
    }

    private void fillUsdValuesForLP(HarvestDTO dto, String vaultHash) {
        String lpHash = LpContracts.harvestStrategyToLp.get(vaultHash);
        double vaultBalance = parseAmount(callErc20TotalSupply(vaultHash), vaultHash);
        double sharedPrice = parseAmount(fetchLastSharePrice(vaultHash), vaultHash);
        double vaultUnderlyingUnit = parseAmount(callUnderlyingUnit(vaultHash), vaultHash);
        double lpBalance = parseAmount(callErc20TotalSupply(lpHash), lpHash);
        Tuple2<Double, Double> lpUnderlyingBalances = fetchUniBalance(vaultHash);

        double vaultSharedBalance = (vaultBalance * sharedPrice);
        double vaultFraction = (vaultSharedBalance / vaultUnderlyingUnit) / lpBalance;

        Tuple2<Double, Double> uniPrices = priceProvider.getPriceForUniPair(vaultHash);

        Long firstVault = Math.round(vaultFraction * lpUnderlyingBalances.component1() * uniPrices.component1());
        Long secondVault = Math.round(vaultFraction * lpUnderlyingBalances.component2() * uniPrices.component2());
        long vaultUsdAmount = firstVault + secondVault;
        dto.setLastUsdTvl((double) vaultUsdAmount);

        double txFraction = (dto.getAmount() / vaultBalance);
        long txUsdAmount = Math.round(vaultUsdAmount * txFraction);
        dto.setUsdAmount(txUsdAmount);
    }

    private BigInteger callErc20TotalSupply(String hash) {
        List<Type> types = web3Service.callMethod(ERC_20_TOTAL_SUPPLY, hash, LATEST);
        if (types == null || types.isEmpty()) {
            log.error("Wrong total supply for " + hash);
            return BigInteger.ZERO;
        }
        return (BigInteger) types.get(0).getValue();
    }

    private BigInteger callUnderlyingUnit(String hash) {
        List<Type> types = web3Service.callMethod(UNDERLYING_UNIT, hash, LATEST);
        if (types == null || types.isEmpty()) {
            log.error("Wrong underlying unit for " + hash);
            return BigInteger.ZERO;
        }
        return (BigInteger) types.get(0).getValue();
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    @PreDestroy
    public void stop() {
        run.set(false);
    }

}
