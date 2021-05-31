package pro.belbix.ethparser.web3.uniswap;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;

import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class UniOwnerBalanceCalculator {
  private final FunctionsUtils functionsUtils;
  private final PriceProvider priceProvider;
  private final UniswapRepository uniswapRepository;
  private final ContractDbService contractDbService;

  public UniOwnerBalanceCalculator(FunctionsUtils functionsUtils, PriceProvider priceProvider,
      UniswapRepository uniswapRepository,
      ContractDbService contractDbService) {
    this.functionsUtils = functionsUtils;
    this.priceProvider = priceProvider;
    this.uniswapRepository = uniswapRepository;
    this.contractDbService = contractDbService;
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
      lpHash = contractDbService.findLpForTokens(
          dto.getCoinAddress(), dto.getOtherCoinAddress(), ETH_NETWORK)
          .map(lp -> lp.getContract().getAddress())
          .orElseThrow();
    } else {
      lpHash = dto.getLpAddress();
    }
    if (lpHash == null) {
      log.error("Not found vault/lp hash for " + dto.getLp());
      return false;
    }
    BigInteger balanceI = functionsUtils.callIntByNameWithAddressArg(
        BALANCE_OF, dto.getOwner(), lpHash, dto.getBlock().longValue(), ETH_NETWORK)
        .orElseThrow(() -> new IllegalStateException("Error get balance from " + lpHash));
    if (balanceI == null) {
      log.warn("Can reach lp balance for " + dto.print());
      return false;
    }
    double balance = functionsUtils.parseAmount(balanceI, lpHash, ETH_NETWORK);
    dto.setOwnerBalance(balance);

    //fill USD value
    double amountUsd = priceProvider
        .getLpTokenUsdPrice(lpHash, balance, dto.getBlock().longValue(), ETH_NETWORK);
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
