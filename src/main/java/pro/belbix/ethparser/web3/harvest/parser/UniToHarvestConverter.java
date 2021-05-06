package pro.belbix.ethparser.web3.harvest.parser;

import static pro.belbix.ethparser.model.tx.UniswapTx.ADD_LIQ;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.TOTAL_SUPPLY;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.tuples.generated.Tuple2;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.model.Web3Model;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.ParserInfo;
import pro.belbix.ethparser.web3.Web3Parser;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class UniToHarvestConverter extends Web3Parser<HarvestDTO, UniswapDTO> {

  private final PriceProvider priceProvider;
  private final FunctionsUtils functionsUtils;
  private final VaultActionsDBService vaultActionsDBService;
  private final NetworkProperties networkProperties;
  private final ContractDbService contractDbService;

  public UniToHarvestConverter(PriceProvider priceProvider, FunctionsUtils functionsUtils,
      VaultActionsDBService vaultActionsDBService, ParserInfo parserInfo,
      AppProperties appProperties,
      NetworkProperties networkProperties,
      ContractDbService contractDbService) {
    super(parserInfo, appProperties);
    this.priceProvider = priceProvider;
    this.functionsUtils = functionsUtils;
    this.vaultActionsDBService = vaultActionsDBService;
    this.networkProperties = networkProperties;
    this.contractDbService = contractDbService;
  }

  @Override
  protected void subscribeToInput() {
    // we write to this parser directly from uniswap parser
  }

  @Override
  protected boolean save(HarvestDTO dto) {
    return vaultActionsDBService.saveHarvestDTO(dto);
  }

  @Override
  protected boolean isActiveForNetwork(String network) {
    return networkProperties.get(network).isConvertUniToHarvest();
  }

  public HarvestDTO parse(UniswapDTO uniswapDTO, String network) {
    if (uniswapDTO == null || !uniswapDTO.isLiquidity()) {
      return null;
    }
    String lpHash = contractDbService.findLpForTokens(
        uniswapDTO.getCoinAddress(), uniswapDTO.getOtherCoinAddress(), ETH_NETWORK)
        .map(lp -> lp.getContract().getAddress())
        .orElseThrow();
    if (!ContractUtils.isFullParsableLp(lpHash, ETH_NETWORK)) {
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
    harvestDTO.setVault(contractDbService.getNameByAddress(lpHash, ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Not found name for " + lpHash)));
    harvestDTO.setVaultAddress(lpHash);
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
    ContractEntity poolContract = contractDbService
        .getPoolContractByVaultAddress(lpHash, ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Not found pool for " + lpHash));
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

    double lpBalance = contractDbService.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, lpHash, block, ETH_NETWORK)
            .orElseThrow(() -> new IllegalStateException("Error get supply for " + lpHash)),
        lpHash, ETH_NETWORK);
    double stBalance = contractDbService.parseAmount(
        functionsUtils.callIntByName(TOTAL_SUPPLY, poolAddress, block, ETH_NETWORK)
            .orElseThrow(() -> new IllegalStateException("Error get supply for " + poolAddress)),
        lpHash, ETH_NETWORK);
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

    Tuple2<String, String> lpTokens = contractDbService
        .tokenAddressesByUniPairAddress(lpHash, ETH_NETWORK);

    harvestDTO.setLpStat(LpStat.createJson(
        contractDbService.getNameByAddress(lpTokens.component1(), ETH_NETWORK).orElse("unknown"),
        contractDbService.getNameByAddress(lpTokens.component2(), ETH_NETWORK).orElse("unknown"),
        firstCoinBalance,
        secondCoinBalance,
        uniPrices.component1(),
        uniPrices.component2()
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
    input.add(new Web3Model<>(dto, ETH_NETWORK));
  }
}
