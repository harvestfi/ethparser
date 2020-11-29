package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.model.HarvestTx.parseAmount;
import static pro.belbix.ethparser.web3.harvest.PriceStubSender.PRICE_STUB_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;
import pro.belbix.ethparser.web3.uniswap.LpContracts;

@Service
@Deprecated
public class HarvestVaultParser implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(HarvestVaultParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private static final Set<String> allowedMethods = new HashSet<>(Arrays.asList("withdraw", "deposit"));
    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();
    private final Web3Service web3Service;
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final HarvestDBService harvestDBService;
    private final EthBlockService ethBlockService;
    private final PriceProvider priceProvider;
    private final Functions functions;

    public HarvestVaultParser(Web3Service web3Service,
                              HarvestDBService harvestDBService,
                              EthBlockService ethBlockService, PriceProvider priceProvider, Functions functions) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.ethBlockService = ethBlockService;
        this.priceProvider = priceProvider;
        this.functions = functions;
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
                        boolean success = true;
                        if (!PRICE_STUB_TYPE.equals(dto.getMethodName())) {
                            success = harvestDBService.saveHarvestDTO(dto);
                        } else {
                            log.info("Last prices send " + dto.getPrices() + " " + dto.getLastGas());
                        }
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
        if (ethLog == null) {
            return null;
        }
        if (PRICE_STUB_TYPE.equals(ethLog.getType())) {
            return createStubPriceDto();
        }
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
        dto.setBlockDate(ethBlockService.getTimestampSecForBlock(harvestTx.getBlockHash(), ethLog.getBlockNumber().longValue()));

        //enrich owner
        dto.setOwner(receipt.getFrom());

        //share price
        dto.setSharePrice(
            parseAmount(
                functions.callPricePerFullShare(harvestTx.getVault().getValue(), dto.getBlock().longValue())
                , harvestTx.getVault().getValue())
        );

        fillUsdPrice(dto, harvestTx.getVault().getValue());

        log.info(dto.print());
        return dto;
    }

    private HarvestDTO createStubPriceDto() {
        HarvestDTO dto = new HarvestDTO();
        dto.setBlockDate(Instant.now().getEpochSecond());
        dto.setBlock(web3Service.fetchCurrentBlock());
        dto.setMethodName(PRICE_STUB_TYPE);
        return dto;
    }

    /*
     * TODO This method totally unclear, but I don't know how to get fAmount without parsing logs
     */
    private void replaceInputCoinValueOnFarmWrap(HarvestTx harvestTx, TransactionReceipt receipt) {
        harvestTx.setAmountIn(harvestTx.getAmount());

        List<Log> logs = receipt.getLogs();
        for (Log ethLog : logs) {
            HarvestTx logTx = harvestVaultLogDecoder.decode(ethLog);
            if (logTx == null) {
                continue;
            }
            long logPlace = harvestTx.getLogId() - logTx.getLogId();
            if ((logPlace > 3 && harvestTx.getMethodName().toLowerCase().equals("deposit"))
//                || (logPlace > 11 && harvestTx.getMethodName().toLowerCase().equals("withdraw")) //no limit for withdraw
                || logPlace <= 0) {
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
        throw new IllegalStateException("Not found transfer value " + harvestTx.getHash());
    }

    /**
     * Separate method for avoid any unnecessary enrichment for other methods
     */
    public void enrichDto(HarvestDTO dto) {
        //set gas
        dto.setLastGas(web3Service.fetchAverageGasPrice());

        //write all prices
        try {
            dto.setPrices(priceProvider.getAllPrices(dto.getBlock().longValue()));
        } catch (Exception e) {
            log.error("Error get prices", e);
        }
    }

    private void fillUsdPrice(HarvestDTO dto, String strategyHash) {
        if (Vaults.lpTokens.contains(dto.getVault())) {
            fillUsdValuesForLP(dto, strategyHash);
        } else {
            fillUsdValues(dto, strategyHash);
        }
    }

    private void fillUsdValues(HarvestDTO dto, String vaultHash) {
        Double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock().longValue());
        if (price == null) {
            throw new IllegalStateException("Unknown coin " + dto.getVault());
        }

        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock().longValue()),
            vaultHash);
        double sharedPrice = parseAmount(functions.callPricePerFullShare(vaultHash, dto.getBlock().longValue()),
            vaultHash);
        double vaultUnderlyingUnit = parseAmount(functions.callUnderlyingUnit(vaultHash, dto.getBlock().longValue()),
            vaultHash);

        double vault = (vaultBalance * sharedPrice) / vaultUnderlyingUnit;
        dto.setLastTvl(vault);
        dto.setLastUsdTvl((double) Math.round(vault * price));
        dto.setUsdAmount((long) (price * dto.getAmount() * dto.getSharePrice()));
    }

    private void fillUsdValuesForLP(HarvestDTO dto, String vaultHash) {
        String lpHash = LpContracts.harvestStrategyToLp.get(vaultHash);
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock().longValue()),
            vaultHash);
        double sharedPrice = parseAmount(functions.callPricePerFullShare(vaultHash, dto.getBlock().longValue()),
            vaultHash);
        double vaultUnderlyingUnit = parseAmount(functions.callUnderlyingUnit(vaultHash, dto.getBlock().longValue()),
            vaultHash);
        double lpBalance = parseAmount(functions.callErc20TotalSupply(lpHash, dto.getBlock().longValue()), lpHash);
        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpHash, dto.getBlock().longValue());

        double vaultSharedBalance = (vaultBalance * sharedPrice);
        double vaultFraction = (vaultSharedBalance / vaultUnderlyingUnit) / lpBalance;

        Tuple2<Double, Double> uniPrices = priceProvider.getPriceForUniPair(vaultHash, dto.getBlock().longValue());

        double firstVault = vaultFraction * lpUnderlyingBalances.component1();
        double secondVault = vaultFraction * lpUnderlyingBalances.component2();

        try {
            Tuple2<String, String> coinNames =
                LpContracts.lpHashToCoinNames.get(LpContracts.harvestStrategyToLp.get(vaultHash));
            LpStat lpStat = new LpStat();
            lpStat.setCoin1(coinNames.component1());
            lpStat.setCoin2(coinNames.component2());
            lpStat.setAmount1(firstVault);
            lpStat.setAmount2(secondVault);
            dto.setLpStat(OBJECT_MAPPER.writeValueAsString(lpStat));
        } catch (JsonProcessingException e) {
            log.error("Error write lp stat", e);
        }

        Long firstVaultUsdAmount = Math.round(firstVault * uniPrices.component1());
        Long secondVaultUsdAmount = Math.round(secondVault * uniPrices.component2());
        long vaultUsdAmount = firstVaultUsdAmount + secondVaultUsdAmount;
        dto.setLastUsdTvl((double) vaultUsdAmount);

        double txFraction = (dto.getAmount() / vaultBalance);
        long txUsdAmount = Math.round(vaultUsdAmount * txFraction);
        dto.setUsdAmount(txUsdAmount);
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }

    public BlockingQueue<Log> getLogs() {
        return logs;
    }

    @PreDestroy
    public void stop() {
        run.set(false);
    }

}
