package pro.belbix.ethparser.service;

import static pro.belbix.ethparser.utils.CommonUtils.parseLong;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.GET_PRICE_PER_FULL_SHARE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.profit.CovalenthqVaultTransaction;
import pro.belbix.ethparser.repositories.covalenthq.CovalenthqVaultTransactionRepository;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@Service
@Log4j2
public class ProfitService {

  private final static String WITHDRAW_NAME = "Withdraw";

  private final HarvestRepository harvestRepository;
  private final ContractDbService contractDbService;
  private final EthBlockService ethBlockService;
  private final FunctionsUtils functionsUtils;
  private final CovalenthqVaultTransactionRepository covalenthqVaultTransactionRepository;


  public ProfitService(HarvestRepository harvestRepository,
      ContractDbService contractDbService,
      EthBlockService ethBlockService,
      FunctionsUtils functionsUtils,
      CovalenthqVaultTransactionRepository covalenthqVaultTransactionRepository
  ) {
    this.harvestRepository = harvestRepository;
    this.contractDbService = contractDbService;
    this.ethBlockService = ethBlockService;
    this.functionsUtils = functionsUtils;
    this.covalenthqVaultTransactionRepository = covalenthqVaultTransactionRepository;
  }

  public BigDecimal calculateProfit(String address, String network) {
    var transactions = covalenthqVaultTransactionRepository.findAllByOwnerAddressAndNetwork(address, network);
    BigDecimal totalProfit = BigDecimal.ZERO;

    for (CovalenthqVaultTransaction i: transactions) {
      if (i.getContractDecimal() == 0 || i.getSharePrice().equals(BigInteger.ZERO) || i.getTokenPrice() == 0) {
        log.error("Can not calculate profit, incorrect transaction : {}", i);
        throw new IllegalStateException();
      }
      var decimal = new BigDecimal(new BigInteger("10").pow(i.getContractDecimal()).toString());
      var value = i.getValue()
          .divide(decimal)
          .multiply(
              new BigDecimal(i.getSharePrice().toString()).divide(decimal)
          )
          .multiply(BigDecimal.valueOf(i.getTokenPrice()));

      if (i.getType().equals(WITHDRAW_NAME)) {
        totalProfit = totalProfit.add(value);
      } else {
        totalProfit = totalProfit.subtract(value);
      }
    }
    return totalProfit;
  }

  public Double calculationProfitForPeriod(String address, String start, String end) {

    List<HarvestDTO> harvestDTOS = harvestRepository.fetchAllByOwnerWithoutNetwork(
        address.toLowerCase(),
        parseLong(start, 0),
        parseLong(end, Long.MAX_VALUE));

    if (harvestDTOS == null || harvestDTOS.isEmpty()) {
      return 0.0;
    }

    return calculationTotalYield(harvestDTOS.stream()
            .collect(Collectors.groupingBy(HarvestDTO::getVault, Collectors.toList())),
        parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
  }

  private Double calculationTotalYield(Map<String, List<HarvestDTO>> maps,
      Long start, Long end) {

    return maps.values().stream().map(e -> calcYieldByVault(e, start, end))
        .reduce(0.0, Double::sum);
  }


  private Double calcYieldByVault(List<HarvestDTO> v, Long start, Long end) {
    long startBlockNumber = (v.get(0).getBlock() * start) / v.get(0).getBlockDate();
    long endBlockNumber = getEndBlockNumber(v, end);

    String vaultAddress = v.get(0).getVaultAddress();
    String network = v.get(0).getNetwork();
    String owner = v.get(0).getOwner();
    String vault = v.get(0).getVault();

    String poolAddress = contractDbService.getPoolContractByVaultAddress(
            vaultAddress, startBlockNumber, network)
        .orElseThrow().getAddress();

    return getYieldValue(owner, vault, vaultAddress, network, startBlockNumber, endBlockNumber,
        poolAddress, start, end);
  }

  private double getYieldValue(String owner, String vault, String vaultAddress, String network,
      long startBlockNumber, Long endBlockNumber, String poolAddress, Long start, Long end) {

    double underlyingBalanceStart = getUnderlyingBalance(owner, startBlockNumber, vaultAddress,
        network, poolAddress);

    double underlyingBalanceEnd = getUnderlyingBalance(owner, endBlockNumber, vaultAddress,
        network, poolAddress);

    double totalDifferenceDepositAndWithdrawUnderlyingStart =
        harvestRepository.fetchTotalDifferenceDepositAndWithdraw(
            vault, owner, start, network);

    double totalDifferenceDepositAndWithdrawUnderlyingEnd =
        harvestRepository.fetchTotalDifferenceDepositAndWithdraw(
            vault, owner, end, network);

    double yieldEnd = underlyingBalanceEnd + totalDifferenceDepositAndWithdrawUnderlyingEnd;
    double yieldStart = underlyingBalanceStart + totalDifferenceDepositAndWithdrawUnderlyingStart;

    return yieldEnd - yieldStart;
  }

  private double getUnderlyingBalance(String owner, long block,
      String vaultAddress, String network, String poolAddress) {
    return functionsUtils.
        parseAmount(getCoinBalance(block, owner, vaultAddress, network)
                .add(getCoinBalance(block, owner, poolAddress, network)),
            vaultAddress, network)
        * pricePerFullShare(vaultAddress, block, network);
  }

  private double pricePerFullShare(String vaultAddress, Long block, String network) {
    var sharePriceInt = functionsUtils.callIntByName(
            GET_PRICE_PER_FULL_SHARE, vaultAddress, block, network)
        .orElse(BigInteger.ZERO);

    if (BigInteger.ONE.equals(sharePriceInt)) {
      return 0.0;
    }
    return functionsUtils
        .parseAmount(sharePriceInt, vaultAddress, network);

  }

  private BigInteger getCoinBalance(long block, String owner, String address, String network) {
    return functionsUtils.callIntByNameWithAddressArg(BALANCE_OF, owner, address, block,
            network)
        .orElseThrow(() -> new IllegalStateException("Error get amount from " + address));
  }

  public Double calculationProfitByVaultForPeriod(String address, String network, String start,
      String end) {
    List<HarvestDTO> harvestDTOS = harvestRepository.fetchAllByVaultAddressAndNetwork(
        address.toLowerCase(),
        parseLong(start, 0),
        parseLong(end, Long.MAX_VALUE),
        network);

    if (harvestDTOS == null || harvestDTOS.isEmpty()) {
      return 0.0;
    }

    return calculationTotalYield(harvestDTOS.stream()
            .collect(Collectors.groupingBy(HarvestDTO::getVault, Collectors.toList())),
        parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
  }

  private long getEndBlockNumber(List<HarvestDTO> v, Long end) {
    if (end == Long.MAX_VALUE) {
      return ethBlockService.getLastBlock(v.get(0).getNetwork());
    }

    return (v.get(v.size() - 1).getBlock() * end) / v.get(v.size() - 1).getBlockDate();
  }
}
