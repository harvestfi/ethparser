package pro.belbix.ethparser.web3.harvest;

import static pro.belbix.ethparser.model.HarvestTx.parseAmount;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.model.HardWorkTx;
import pro.belbix.ethparser.repositories.HardWorkRepository;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.uniswap.LpContracts;

@Service
public class HardWorkParser implements Web3Parser {

    public static final String CONTROLLER = "0x222412af183BCeAdEFd72e4Cb1b71f1889953b1C".toLowerCase();
    private static final Logger log = LoggerFactory.getLogger(HardWorkParser.class);
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final HardWorkLogDecoder hardWorkLogDecoder = new HardWorkLogDecoder();

    private final PriceProvider priceProvider;
    private final Functions functions;
    private final Web3Service web3Service;
    private final HardWorkDbService hardWorkDbService;

    public HardWorkParser(PriceProvider priceProvider,
                          Functions functions,
                          Web3Service web3Service,
                          HardWorkDbService hardWorkDbService) {
        this.priceProvider = priceProvider;
        this.functions = functions;
        this.web3Service = web3Service;
        this.hardWorkDbService = hardWorkDbService;
    }

    @Override
    public void startParse() {
        log.info("Start parse Hard work logs");
        web3Service.subscribeOnLogs(logs);
        new Thread(() -> {
            while (run.get()) {
                Log ethLog = null;
                try {
                    ethLog = logs.poll(1, TimeUnit.SECONDS);
                    HardWorkDTO dto = parseLog(ethLog);
                    if (dto != null) {
                        boolean saved = hardWorkDbService.save(dto);
                        if(saved) {
                            output.put(dto);
                        }
                    }
                } catch (Exception e) {
                    log.error("Can't save " + ethLog, e);
                }
            }
        }).start();
    }

    public HardWorkDTO parseLog(Log ethLog) {
        if (ethLog == null || !CONTROLLER.equals(ethLog.getAddress())) {
            return null;
        }

        HardWorkTx tx;
        try {
            tx = hardWorkLogDecoder.decode(ethLog);
        } catch (Exception e) {
            log.error("Error decode " + ethLog, e);
            return null;
        }
        if (tx == null) {
            return null;
        }
        if(!"SharePriceChangeLog".equals(tx.getMethodName())) {
            throw new IllegalStateException("Unknown method " + tx.getMethodName());
        }

        if (!Vaults.vaultNames.containsKey(tx.getVault())) {
            log.warn("Unknown vault " + tx.getVault());
            return null;
        }

        HardWorkDTO dto = new HardWorkDTO();
        dto.setId(tx.getHash());
        dto.setBlock(tx.getBlock());
        dto.setBlockDate(tx.getBlockDate());
        dto.setVault(Vaults.vaultNames.get(tx.getVault()));
        dto.setShareChange(parseAmount(tx.getNewSharePrice().subtract(tx.getOldSharePrice()), tx.getVault()));

        fillUsdValues(dto, tx.getVault());

//        parseReceipt(dto, tx.getVault());
        log.info(dto.print());
        return dto;
    }

    private void parseReceipt(HardWorkDTO dto, String vaultHash) {
        TransactionReceipt receipt = web3Service.fetchTransactionReceipt(dto.getId());
        for (Log ethLog : receipt.getLogs()) {
            HardWorkTx tx = hardWorkLogDecoder.decode(ethLog);
            if("ProfitLogInReward".equals(tx.getMethodName())) {
                double profit = parseAmount(tx.getProfitAmount(), vaultHash);
                double fees = parseAmount(tx.getFeeAmount(), vaultHash);
//                log.info(profit+"");
            }
        }
    }

    private void fillUsdValues(HardWorkDTO dto, String vaultHash) {
        if (Vaults.lpTokens.contains(dto.getVault())) {
            fillUsdValuesForLP(dto, vaultHash);
        } else {
            fillUsdValuesForRegular(dto, vaultHash);
        }
    }

    private void fillUsdValuesForRegular(HardWorkDTO dto, String vaultHash) {
        Double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock());
        if (price == null) {
            throw new IllegalStateException("Unknown coin " + vaultHash);
        }
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock()), vaultHash);
        double changed = dto.getShareChange() * vaultBalance;
        double usdValue = price * changed;
        dto.setShareChangeUsd(usdValue);
    }

    private void fillUsdValuesForLP(HardWorkDTO dto, String vaultHash) {
        String lpHash = LpContracts.harvestStrategyToLp.get(vaultHash);
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock()),
            vaultHash);
        double sharedPrice = dto.getShareChange();

        double lpBalance = parseAmount(functions.callErc20TotalSupply(lpHash, dto.getBlock()), lpHash);
        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpHash, dto.getBlock());

        double vaultSharedBalance = (vaultBalance * sharedPrice);
        double vaultFraction = vaultSharedBalance / lpBalance;

        Tuple2<Double, Double> uniPrices = priceProvider.getPriceForUniPair(vaultHash, dto.getBlock());

        double firstVault = vaultFraction * lpUnderlyingBalances.component1();
        double secondVault = vaultFraction * lpUnderlyingBalances.component2();

        Long firstVaultUsdAmount = Math.round(firstVault * uniPrices.component1());
        Long secondVaultUsdAmount = Math.round(secondVault * uniPrices.component2());
        long usdAmount = firstVaultUsdAmount + secondVaultUsdAmount;
        dto.setShareChangeUsd(usdAmount);
    }

    @Override
    public BlockingQueue<DtoI> getOutput() {
        return output;
    }
}
