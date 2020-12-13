package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.model.HarvestTx.parseAmount;
import static pro.belbix.ethparser.web3.harvest.PriceStubSender.PRICE_STUB_TYPE;
import static pro.belbix.ethparser.web3.uniswap.contracts.Tokens.FARM_TOKEN;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
import pro.belbix.ethparser.web3.harvest.contracts.StakeContracts;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@Service
public class HarvestVaultParserV2 implements Web3Parser {

    private static final Logger log = LoggerFactory.getLogger(HarvestVaultParserV2.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private static final Set<String> allowedMethods = new HashSet<>(Collections.singletonList("transfer"));
    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    public static final String UNI_ROUTER = "0x7a250d5630b4cf539739df2c5dacb4c659f2488d".toLowerCase();
    public static final double BURNED_FARM = 14850.0;
    private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();
    private final Web3Service web3Service;
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final HarvestDBService harvestDBService;
    private final EthBlockService ethBlockService;
    private final PriceProvider priceProvider;
    private final Functions functions;

    public HarvestVaultParserV2(Web3Service web3Service,
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

        if (!isAllowedMethod(harvestTx)) {
            return null;
        }

        if (isPs(harvestTx)) {
            if (!parsePs(harvestTx)) {
                return null;
            }
        } else {
            if (!parseVaults(harvestTx, ethLog)) {
                return null;
            }
        }

        HarvestDTO dto = harvestTx.toDto();

        //enrich date
        dto.setBlockDate(
            ethBlockService.getTimestampSecForBlock(harvestTx.getBlockHash(), ethLog.getBlockNumber().longValue()));

        if (isPs(harvestTx)) {
            fillPsTvlAndUsdValue(dto, harvestTx.getVault().getValue());
        } else {
            //share price
            fillSharedPrice(dto, harvestTx);

            //usd values
            fillUsdPrice(dto, harvestTx.getVault().getValue());
        }
        log.info(dto.print());
        return dto;
    }

    private void fillPsTvlAndUsdValue(HarvestDTO dto, String vaultHash) {
        String st = StakeContracts.vaultHashToStakeHash.get(vaultHash);
        Double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock().longValue());
        double vaultBalance = parseAmount(
            functions.callErc20TotalSupply(st, dto.getBlock().longValue()), vaultHash);
        double allFarm = parseAmount(
            functions.callErc20TotalSupply(FARM_TOKEN, dto.getBlock().longValue()), vaultHash)
            - BURNED_FARM;
        dto.setLastUsdTvl(price * vaultBalance);
        dto.setLastTvl(vaultBalance);
        dto.setSharePrice(allFarm);
        dto.setUsdAmount(Math.round(dto.getAmount() * price));
    }

    private boolean parsePs(HarvestTx harvestTx) {
        if("Staked".equals(harvestTx.getMethodName())) {
            harvestTx.setMethodName("Deposit");
        } else if ("Staked#V2".equals(harvestTx.getMethodName())) {
            harvestTx.setMethodName("Deposit");
            harvestTx.setAmount(harvestTx.getIntFromArgs()[0]);
        } else if ("withdrawn".equals(harvestTx.getMethodName().toLowerCase())) {
            TransactionReceipt receipt = web3Service.fetchTransactionReceipt(harvestTx.getHash());
            String owner = receipt.getFrom();
            if (!owner.equals(harvestTx.getOwner())) {
                return false; //withdrawn for not owner is a garbage
            }
            harvestTx.setMethodName("Withdraw");
        } else {
            return false;
        }
        return true;
    }

