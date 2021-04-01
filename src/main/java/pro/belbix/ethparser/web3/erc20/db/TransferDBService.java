package pro.belbix.ethparser.web3.erc20.db;

import static pro.belbix.ethparser.web3.contracts.EthContractConstants.ZERO_ADDRESS;
import static pro.belbix.ethparser.web3.erc20.TransferType.KEEP_OWNERSHIP;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_BUY;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_SELL;
import static pro.belbix.ethparser.web3.erc20.TransferType.NOT_TRADE;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_EXIT;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_STAKE;
import static pro.belbix.ethparser.web3.erc20.TransferType.REWARD;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.TransferDTO;
import pro.belbix.ethparser.repositories.v0.TransferRepository;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TransferDBService {

  private final TransferDTOComparator transferDTOComparator = new TransferDTOComparator();
  private static final Set<String> notCheckableAddresses = new HashSet<>();

  static {
    notCheckableAddresses.add("0x8f5adC58b32D4e5Ca02EAC0E293D35855999436C"); // st_ps
    notCheckableAddresses.add("0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50"); //ps
    notCheckableAddresses.add("0x59258F4e15A5fC74A7284055A8094F58108dbD4f"); // ps_v0
    notCheckableAddresses.add(ZERO_ADDRESS);
  }

  private final Pageable limitOne = PageRequest.of(0, 1);
  private final Pageable limitAll = PageRequest.of(0, Integer.MAX_VALUE);
  private final TransferRepository transferRepository;
  private final EntityManager entityManager;
  private final PriceProvider priceProvider;

  public TransferDBService(TransferRepository transferRepository, EntityManager entityManager,
      PriceProvider priceProvider) {
    this.transferRepository = transferRepository;
    this.entityManager = entityManager;
    this.priceProvider = priceProvider;
  }

  @Transactional
  public boolean saveDto(TransferDTO dto) {
    if (transferRepository.existsById(dto.getId())) {
      log.warn("Duplicate transfer info " + dto);
      return false;
    }
    entityManager.persist(dto);
    checkBalances(dto);
    fillProfit(dto);
    transferRepository.save(dto);
    return true;
  }

  public boolean checkBalances(TransferDTO dto) {
    return checkBalance(dto.getOwner(), dto.getBalanceOwner(), dto.getBlockDate())
        && checkBalance(dto.getRecipient(), dto.getBalanceRecipient(), dto.getBlockDate());
  }

  public void fillProfit(TransferDTO dto) {
    fillProfitForPs(dto);
    fillProfitForReward(dto);
    fillProfitForTrade(dto);
  }

  private boolean checkBalance(String holder, double expectedBalance, long blockDate) {
    if (notCheckableAddresses.contains(holder.toLowerCase())) {
      return true;
    }
    Double balance = transferRepository.getBalanceForOwner(holder, blockDate);
    if (balance == null) {
      balance = 0.0;
    }
    if (Math.abs(balance - expectedBalance) > 1) {
      log.info("Balance for " + holder + " dbBalance: " + balance + " != " + expectedBalance);
      return false;
    }
    return true;
  }

  private void fillProfitForPs(TransferDTO dto) {
    if (!PS_EXIT.name().equals(dto.getType())) {
      return;
    }
    List<TransferDTO> transfers = transferRepository.fetchAllByOwnerAndRecipient(
        dto.getRecipient(),
        dto.getRecipient(),
        0,
        dto.getBlockDate());
    if (isNotContainsDto(transfers, dto.getId())) {
      transfers.add(dto);
    }
    double profit = calculatePsProfit(transfers);
    dto.setProfit(profit);
    dto.setProfitUsd(profit * dto.getPrice());
  }

  private void fillProfitForReward(TransferDTO dto) {
    if (!REWARD.name().equals(dto.getType())) {
      return;
    }
    double farmProfit = dto.getValue();
    dto.setProfit(farmProfit);
    dto.setProfitUsd(farmProfit * dto.getPrice());
  }

  private void fillProfitForTrade(TransferDTO dto) {
    if (!LP_SELL.name().equals(dto.getType())) {
      return;
    }
    List<TransferDTO> transfers = transferRepository.fetchAllByOwnerAndRecipient(
        dto.getOwner(),
        dto.getOwner(),
        0,
        dto.getBlockDate());
    if (isNotContainsDto(transfers, dto.getId())) {
      transfers.add(dto);
    }
    transfers.sort(transferDTOComparator);
    double profit = calculateSellProfits(transfers, dto.getOwner());
    dto.setProfit(profit);
    dto.setProfitUsd(profit * dto.getPrice());
  }

  private boolean isNotContainsDto(List<TransferDTO> dtos, String id) {
    for (TransferDTO dto : dtos) {
      if (id.equalsIgnoreCase(dto.getId())) {
        return false;
      }
    }
    return true;
  }

  static double calculatePsProfit(List<TransferDTO> transfers) {
    double stacked = 0.0;
    double exits = 0.0;
    double lastProfit = 0.0;
    for (TransferDTO transfer : transfers) {
      lastProfit = 0;
      if (!PS_EXIT.name().equalsIgnoreCase(transfer.getType())
          && !PS_STAKE.name().equalsIgnoreCase(transfer.getType())) {
        continue;
      }

      if (PS_EXIT.name().equalsIgnoreCase(transfer.getType())) {
        exits += transfer.getValue();
      }
      //count all stacked
      if (PS_STAKE.name().equalsIgnoreCase(transfer.getType())) {
        stacked += transfer.getValue();
      }

      // return profit only for last exit, so refresh balances after each full exit
      // it is a shortcut
      // will not work in rare situation when holder has profit more than initial stake amount (impossible I guess)
      if (exits > stacked) {
        lastProfit = exits - stacked;
        stacked = 0;
        exits = 0;
      }
    }
    return lastProfit;
  }

  static double calculateSellProfits(List<TransferDTO> transfers, String owner) {
    double bought = 0;
    double boughtUsd = 0;
    double profit = 0;
    for (int i = 0; i < transfers.size(); i++) {
      TransferDTO transfer = transfers.get(i);
//            //remember how many we bought
      if (LP_BUY.name().equalsIgnoreCase(transfer.getType())) {
        bought += transfer.getValue();
        boughtUsd += transfer.getValue() * transfer.getPrice();
      }

      // count transfers between accounts
      if (NOT_TRADE.contains(transfer.getType()) && KEEP_OWNERSHIP.contains(transfer.getType())) {
        if (owner.equalsIgnoreCase(transfer.getRecipient())) {
          bought += transfer.getValue();
          boughtUsd += transfer.getValue() * transfer.getPrice();
        } else if (owner.equalsIgnoreCase(transfer.getOwner())) {
          bought -= transfer.getValue();
          boughtUsd -= transfer.getValue() * transfer.getPrice();
        } else {
          throw new IllegalStateException("Wrong owner " + owner + " for " + transfer);
        }
      }

      // let's check sells
      if (LP_SELL.name().equalsIgnoreCase(transfer.getType())) {
        if (i == 0) {
          log.error("Wrong sequence");
          continue;
        }
        double sell = transfer.getValue();
        double sellPrice = transfer.getPrice();
        //received tokens sells don't count
        if (bought < 0.01 && bought > -0.01) {
          continue;
        }

        //if we sell more than bought, just skip a part for not bought tokens
        if (sell > bought) {
          sell = bought;
        }

        if (sell <= bought) {
          double rate = (sell / bought);
          bought -= sell; // keep only uncovered amount
          double coveredUsd = boughtUsd * rate;
          boughtUsd -= coveredUsd;
          double sellUsd = sell * sellPrice;
          double sellProfit = sellUsd - coveredUsd;
          if (transfer.getPrice() != 0) {
            transfer.setProfit(
                sellProfit / transfer.getPrice()); // it is synthetic value for compatibility
            transfer.setProfitUsd(sellProfit);
            if (i == transfers.size() - 1) {
              profit = sellProfit / transfer.getPrice();
            }
          }
        }
      }
      if (bought == 0) {
        boughtUsd = 0;
      }
    }
    return profit;
  }

  // used only for recalculation
  public void fillBalances(TransferDTO dto) {
    Double balanceOwner = transferRepository.getBalanceForOwner(dto.getOwner(), dto.getBlockDate());
    if (balanceOwner == null) {
      balanceOwner = 0.0;
    }
    dto.setBalanceOwner(balanceOwner);

    Double balanceRecipient = transferRepository
        .getBalanceForOwner(dto.getRecipient(), dto.getBlockDate());
    if (balanceRecipient == null) {
      balanceRecipient = 0.0;
    }
    dto.setBalanceRecipient(balanceRecipient);
  }

}
