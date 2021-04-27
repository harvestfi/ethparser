package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.model.tx.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.PARSABLE_UNI_PAIRS;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class UniToHarvestConverter implements Web3Parser {

  private final ContractUtils contractUtils = ContractUtils.getInstance(ETH_NETWORK);
  private static final AtomicBoolean run = new AtomicBoolean(true);
  private final BlockingQueue<UniswapDTO> uniswapDTOS = new ArrayBlockingQueue<>(100);
  private final BlockingQueue<DtoI> output = new ArrayBlockingQueue<>(100);
  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final VaultActionsDBService vaultActionsDBService;
  private final ParserInfo parserInfo;
  private final AppProperties appProperties;
  private Instant lastTx = Instant.now();

  public UniToHarvestConverter(PriceProvider priceProvider, FunctionsUtils functionsUtils,
      VaultActionsDBService vaultActionsDBService, ParserInfo parserInfo,
      AppProperties appProperties) {
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.vaultActionsDBService = vaultActionsDBService;
    this.parserInfo = parserInfo;
    this.appProperties = appProperties;
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
            boolean success = vaultActionsDBService.saveHarvestDTO(dto);
            if (success) {
              output.put(dto);
            }
          }
        } catch (Exception e) {
          log.error("Can't save harvest dto for" + uniswapDTO, e);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }).start();
  }

  public HarvestDTO convert(UniswapDTO uniswapDTO) {
    if (uniswapDTO == null || !uniswapDTO.isLiquidity()) {
      return null;
    }
    String lpHash = contractUtils.findUniPairForTokens(
        contractUtils.getAddressByName(uniswapDTO.getCoin(), ContractType.TOKEN)
            .orElseThrow(
                () -> new IllegalStateException("Not found address for " + uniswapDTO.getCoin())),
        contractUtils.getAddressByName(uniswapDTO.getOtherCoin(), ContractType.TOKEN)
            .orElseThrow(() -> new IllegalStateException(
                "Not found address for " + uniswapDTO.getOtherCoin()))
    );
    if (!PARSABLE_UNI_PAIRS.get(ETH_NETWORK).contains(lpHash)) {
      return null;
    }
    HarvestDTO harvestDTO = new HarvestDTO();
    fillCommonFields(uniswapDTO, harvestDTO, lpHash);

    fillUsdValuesForLP(uniswapDTO, harvestDTO, lpHash);

    log.info(harvestDTO.print());
    return harvestDTO;
  }

  private void fillCommonFields(UniswapDTO uniswapDTO, HarvestDTO harvestDTO, String lpHash) {
    harvestDTO.setId(uniswapDTO.getId());
    harvestDTO.setHash(uniswapDTO.getHash());
    harvestDTO.setBlock(uniswapDTO.getBlock().longValue());
    harvestDTO.setNetwork(ETH_NETWORK);
    harvestDTO.setConfirmed(1);
    harvestDTO.setBlockDate(uniswapDTO.getBlockDate());
    harvestDTO.setOwner(uniswapDTO.getOwner());
    harvestDTO.setVault(contractUtils.getNameByAddress(lpHash)
        .orElseThrow(() -> new IllegalStateException("Not found name for " + lpHash)));
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

  public void fillUsdValuesForLP(UniswapDTO uniswapDTO, HarvestDTO harvestDTO, String lpHash) {
    long block = harvestDTO.getBlock();
    ContractEntity poolContract = contractUtils.poolByVaultAddress(lpHash)
        .orElseThrow(() -> new IllegalStateException("Not found pool for " + lpHash))
        .getContract();
    if (poolContract.getCreated() > uniswapDTO.getBlock().longValue()) {
      log.warn("Pool not created yet {} ", poolContract.getName());
      harvestDTO.setLastTvl(0.0);
      harvestDTO.setLpStat("");
      harvestDTO.setLastUsdTvl(0.0);
      harvestDTO.setUsdAmount(0L);
      harvestDTO.setAmount(0.0);
      return;
    }
    String poolAddress = poolContract.getAddress();

    double lpBalance = ContractUtils.getInstance(ETH_NETWORK).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, lpHash, block, ETH_NETWORK)
            .orElseThrow(() -> new IllegalStateException("Error get supply for " + lpHash)),
        lpHash);
    double stBalance = ContractUtils.getInstance(ETH_NETWORK).parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, poolAddress, block, ETH_NETWORK)
            .orElseThrow(() -> new IllegalStateException("Error get supply for " + poolAddress)),
        lpHash);
    harvestDTO.setLastTvl(stBalance);
    double stFraction = stBalance / lpBalance;
    if (Double.isNaN(stFraction) || Double.isInfinite(stFraction)) {
      stFraction = 0;
    }

    Tuple2<Double, Double> lpUnderlyingBalances = functionsUtils.callReserves(
        lpHash, block, ETH_NETWORK);
    double firstCoinBalance = lpUnderlyingBalances.component1() * stFraction;
    double secondCoinBalance = lpUnderlyingBalances.component2() * stFraction;

    Tuple2<Double, Double> uniPrices = priceProvider
        .getPairPriceForLpHash(lpHash, block, ETH_NETWORK);
    harvestDTO.setLpStat(LpStat.createJson(
        lpHash,
        firstCoinBalance,
        secondCoinBalance,
        uniPrices.component1(),
        uniPrices.component2(),
        ETH_NETWORK
    ));
    double firstCoinUsdAmount = firstCoinBalance * uniPrices.component1();
    double secondCoinUsdAmount = secondCoinBalance * uniPrices.component2();
    double vaultUsdAmount = firstCoinUsdAmount + secondCoinUsdAmount;
    harvestDTO.setLastUsdTvl(vaultUsdAmount);

    double usdAmount =
        uniswapDTO.getAmount()
            * priceProvider.getPriceForCoin(uniswapDTO.getCoin(), block, ETH_NETWORK)
            * 2;
    harvestDTO.setUsdAmount(Math.round(usdAmount));

    double fraction = usdAmount / (vaultUsdAmount / stFraction);
    if (Double.isNaN(fraction) || Double.isInfinite(fraction)) {
      fraction = 0;
    }
    harvestDTO.setAmount(lpBalance * fraction); //not accurate
  }

  public void addDtoToQueue(UniswapDTO dto) {
    uniswapDTOS.add(dto);
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
