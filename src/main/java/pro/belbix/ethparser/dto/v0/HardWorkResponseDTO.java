package pro.belbix.ethparser.dto.v0;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HardWorkResponseDTO extends HardWorkDTO {

  private Double weeklyAverageTvl;
  private long periodOfWork;
  private long psPeriodOfWork;
  private double tvl;

  public HardWorkResponseDTO(HardWorkDTO hardWorkDTO) {

    id = hardWorkDTO.id;
    vault = hardWorkDTO.vault;
    vaultAddress = hardWorkDTO.vaultAddress;
    block = hardWorkDTO.block;
    blockDate = hardWorkDTO.blockDate;
    network = hardWorkDTO.network;
    shareChange = hardWorkDTO.shareChange;
    fullRewardUsd = hardWorkDTO.fullRewardUsd;
    farmBuyback = hardWorkDTO.farmBuyback;
    fee = hardWorkDTO.fee;
    farmBuybackEth = hardWorkDTO.farmBuybackEth;
    gasUsed = hardWorkDTO.gasUsed;
    invested = hardWorkDTO.invested;
    investmentTarget = hardWorkDTO.investmentTarget;
    farmPrice = hardWorkDTO.farmPrice;
    ethPrice = hardWorkDTO.ethPrice;
    profitSharingRate = hardWorkDTO.profitSharingRate;
    buyBackRate = hardWorkDTO.buyBackRate;
    autoStake = hardWorkDTO.autoStake;

    idleTime = hardWorkDTO.idleTime;
    feeEth = hardWorkDTO.feeEth;
    savedGasFeesSum = hardWorkDTO.savedGasFeesSum;
    savedGasFees = hardWorkDTO.savedGasFees;
    poolUsers = hardWorkDTO.poolUsers;
    callsQuantity = hardWorkDTO.callsQuantity;
    farmBuybackSum = hardWorkDTO.farmBuybackSum;
    psApr = hardWorkDTO.psApr;
    psTvlUsd = hardWorkDTO.psTvlUsd;
    weeklyProfit = hardWorkDTO.weeklyProfit;

    weeklyAllProfit = hardWorkDTO.weeklyAllProfit;
    apr = hardWorkDTO.apr;
    perc = hardWorkDTO.perc;
    fullRewardUsdTotal = hardWorkDTO.fullRewardUsdTotal;
    allProfit = hardWorkDTO.allProfit;

  }

}
