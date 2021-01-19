package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.model.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;
import static pro.belbix.ethparser.web3.harvest.contracts.StakeContracts.vaultHashToStakeHash;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_GRAIN_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_USDC_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.UNI_LP_WETH_FARM;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.findLpForCoins;
import static pro.belbix.ethparser.web3.uniswap.contracts.LpContracts.findNameForLpHash;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.web3.Functions;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;

@Service
@Log4j2
public class UniToHarvestConverter implements Web3Parser {

    public static final Set<String> allowContracts = new HashSet<>(Arrays.asList(
        UNI_LP_USDC_FARM,
        UNI_LP_WETH_FARM,
        UNI_LP_GRAIN_FARM
    ));
    private static final AtomicBoolean run = new AtomicBoolean(true);
    private final BlockingQueue<UniswapDTO> uniswapDTOS = new ArrayBlockingQueue<>(1000);
    private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
    private Instant lastTx = Instant.now();

    private final PriceProvider priceProvider;
    private final Functions functions;
    private final HarvestDBService harvestDBService;
    private final ParserInfo parserInfo;

    public UniToHarvestConverter(PriceProvider priceProvider, Functions functions,
                                 HarvestDBService harvestDBService, ParserInfo parserInfo) {
        this.priceProvider = priceProvider;
        this.functions = functions;
        this.harvestDBService = harvestDBService;
        this.parserInfo = parserInfo;
    }

    @Override
    public void startParse() {
        log.info("Start UniToHarvestConverter");
        parserInfo.addParser(this);
        new Thread(() -> {
            while (run.get()) {
                UniswapDTO uniswapDTO = null;
                try {
                    uniswapDTO = uniswapDTOS.poll(1, TimeUnit.SECONDS);
                    HarvestDTO dto = convert(uniswapDTO);
                    if (dto != null) {
                        lastTx = Instant.now();
                        boolean success = harvestDBService.saveHarvestDTO(dto);
                        if (success) {
                            output.put(dto);
                        }
                    }
                } catch (Exception e) {
                    log.error("Can't save harvest dto for" + uniswapDTO, e);
                }
            }
        }).start();
    }

    public void addDtoToQueue(UniswapDTO dto) {
        uniswapDTOS.add(dto);
    }

    public HarvestDTO convert(UniswapDTO uniswapDTO) {
        if (uniswapDTO == null || !uniswapDTO.isLiquidity()) {
            return null;
        }
        String lpHash = findLpForCoins(uniswapDTO.getCoin(), uniswapDTO.getOtherCoin());
        if (!allowContracts.contains(lpHash)) {
            return null;
        }
        HarvestDTO harvestDTO = new HarvestDTO();
        fillCommonFields(uniswapDTO, harvestDTO, lpHash);

        fillUsdValuesForLP(uniswapDTO, harvestDTO, lpHash);

        try {
            harvestDTO.setPrices(priceProvider.getAllPrices(harvestDTO.getBlock()));
        } catch (JsonProcessingException e) {
            log.info("Error parse prices");
        }
        log.info(harvestDTO.print());
        return harvestDTO;
    }

    public void fillUsdValuesForLP(UniswapDTO uniswapDTO, HarvestDTO harvestDTO, String lpHash) {
        long block = harvestDTO.getBlock();
        String stakeHash = vaultHashToStakeHash.get(lpHash);

        double lpBalance = parseAmount(functions.callErc20TotalSupply(lpHash, block), lpHash);
        double stBalance = parseAmount(functions.callErc20TotalSupply(stakeHash, block), lpHash);
        harvestDTO.setLastTvl(stBalance);
        double stFraction = stBalance / lpBalance;
        if(Double.isNaN(stFraction) || Double.isInfinite(stFraction)) {
            stFraction = 0;
        }

        Tuple2<Double, Double> lpUnderlyingBalances = functions.callReserves(lpHash, block);
        double firstCoinBalance = lpUnderlyingBalances.component1() * stFraction;
        double secondCoinBalance = lpUnderlyingBalances.component2() * stFraction;

        harvestDTO.setLpStat(LpStat.createJson(lpHash, firstCoinBalance, secondCoinBalance));

        Tuple2<Double, Double> uniPrices = priceProvider.getPairPriceForLpHash(lpHash, block);
        double firstCoinUsdAmount = firstCoinBalance * uniPrices.component1();
        double secondCoinUsdAmount = secondCoinBalance * uniPrices.component2();
        double vaultUsdAmount = firstCoinUsdAmount + secondCoinUsdAmount;
        harvestDTO.setLastUsdTvl(vaultUsdAmount);

        double usdAmount = uniswapDTO.getAmount() * priceProvider.getPriceForCoin(uniswapDTO.getCoin(), block) * 2;
        harvestDTO.setUsdAmount(Math.round(usdAmount));

        double fraction = usdAmount / (vaultUsdAmount / stFraction);
        if(Double.isNaN(fraction) || Double.isInfinite(fraction)) {
            fraction = 0;
        }
        harvestDTO.setAmount(lpBalance * fraction); //not accurate
    }

    private void fillCommonFields(UniswapDTO uniswapDTO, HarvestDTO harvestDTO, String lpHash) {
        harvestDTO.setId(uniswapDTO.getId());
        harvestDTO.setHash(uniswapDTO.getHash());
        harvestDTO.setBlock(uniswapDTO.getBlock().longValue());
        harvestDTO.setConfirmed(true);
        harvestDTO.setBlockDate(uniswapDTO.getBlockDate());
        harvestDTO.setOwner(uniswapDTO.getOwner());
        harvestDTO.setVault(findNameForLpHash(lpHash));
        harvestDTO.setLastGas(uniswapDTO.getLastGas());
        harvestDTO.setSharePrice(1.0);
        harvestDTO.setOwnerBalance(uniswapDTO.getOwnerBalance());
        harvestDTO.setOwnerBalanceUsd(uniswapDTO.getOwnerBalanceUsd());

        if (ADD_LIQ.equals(uniswapDTO.getType())) {
            harvestDTO.setMethodName("Deposit");
        } else {
            harvestDTO.setMethodName("Withdraw");
        }
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
