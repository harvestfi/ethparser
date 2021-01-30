package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.web3.ContractConstants.D18;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.erc20.Tokens.FARM_NAME;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.model.HardWorkTx;
import pro.belbix.ethparser.model.TokenTx;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.erc20.decoder.ERC20Decoder;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.decoder.HardWorkLogDecoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class HardWorkParser implements Web3Parser {

    public static final String CONTROLLER = "0x222412af183BCeAdEFd72e4Cb1b71f1889953b1C".toLowerCase();
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final HardWorkLogDecoder hardWorkLogDecoder = new HardWorkLogDecoder();
    private final ERC20Decoder erc20Decoder = new ERC20Decoder();
    private final PriceProvider priceProvider;
    private final Functions functions;
    private final Web3Service web3Service;
    private final HardWorkDbService hardWorkDbService;
    private final ParserInfo parserInfo;
    private Instant lastTx = Instant.now();

    public HardWorkParser(PriceProvider priceProvider,
                          Functions functions,
                          Web3Service web3Service,
                          HardWorkDbService hardWorkDbService, ParserInfo parserInfo) {
        this.priceProvider = priceProvider;
        this.functions = functions;
        this.web3Service = web3Service;
        this.hardWorkDbService = hardWorkDbService;
        this.parserInfo = parserInfo;
    }

    @Override
    public void startParse() {
        log.info("Start parse Hard work logs");
        web3Service.subscribeOnLogs(logs);
        parserInfo.addParser(this);
        new Thread(() -> {
            while (run.get()) {
                Log ethLog = null;
                try {
                    ethLog = logs.poll(1, TimeUnit.SECONDS);
                    HardWorkDTO dto = parseLog(ethLog);
                    if (dto != null) {
                        lastTx = Instant.now();
                        boolean saved = hardWorkDbService.save(dto);
                        if (saved) {
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

        // parse SharePriceChangeLog was wrong solution
        // some strategies doesn't change share price
        // but it is a good point for catch doHardWork
        if (!"SharePriceChangeLog".equals(tx.getMethodName())) {
            throw new IllegalStateException("Unknown method " + tx.getMethodName());
        }

        if (!Vaults.vaultHashToName.containsKey(tx.getVault())) {
            log.warn("Unknown vault " + tx.getVault());
            return null;
        }

        HardWorkDTO dto = new HardWorkDTO();
        dto.setId(tx.getHash() + "_" + tx.getLogId());
        dto.setBlock(tx.getBlock());
        dto.setBlockDate(tx.getBlockDate());
        dto.setVault(Vaults.vaultHashToName.get(tx.getVault()));
        dto.setShareChange(parseAmount(tx.getNewSharePrice().subtract(tx.getOldSharePrice()), tx.getVault()));

        parseRewards(dto, tx.getHash(), tx.getStrategy());

        log.info(dto.print());
        return dto;
    }

    // not in the root because it can be weekly reward
    private void parseRewards(HardWorkDTO dto, String txHash, String strategyHash) {
        String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
        // todo replace to VaultModel
        String underlyingTokenHash = functions.callUnderlying(vaultHash, dto.getBlock());
        TransactionReceipt tr = web3Service.fetchTransactionReceipt(txHash);
        boolean autoStake = isAutoStake(tr.getLogs());
        for (Log ethLog : tr.getLogs()) {
            parseRewardAddedEvents(ethLog, dto, autoStake);
        }

        // for AutoStaking vault rewards already parsed
        // skip vault reward parsing if we didn't earn anything
        // BROKEN LOGIC if we don't send rewards to strategy (yes, it happened for uni strats)
//        if (!autoStake && dto.getFarmBuyback() != 0) {
//            for (Log ethLog : tr.getLogs()) {
//                parseVaultReward(ethLog, dto, underlyingTokenHash, strategyHash);
//            }
//        }
        fillFeeInfo(dto, txHash, tr);
    }

    private boolean isAutoStake(List<Log> logs) {
        return logs.stream()
            .filter(l -> {
                try {
                    return hardWorkLogDecoder.decode(l).getMethodName().equals("RewardAdded");
                } catch (Exception ignored) {
                }
                return false;
            }).count() > 1;
    }

    private void parseRewardAddedEvents(Log ethLog, HardWorkDTO dto, boolean autoStake) {
        HardWorkTx tx;
        try {
            tx = hardWorkLogDecoder.decode(ethLog);
        } catch (Exception e) {
            return;
        }
        if (tx == null) {
            return;
        }

        if ("RewardAdded".equals(tx.getMethodName())) {
            if (!autoStake && dto.getFarmBuyback() != 0.0) {
                throw new IllegalStateException("Duplicate RewardAdded for " + dto);
            }
            double reward = tx.getReward().doubleValue() / D18;

            // AutoStake strategies have two RewardAdded events - first for PS and second for stake contract
            if (autoStake && dto.getFarmBuyback() != 0) {
                double farmPrice = priceProvider.getPriceForCoin(FARM_NAME, dto.getBlock());
                double stReward = ((reward * farmPrice) / 0.3) * 0.7;
                dto.setShareChangeUsd(stReward);
            } else {
                dto.setFarmBuyback(reward);

                if (!autoStake) {
                    double farmPrice = priceProvider.getPriceForCoin(FARM_NAME, dto.getBlock());
                    double stReward = ((reward * farmPrice) / 0.3) * 0.7;
                    dto.setShareChangeUsd(stReward);
                }
            }

        }
    }

    private void fillFeeInfo(HardWorkDTO dto, String txHash, TransactionReceipt tr) {
        Transaction transaction = web3Service.findTransaction(txHash);
        double gas = (tr.getGasUsed().doubleValue());
        double gasPrice = transaction.getGasPrice().doubleValue() / D18;
        double ethPrice = priceProvider.getPriceForCoin("ETH", dto.getBlock());
        double feeUsd = gas * gasPrice * ethPrice;
        dto.setFee(feeUsd);
    }

    private void parseVaultReward(Log ethLog, HardWorkDTO dto, String underlyingTokenHash, String strategyHash) {
        TokenTx tx;
        try {
            tx = erc20Decoder.decode(ethLog);
        } catch (Exception e) {
            return;
        }
        if (tx == null) {
            return;
        }

        if ("Transfer".equals(tx.getMethodName())) {
            // suppose that underlying token transferred to the strategy is reward
            if (!tx.getTokenAddress().equalsIgnoreCase(underlyingTokenHash)
                || !tx.getRecipient().equalsIgnoreCase(strategyHash)) {
                return;
            }

            if (dto.getShareChangeUsd() != 0.0) {
                //it is not elegant, but sometimes we have a few transfers and suppose that the last is reward
                log.debug("Duplicate transfer underlying, old value {}", dto.getShareChangeUsd());
            }
            String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
            double vaultReward = parseAmount(tx.getValue(), vaultHash);
            double price = calculateVaultRewardUsdPrice(dto.getVault(), underlyingTokenHash, dto.getBlock());
            dto.setShareChangeUsd(vaultReward * price);
        }
    }

    private double calculateVaultRewardUsdPrice(String vaultName, String underlyingTokenHash, long block) {
        if (Vaults.isLp(vaultName)) {
            return priceProvider.getLpPositionAmountInUsd(underlyingTokenHash, 1, block);
        } else {
            return priceProvider.getPriceForCoin(vaultName, block);
        }
    }

    @Deprecated
    private void fillUsdValues(HardWorkDTO dto) {
        if (Vaults.isLp(dto.getVault())) {
            fillUsdValuesForLP(dto);
        } else {
            fillUsdValuesForRegular(dto);
        }
    }

    @Deprecated
    private void fillUsdValuesForLP(HardWorkDTO dto) {
        String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
        String lpHash = Vaults.underlyingToken.get(vaultHash);
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock()),
            vaultHash);

        double lpBalance = parseAmount(functions.callErc20TotalSupply(lpHash, dto.getBlock()), lpHash);
        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpHash, dto.getBlock());

        double vaultSharedBalance = (vaultBalance * dto.getShareChange());
        double vaultFraction = vaultSharedBalance / lpBalance;

        Tuple2<Double, Double> uniPrices = priceProvider.getPairPriceForStrategyHash(vaultHash, dto.getBlock());

        double firstVault = vaultFraction * lpUnderlyingBalances.component1();
        double secondVault = vaultFraction * lpUnderlyingBalances.component2();

        Long firstVaultUsdAmount = Math.round(firstVault * uniPrices.component1());
        Long secondVaultUsdAmount = Math.round(secondVault * uniPrices.component2());
        long usdAmount = firstVaultUsdAmount + secondVaultUsdAmount;
        dto.setShareChangeUsd(usdAmount);
    }

    @Deprecated
    private void fillUsdValuesForRegular(HardWorkDTO dto) {
        String vaultHash = Vaults.vaultNameToHash.get(dto.getVault());
        Double price = priceProvider.getPriceForCoin(dto.getVault(), dto.getBlock());
        if (price == null) {
            throw new IllegalStateException("Unknown coin " + vaultHash);
        }
        double vaultBalance = parseAmount(functions.callErc20TotalSupply(vaultHash, dto.getBlock()), vaultHash);
        double changed = dto.getShareChange() * vaultBalance;
        double usdValue = price * changed;
        dto.setShareChangeUsd(usdValue);
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
