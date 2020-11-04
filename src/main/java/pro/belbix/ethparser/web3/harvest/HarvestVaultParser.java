package pro.belbix.ethparser.web3.harvest;

import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.web3.harvest.HarvestFunctions.GET_PRICE_PER_FULL_SHARE;
import static pro.belbix.ethparser.web3.harvest.HarvestFunctions.GET_RESERVES;

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
        dto.setSharePrice(fetchLastSharePrice(harvestTx.getVault().getValue()));

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

    private double fetchLastSharePrice(String contractAddress) {
        List<Type> types = web3Service
            .callMethod(GET_PRICE_PER_FULL_SHARE, contractAddress, LATEST); //TODO archive data required for not LATEST
        if (types == null || types.isEmpty()) {
            return 1.0;
        }
        return HarvestTx.parseAmount((BigInteger) types.get(0).getValue(), contractAddress);
    }

    private void fillUsdPrice(HarvestDTO dto, String strategyHash) {
        if (Vaults.lpTokens.contains(dto.getVault())) {
            double lpUsdAmount = getUniLpUsdAmount(dto.getVault(), strategyHash);
            long usdAmount = (long) (lpUsdAmount * dto.getAmount());
            dto.setUsdAmount(usdAmount);
            dto.setTvlFactor(lpUsdAmount);
        } else {
            Double price = priceProvider.getPriceForCoin(dto.getVault());
            if (price == null) {
                throw new IllegalStateException("Unknown coin " + dto.getVault());
            }
            dto.setTvlFactor(price);
            dto.setUsdAmount((long) (price * dto.getAmount()));
        }
    }

    public double getUniLpUsdAmount(String vaultName, String strategyHash) {
        Tuple2<Double, Double> prices = priceProvider.getPriceForUniPair(vaultName);
        Tuple2<Double, Double> values = fetchUniBalance(strategyHash);
        if (values.component1() == 0 || values.component2() == 0) {
            throw new IllegalStateException("Wrong values for " + strategyHash);
        }
        Double value1 = prices.component1() * values.component1();
        Double value2 = prices.component2() * values.component2();
        return Math.round((value1 + value2));
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
        Tuple2<Long, Long> dividers = Vaults.coinDividers.get(contractName);
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

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    @PreDestroy
    public void stop() {
        run.set(false);
    }

}