    private boolean parseVaults(HarvestTx harvestTx, Log ethLog) {
        if (ZERO_ADDRESS.equals(harvestTx.getAddressFromArgs1().getValue())) {
            harvestTx.setMethodName("Deposit");
            harvestTx.setOwner(harvestTx.getAddressFromArgs2().getValue());
        } else if (ZERO_ADDRESS.equals(harvestTx.getAddressFromArgs2().getValue())) {
            harvestTx.setMethodName("Withdraw");
            harvestTx.setOwner(harvestTx.getAddressFromArgs1().getValue());
        } else {
            if (isMigration(harvestTx, StakeContracts.vaultHashToStakeHash.get(ethLog.getAddress()))) {
                log.warn("migrate? tx " + harvestTx.toString());
                harvestTx.setOwner(harvestTx.getAddressFromArgs1().getValue());
                harvestTx.setMethodName("Withdraw");
            } else {
                //test purpose
//                if(checkTransactionStructure(harvestTx)) {
//                    return null; //it's normal
//                }
//                log.error("unknown tx " + harvestTx.toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkTransactionStructure(HarvestTx harvestTx) {
        TransactionReceipt receipt = web3Service.fetchTransactionReceipt(harvestTx.getHash());
        return !UNI_ROUTER.equals(receipt.getTo())
            && !"0xebb4d6cfc2b538e2a7969aa4187b1c00b2762108".equals(receipt.getTo()) //?
            && !"0x743dd3139c6b70f664ab4329b2cde646f0bac99a".equals(receipt.getTo()) //swap WETH_V0 uni
            && !"0x7fe2153de0006d76c85cc04c8ea10bf4546c879e".equals(receipt.getTo()) //swap WETH_V0 uni
            && !"0x494cc492c9f01699bff1449180201dbfbd592ea5".equals(receipt.getTo()) //swap WETH_V0 uni
            && !"0x343e3a490c9251dc0eaa81da146ba6abe6c78b2d".equals(receipt.getTo()) //zapper WETH_V0 uni
            && !StakeContracts.hashToName.containsKey(receipt.getTo()) //stacking
            && !Vaults.vaultHashToName.containsKey(receipt.getTo()  //transfer
        );
    }

    private boolean isMigration(HarvestTx harvestTx, String currentStackingContract) {
        return StakeContracts.hashToName.containsKey(harvestTx.getAddressFromArgs2().getValue()) //it is transfer to stacking
            && !harvestTx.getAddressFromArgs2().getValue()
            .equals(currentStackingContract); // and it is not current contract
    }

    private void fillSharedPrice(HarvestDTO dto, HarvestTx harvestTx) {
        BigInteger sharedPriceInt = functions
            .callPricePerFullShare(harvestTx.getVault().getValue(), dto.getBlock().longValue());
        double sharedPrice;
        if (BigInteger.ONE.equals(sharedPriceInt)) {
            sharedPrice = 0.0;
        } else {
            sharedPrice = parseAmount(sharedPriceInt, harvestTx.getVault().getValue());
        }
        dto.setSharePrice(sharedPrice);
    }

    private HarvestDTO createStubPriceDto() {
        HarvestDTO dto = new HarvestDTO();
        dto.setBlockDate(Instant.now().getEpochSecond());
        dto.setBlock(web3Service.fetchCurrentBlock());
        dto.setMethodName(PRICE_STUB_TYPE);
        return dto;
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
        double sharedPrice = dto.getSharePrice();
//        double vaultUnderlyingUnit = parseAmount(functions.callUnderlyingUnit(vaultHash, dto.getBlock().longValue()),
//            vaultHash);
        double vaultUnderlyingUnit = 1.0; // currently always 1

        double vault = (vaultBalance * sharedPrice) / vaultUnderlyingUnit;
        dto.setLastTvl(vault);
        dto.setLastUsdTvl((double) Math.round(vault * price));
        dto.setUsdAmount((long) (price * dto.getAmount() * dto.getSharePrice()));
    }

    public void fillUsdValuesForLP(HarvestDTO dto, String vaultHash) {
        String lpHash = LpContracts.harvestStrategyToLp.get(vaultHash);
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock().longValue()),
            vaultHash);
        double sharedPrice = dto.getSharePrice();
//        double vaultUnderlyingUnit = parseAmount(functions.callUnderlyingUnit(vaultHash, dto.getBlock().longValue()),
//            vaultHash);
        double vaultUnderlyingUnit = 1.0; // currently always 1
        double lpBalance = parseAmount(functions.callErc20TotalSupply(lpHash, dto.getBlock().longValue()), lpHash);
        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpHash, dto.getBlock().longValue());

        double vaultSharedBalance = (vaultBalance * sharedPrice);
        double vaultFraction = (vaultSharedBalance / vaultUnderlyingUnit) / lpBalance;

        Tuple2<Double, Double> uniPrices = priceProvider.getPairPriceForStrategyHash(vaultHash, dto.getBlock().longValue());

        double firstVault = vaultFraction * lpUnderlyingBalances.component1();
        double secondVault = vaultFraction * lpUnderlyingBalances.component2();

        dto.setLpStat(LpStat.createJson(lpHash, firstVault, secondVault));

        Long firstVaultUsdAmount = Math.round(firstVault * uniPrices.component1());
        Long secondVaultUsdAmount = Math.round(secondVault * uniPrices.component2());
        long vaultUsdAmount = firstVaultUsdAmount + secondVaultUsdAmount;
        dto.setLastUsdTvl((double) vaultUsdAmount);

        double txFraction = (dto.getAmount() / vaultBalance);
        long txUsdAmount = Math.round(vaultUsdAmount * txFraction);
        dto.setUsdAmount(txUsdAmount);
    }

    private boolean isAllowedMethod(HarvestTx harvestTx) {
        if (isPs(harvestTx)) {
            return "staked".equals(harvestTx.getMethodName().toLowerCase())
                || "Staked#V2".equals(harvestTx.getMethodName())
                || "withdrawn".equals(harvestTx.getMethodName().toLowerCase());
        }
        return allowedMethods.contains(harvestTx.getMethodName().toLowerCase());
    }

    private boolean isPs(HarvestTx harvestTx) {
        return Vaults.PS.equals(harvestTx.getVault().getValue())
            ||  Vaults.PS_V0.equals(harvestTx.getVault().getValue());
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
