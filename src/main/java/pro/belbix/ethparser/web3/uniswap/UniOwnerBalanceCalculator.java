package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.web3.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;

import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class UniOwnerBalanceCalculator {

  private final FunctionsUtils functionsUtils;
  private final PriceProvider priceProvider;
  private final UniswapRepository uniswapRepository;

  public UniOwnerBalanceCalculator(FunctionsUtils functionsUtils, PriceProvider priceProvider,
      UniswapRepository uniswapRepository) {
    this.functionsUtils = functionsUtils;
    this.priceProvider = priceProvider;
    this.uniswapRepository = uniswapRepository;
  }

  public boolean fillBalance(UniswapDTO dto) {
    try {
      if (dto.isLiquidity()) {
        return balanceForLp(dto);
      } else {
        return balanceForFarm(dto);
      }

    } catch (Exception e) {
      log.error("Can't fill balance for " + dto.print(), e);
    }
    return false;
  }

  private boolean balanceForLp(UniswapDTO dto) {
    String lpHash;
    if (dto.getLp() == null) {
      lpHash = ContractUtils.findUniPairForTokens(
          ContractUtils.getAddressByName(dto.getCoin(), ContractType.TOKEN)
              .orElseThrow(
                  () -> new IllegalStateException("Not found address for " + dto.getCoin())),
          ContractUtils.getAddressByName(dto.getOtherCoin(), ContractType.TOKEN)
              .orElseThrow(
                  () -> new IllegalStateException("Not found address for " + dto.getOtherCoin()))
      );
    } else {
      lpHash = ContractUtils.getAddressByName(dto.getLp(), ContractType.UNI_PAIR).orElse(null);
    }
    if (lpHash == null) {
      log.error("Not found vault/lp hash for " + dto.getLp());
      return false;
    }
    BigInteger balanceI = functionsUtils.callIntByName(
        BALANCE_OF, dto.getOwner(), lpHash, dto.getBlock().longValue())
        .orElseThrow(() -> new IllegalStateException("Error get balance from " + lpHash));
    if (balanceI == null) {
      log.warn("Can reach lp balance for " + dto.print());
      return false;
    }
    double balance = parseAmount(balanceI, lpHash);
    dto.setOwnerBalance(balance);

    //fill USD value
    double amountUsd = priceProvider
        .getLpTokenUsdPrice(lpHash, balance, dto.getBlock().longValue());
    dto.setOwnerBalanceUsd(amountUsd);
    return true;
  }

  private boolean balanceForFarm(UniswapDTO dto) {
    List<UniswapDTO> txs = uniswapRepository.fetchAllByOwner(dto.getOwner(), 0, dto.getBlockDate());
    double balance = 0;
    for (UniswapDTO oldTx : txs) {
      if (oldTx.getId().equals(dto.getId())) {
        continue;
      }
      if (oldTx.isBuy()) {
        balance += oldTx.getAmount();
      }
      if (oldTx.isSell()) {
        balance -= oldTx.getAmount();
      }
    }
    if (dto.isBuy()) {
      balance += dto.getAmount();
    }
    if (dto.isSell()) {
      balance -= dto.getAmount();
    }
    dto.setOwnerBalance(balance);
    dto.setOwnerBalanceUsd(balance * dto.getLastPrice());
    return true;
  }
}
