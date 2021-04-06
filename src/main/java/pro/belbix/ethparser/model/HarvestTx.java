package pro.belbix.ethparser.model;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.MethodDecoder.parseAmount;

import java.math.BigInteger;
import lombok.Data;
import org.web3j.abi.datatypes.Address;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Data
public class HarvestTx implements EthTransactionI {

  private long logId;
  private String hash;
  private String methodName;
  private String owner;
  private BigInteger block;
  private String blockHash;
  private BigInteger amount;
  private BigInteger amountIn;
  private Address vault;
  private Address fToken;
  private Address[] addressFromArgs;
  private Address addressFromArgs1;
  private Address addressFromArgs2;
  private BigInteger[] intFromArgs;
  private BigInteger intFromArgs1;
  private BigInteger intFromArgs2;
  private boolean success = false;
  private boolean enriched;
  private boolean migration = false;

  public HarvestDTO toDto() {
    HarvestDTO dto = new HarvestDTO();
    dto.setId(hash + "_" + logId);
    dto.setHash(hash);
    dto.setBlock(block.longValue());
    dto.setVault(ContractUtils.getInstance(ETH_NETWORK).getNameByAddress(vault.getValue())
        .orElseThrow(() -> new IllegalStateException("Not found name for " + vault.getValue()))
    );
    dto.setConfirmed(1);
    dto.setMethodName(methodName);
    dto.setAmount(parseAmount(amount, vault.getValue()));
    if (amountIn != null) {
      dto.setAmountIn(parseAmount(amountIn, fToken.getValue()));
    }
    dto.setOwner(owner);

    enrichMethodDepend(dto);
    return dto;
  }

  private void enrichMethodDepend(HarvestDTO dto) {
    switch (methodName) {
      case "deposit":
      case "withdraw":
        break;
      case "underlyingBalanceWithInvestmentForHolder":
      case "setStrategy":
        break;
      case "setVaultFractionToInvest":
        break;
      case "depositFor":
        break;
      case "withdrawAll":
      case "underlyingBalanceInVault":
      case "underlyingBalanceWithInvestment":
      case "governance":
      case "controller":
      case "underlying":
      case "strategy":
      case "getPricePerFullShare":
      case "doHardWork":
      case "rebalance":
        break;
    }
  }
}
