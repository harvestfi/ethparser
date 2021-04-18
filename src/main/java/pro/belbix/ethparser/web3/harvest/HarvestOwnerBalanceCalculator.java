package pro.belbix.ethparser.web3.harvest;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.BALANCE_OF;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.UNDERLYING;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.FARM_TOKEN;

import java.math.BigInteger;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class HarvestOwnerBalanceCalculator {
  private final FunctionsUtils functionsUtils;
  private final PriceProvider priceProvider;

  public HarvestOwnerBalanceCalculator(FunctionsUtils functionsUtils, PriceProvider priceProvider) {
    this.functionsUtils = functionsUtils;
    this.priceProvider = priceProvider;
  }

  public boolean fillBalance(HarvestDTO dto, String network) {
    try {
      if (ContractUtils.getInstance(network).isVaultName(dto.getVault())) {
        if (ContractUtils.getInstance(network).isPsName(dto.getVault())) {
          return balanceForPs(dto, network);
        }
        return balanceForVault(dto, network);
      } else {
        return balanceForNonVaultLp(dto, network);
      }
    } catch (Exception e) {
      log.error("Can't fill balance for " + dto.print(), e);
    }
    return false;
  }

  private boolean balanceForPs(HarvestDTO dto, String network) {
    String psHash = ContractUtils.getInstance(network).getAddressByName(dto.getVault(), ContractType.VAULT)
        .orElseThrow(() -> new IllegalStateException("Not found address by " + dto.getVault()));
    BigInteger balanceI = functionsUtils.callIntByName(
        BALANCE_OF, dto.getOwner(), psHash, dto.getBlock(), network)
        .orElseThrow(() -> new IllegalStateException("Error get balance from " + psHash));
    if (balanceI == null) {
      log.warn("Can reach ps balance for " + dto.print());
      return false;
    }
    double balance = ContractUtils.getInstance(network).parseAmount(balanceI, psHash);
    dto.setOwnerBalance(balance);

    double price = priceProvider.getPriceForCoin(FARM_TOKEN, dto.getBlock(), network);
    dto.setOwnerBalanceUsd(balance * price);
    return true;
  }

  private boolean balanceForVault(HarvestDTO dto, String network) {
    long block = dto.getBlock();
    String vaultHash = ContractUtils.getInstance(network).getAddressByName(dto.getVault(), ContractType.VAULT)
        .orElseThrow(() -> new IllegalStateException("Not found address by " + dto.getVault()));
    BigInteger balanceI;
    if (dto.isMigrated()) {
      //migration process broken UnderlyingBalance for vault
      //but we have shortcut - after migration we can check balanceOf
      String stHash = ContractUtils.getInstance(network).poolByVaultAddress(vaultHash)
          .orElseThrow(() -> new IllegalStateException("Not found st for " + dto.getVault()))
          .getContract().getAddress();
      balanceI = functionsUtils.callIntByName(BALANCE_OF, dto.getOwner(), stHash, block, network)
          .orElse(null);
    } else {
      balanceI = functionsUtils.callIntByName("underlyingBalanceWithInvestmentForHolder",
          dto.getOwner(), vaultHash, block, network)
          .orElse(null);
    }
    if (balanceI == null) {
      log.warn("Can reach vault balance for " + dto.print());
      //maybe strategy disabled? try balanceOf
      balanceI = functionsUtils.callIntByName(BALANCE_OF, dto.getOwner(), vaultHash, block, network)
          .orElseThrow(() -> new IllegalStateException("Error get balance from " + vaultHash));
      if (balanceI == null) {
        return false;
      }
    }

    double balance = ContractUtils.getInstance(network).parseAmount(balanceI, vaultHash);
    if (balance == 0
        && dto.getAmount() != 0
        && "Deposit".equals(dto.getMethodName())) {
      log.info("Zero balance for deposit, assume owner is external contract");
      //todo investigate how to determinate the balance for contracts
    }
    dto.setOwnerBalance(balance);

    //fill USD value
    String underlyingAddress =
        functionsUtils.callAddressByName(UNDERLYING, vaultHash, dto.getBlock(), network)
        .orElseThrow(
            () -> new IllegalStateException("Can't fetch underlying token for " + vaultHash));
    if (ContractUtils.getInstance(network).isLp(underlyingAddress)) {
      if (underlyingAddress == null) {
        throw new IllegalStateException("Not found lp hash for " + vaultHash);
      }
      double amountUsd = priceProvider
          .getLpTokenUsdPrice(underlyingAddress, balance, block, network);
      dto.setOwnerBalanceUsd(amountUsd);
    } else {
      double price = priceProvider.getPriceForCoin(underlyingAddress, block, network);
      dto.setOwnerBalanceUsd(balance * price);
    }
    return true;
  }

  private boolean balanceForNonVaultLp(HarvestDTO dto, String network) {
    String lpHash = ContractUtils.getInstance(network).getAddressByName(dto.getVault(), ContractType.VAULT).orElse(null);
    if (lpHash == null) {
      log.error("Not found vault/lp hash for " + dto.getVault());
      return false;
    }
    BigInteger balanceI = functionsUtils.callIntByName(
        BALANCE_OF, dto.getOwner(), lpHash, dto.getBlock(), network)
        .orElseThrow(() -> new IllegalStateException("Error get balance from " + lpHash));
    if (balanceI == null) {
      log.warn("Can reach lp balance for " + dto.print());
      return false;
    }
    double balance = ContractUtils.getInstance(network).parseAmount(balanceI, lpHash);
    dto.setOwnerBalance(balance);

    //fill USD value
    double amountUsd = priceProvider
        .getLpTokenUsdPrice(lpHash, balance, dto.getBlock(), network);
    dto.setOwnerBalanceUsd(amountUsd);
    return true;
  }

}
