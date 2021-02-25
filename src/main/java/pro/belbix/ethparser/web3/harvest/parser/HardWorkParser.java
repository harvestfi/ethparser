package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.web3.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.FunctionsNames.UNDERLYING_BALANCE_IN_VAULT;
import static pro.belbix.ethparser.web3.FunctionsNames.UNDERLYING_BALANCE_WITH_INVESTMENT;
import static pro.belbix.ethparser.web3.FunctionsNames.VAULT_FRACTION_TO_INVEST_DENOMINATOR;
import static pro.belbix.ethparser.web3.FunctionsNames.VAULT_FRACTION_TO_INVEST_NUMERATOR;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLER;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D18;

import java.math.BigInteger;
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
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.model.HardWorkTx;
import pro.belbix.ethparser.model.TokenTx;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.FunctionsUtils;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.erc20.decoder.ERC20Decoder;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;
import pro.belbix.ethparser.web3.harvest.decoder.HardWorkLogDecoder;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class HardWorkParser implements Web3Parser {

    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<Log> logs = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private final HardWorkLogDecoder hardWorkLogDecoder = new HardWorkLogDecoder();
    private final ERC20Decoder erc20Decoder = new ERC20Decoder();
    private final PriceProvider priceProvider;
    private final FunctionsUtils functionsUtils;
    private final Web3Service web3Service;
    private final HardWorkDbService hardWorkDbService;
    private final ParserInfo parserInfo;
    private final AppProperties appProperties;
    private Instant lastTx = Instant.now();

    public HardWorkParser(PriceProvider priceProvider,
                          FunctionsUtils functionsUtils,
                          Web3Service web3Service,
                          HardWorkDbService hardWorkDbService, ParserInfo parserInfo,
                          AppProperties appProperties) {
        this.priceProvider = priceProvider;
        this.functionsUtils = functionsUtils;
        this.web3Service = web3Service;
        this.hardWorkDbService = hardWorkDbService;
        this.parserInfo = parserInfo;
        this.appProperties = appProperties;
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
                    if (appProperties.isStopOnParseError()) {
                        System.exit(-1);
                    }
                }
            }
        }).start();
    }

    public HardWorkDTO parseLog(Log ethLog) {
        if (ethLog == null || !CONTROLLER.equals(ethLog.getAddress())) {
            return null;
        }

        HardWorkTx tx = hardWorkLogDecoder.decode(ethLog);
        if (tx == null) {
            return null;
        }

        // parse SharePriceChangeLog was wrong solution
        // some strategies doesn't change share price
        // but it is a good point for catch doHardWork
        if (!"SharePriceChangeLog".equals(tx.getMethodName())) {
            throw new IllegalStateException("Unknown method " + tx.getMethodName());
        }

        if (ContractUtils.getNameByAddress(tx.getVault()).isEmpty()) {
            log.warn("Unknown vault " + tx.getVault());
            return null;
        }
        String vaultName = ContractUtils.getNameByAddress(tx.getVault())
            .orElseThrow(() -> new IllegalStateException("Not found name by " + tx.getVault()));
        if (vaultName.endsWith("_V0")) {
            // skip old strategies
            return null;
        }
        HardWorkDTO dto = new HardWorkDTO();
        dto.setId(tx.getHash() + "_" + tx.getLogId());
        dto.setBlock(tx.getBlock());
        dto.setBlockDate(tx.getBlockDate());
        dto.setVault(vaultName);
        dto.setShareChange(parseAmount(tx.getNewSharePrice().subtract(tx.getOldSharePrice()), tx.getVault()));

        parseRewards(dto, tx.getHash(), tx.getStrategy());
        parseVaultInvestedFunds(dto);

        log.info(dto.print());
        return dto;
    }

    // not in the root because it can be weekly reward
    private void parseRewards(HardWorkDTO dto, String txHash, String strategyHash) {
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
                double farmPrice = priceProvider.getPriceForCoin("FARM", dto.getBlock());
                double stReward = ((reward * farmPrice) / 0.3) * 0.7;
                dto.setShareChangeUsd(stReward);
            } else {
                dto.setFarmBuyback(reward);
                double farmPrice = priceProvider.getPriceForCoin("FARM", dto.getBlock());
                double ethPrice = priceProvider.getPriceForCoin("ETH", dto.getBlock());
                double farmBuybackEth = reward * farmPrice / ethPrice;
                dto.setFarmBuybackEth(farmBuybackEth);

                if (!autoStake) {
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
        double feeEth = gas * gasPrice;
        dto.setFeeEth(feeEth);
        dto.setGasUsed(gas);
    }

    private void parseVaultInvestedFunds(HardWorkDTO dto) {
        String vaultHash = ContractUtils.getAddressByName(dto.getVault(), ContractType.VAULT).get();
        double underlyingBalanceInVault = functionsUtils.callIntByName(
            UNDERLYING_BALANCE_IN_VAULT,
            vaultHash,
            dto.getBlock()).orElse(BigInteger.ZERO).doubleValue();
        double underlyingBalanceWithInvestment = functionsUtils.callIntByName(
            UNDERLYING_BALANCE_WITH_INVESTMENT,
            vaultHash,
            dto.getBlock()).orElse(BigInteger.ZERO).doubleValue();
        double vaultFractionToInvestNumerator = functionsUtils.callIntByName(
            VAULT_FRACTION_TO_INVEST_NUMERATOR,
            vaultHash,
            dto.getBlock()).orElse(BigInteger.ZERO).doubleValue();
        double vaultFractionToInvestDenominator = functionsUtils.callIntByName(
            VAULT_FRACTION_TO_INVEST_DENOMINATOR,
            vaultHash,
            dto.getBlock()).orElse(BigInteger.ZERO).doubleValue();

        double invested =
            100.0 * (underlyingBalanceWithInvestment - underlyingBalanceInVault) / underlyingBalanceWithInvestment;
        dto.setInvested(invested);
        double target = 100.0 * (vaultFractionToInvestNumerator / vaultFractionToInvestDenominator);
        dto.setInvestmentTarget(target);
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
