package pro.belbix.ethparser.web3.harvest.parser;

import static java.util.Collections.singletonList;
import static pro.belbix.ethparser.web3.ContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_TOKEN;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.model.HarvestTx;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.contracts.StakeContracts;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.decoder.HarvestVaultLogDecoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class HarvestVaultParserV2 implements Web3Parser {

    public static final double BURNED_FARM = 14850.0;
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private static final Set<String> allowedMethods = new HashSet<>(Collections.singletonList("transfer"));
    private final HarvestVaultLogDecoder harvestVaultLogDecoder = new HarvestVaultLogDecoder();
    private final Web3Service web3Service;
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final HarvestDBService harvestDBService;
    private final EthBlockService ethBlockService;
    private final PriceProvider priceProvider;
    private final Functions functions;
    private final ParserInfo parserInfo;
    private final HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
    private Instant lastTx = Instant.now();
    private long count = 0;

    public HarvestVaultParserV2(Web3Service web3Service,
                                HarvestDBService harvestDBService,
                                EthBlockService ethBlockService,
                                PriceProvider priceProvider,
                                Functions functions,
                                ParserInfo parserInfo,
                                HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator) {
        this.web3Service = web3Service;
        this.harvestDBService = harvestDBService;
        this.ethBlockService = ethBlockService;
        this.priceProvider = priceProvider;
        this.functions = functions;
        this.parserInfo = parserInfo;
        this.harvestOwnerBalanceCalculator = harvestOwnerBalanceCalculator;
    }

    @Override
    public void startParse() {
        log.info("Start parse Harvest vaults logs");
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
                    HarvestDTO dto = parseVaultLog(ethLog);
                    handleDto(dto);
                } catch (Exception e) {
                    log.error("Can't save " + ethLog, e);
                }
            }
        }).start();
    }

    private void handleDto(HarvestDTO dto) throws InterruptedException {
        if (dto != null) {
            lastTx = Instant.now();
            enrichDto(dto);
            harvestOwnerBalanceCalculator.fillBalance(dto);
            boolean success = harvestDBService.saveHarvestDTO(dto);

            if (success) {
                output.put(dto);
            }
            if (dto.getMigration() != null) {
                handleDto(dto.getMigration());
            }
        }
    }

    public HarvestDTO parseVaultLog(Log ethLog) {
        if (ethLog == null) {
            return null;
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
            fillSharedPrice(dto);

            //usd values
            fillUsdPrice(dto);
        }
        log.info(dto.print());
        if (harvestTx.isMigration()) {
            parseMigration(dto);
        }
        return dto;
    }

    private void fillPsTvlAndUsdValue(HarvestDTO dto, String vaultHash) {
        String st = StakeContracts.vaultHashToStakeHash.get(vaultHash);
        Double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock());
        double vaultBalance = parseAmount(
            functions.callErc20TotalSupply(st, dto.getBlock()), vaultHash);
        double allFarm = parseAmount(
            functions.callErc20TotalSupply(FARM_TOKEN, dto.getBlock()), vaultHash)
            - BURNED_FARM;
        dto.setLastUsdTvl(price * vaultBalance);
        dto.setLastTvl(vaultBalance);
        dto.setSharePrice(allFarm);
        dto.setUsdAmount(Math.round(dto.getAmount() * price));
    }

    private boolean parsePs(HarvestTx harvestTx) {
        if ("Staked".equals(harvestTx.getMethodName())) {
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
                log.info("migrate tx " + harvestTx.getHash());
                harvestTx.setOwner(harvestTx.getAddressFromArgs1().getValue());
                harvestTx.setMethodName("Withdraw");
                harvestTx.setMigration(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public void parseMigration(HarvestDTO dto) {
        int onBlock = dto.getBlock().intValue();
        String newVault = Vaults.oldVaultToNew(dto.getVault());
        String newVaultHash = Vaults.vaultNameToHash.get(newVault);
        String stHash = StakeContracts.vaultHashToStakeHash.get(newVaultHash);
        if (stHash == null) {
            throw new IllegalStateException("Stake address not found for " + newVault);
        }

        List<LogResult> logResults = web3Service
            .fetchContractLogs(singletonList(stHash), onBlock, onBlock);
        HarvestTx migrationTx = null;
        for (LogResult ethLog : logResults) {
            HarvestTx tx = harvestVaultLogDecoder.decode((Log) ethLog.get());
            if (tx.getMethodName().equalsIgnoreCase("Migrated")) {
                migrationTx = tx;
                break;
            }
        }
        if (migrationTx == null) {
            log.warn("Migration not found for " + dto);
            return;
        }
        migrationTx.setVault(new Address(newVaultHash));
        createHarvestFromMigration(dto, migrationTx);
    }

    private void createHarvestFromMigration(HarvestDTO dto, HarvestTx migrationTx) {
        HarvestDTO migrationDto = migrationTx.toDto();
        migrationDto.setBlockDate(
            ethBlockService.getTimestampSecForBlock(migrationTx.getBlockHash(), migrationTx.getBlock().longValue()));
        migrationDto.setMethodName("Deposit");
        migrationDto.setVault(Vaults.oldVaultToNew(dto.getVault()));

        migrationDto
            .setAmount(
                parseAmount(migrationTx.getIntFromArgs()[1], Vaults.vaultNameToHash.get(migrationDto.getVault())));

        fillSharedPrice(migrationDto);
        fillUsdPrice(migrationDto);
        migrationDto.setMigrated(true);
        log.info("Migrated harvest " + migrationDto);
        dto.setMigration(migrationDto);
    }

    private boolean isMigration(HarvestTx harvestTx, String currentStackingContract) {
        return StakeContracts.hashToName.containsKey(harvestTx.getAddressFromArgs2().getValue())
            //it is transfer to stacking
            && !harvestTx.getAddressFromArgs2().getValue()
            .equals(currentStackingContract); // and it is not current contract
    }

    private void fillSharedPrice(HarvestDTO dto) {
        String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
        BigInteger sharedPriceInt = functions
            .callPricePerFullShare(vaultHash, dto.getBlock());
        double sharedPrice;
        if (BigInteger.ONE.equals(sharedPriceInt)) {
            sharedPrice = 0.0;
        } else {
            sharedPrice = parseAmount(sharedPriceInt, vaultHash);
        }
        dto.setSharePrice(sharedPrice);
    }

    public void enrichDto(HarvestDTO dto) {
        //set gas
        dto.setLastGas(web3Service.fetchAverageGasPrice());
    }

    private void fillUsdPrice(HarvestDTO dto) {
        if (Vaults.isLp(dto.getVault())) {
            fillUsdValuesForLP(dto);
        } else {
            fillUsdValues(dto);
        }
    }

    private void fillUsdValues(HarvestDTO dto) {
        String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
        Double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock());
        if (price == null) {
            throw new IllegalStateException("Unknown coin " + dto.getVault());
        }
        dto.setUnderlyingPrice(price);
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock()),
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

    public void fillUsdValuesForLP(HarvestDTO dto) {
        long dtoBlock = dto.getBlock();
        String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
        String lpHash = Vaults.underlyingToken.get(vaultHash);
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dtoBlock),
            vaultHash);
        double sharedPrice = dto.getSharePrice();
//        double vaultUnderlyingUnit = parseAmount(functions.callUnderlyingUnit(vaultHash, dtoBlock),
//            vaultHash);
        double vaultUnderlyingUnit = 1.0; // currently always 1
        double lpTotalSupply = parseAmount(functions.callErc20TotalSupply(lpHash, dtoBlock), lpHash);

        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpHash, dtoBlock);
        if (lpUnderlyingBalances == null) {
            log.error("lpUnderlyingBalances is null. mb wrong lp contract for " + dto);
            return;
        }

        double lpUnderlyingBalance1 = lpUnderlyingBalances.component1();
        double lpUnderlyingBalance2 = lpUnderlyingBalances.component2();

        double vaultSharedBalance = (vaultBalance * sharedPrice);
        double vaultFraction = (vaultSharedBalance / vaultUnderlyingUnit) / lpTotalSupply;

        Tuple2<Double, Double> uniPrices = priceProvider.getPairPriceForStrategyHash(vaultHash, dtoBlock);

        //suppose it's ONE_INCH ETH pair
        // todo investigate how to calculate it (Mooniswap contract)
        if (lpUnderlyingBalance1 == 0) {
            double coin2Usd = lpUnderlyingBalance2 * uniPrices.component2();
            lpUnderlyingBalance1 = (coin2Usd / uniPrices.component1());
        } else if (lpUnderlyingBalance2 == 0) {
            double coin1Usd = lpUnderlyingBalance1 * uniPrices.component1();
            lpUnderlyingBalance2 = (coin1Usd / uniPrices.component2());
        }

        double firstVault = vaultFraction * lpUnderlyingBalance1;
        double secondVault = vaultFraction * lpUnderlyingBalance2;

        dto.setLpStat(LpStat.createJson(
            lpHash,
            firstVault,
            secondVault,
            uniPrices.component1(),
            uniPrices.component2()
        ));

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
            return "staked".equalsIgnoreCase(harvestTx.getMethodName())
                || "Staked#V2".equalsIgnoreCase(harvestTx.getMethodName())
                || "withdrawn".equalsIgnoreCase(harvestTx.getMethodName());
        }
        return allowedMethods.contains(harvestTx.getMethodName().toLowerCase());
    }

    private boolean isPs(HarvestTx harvestTx) {
        return Vaults.PS.equals(harvestTx.getVault().getValue())
            || Vaults.PS_V0.equals(harvestTx.getVault().getValue());
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

    @Override
    public Instant getLastTx() {
        return lastTx;
    }

}
