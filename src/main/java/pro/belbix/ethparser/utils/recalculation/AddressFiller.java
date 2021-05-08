package pro.belbix.ethparser.utils.recalculation;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.dto.DtoI;
import pro.belbix.ethparser.model.LpStat;
import pro.belbix.ethparser.repositories.v0.HardWorkRepository;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.RewardsRepository;
import pro.belbix.ethparser.repositories.v0.TransferRepository;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@Service
public class AddressFiller {

  private final ContractDbService contractDbService;
  private final HardWorkRepository hardWorkRepository;
  private final HarvestRepository harvestRepository;
  private final RewardsRepository rewardsRepository;
  private final TransferRepository transferRepository;
  private final UniswapRepository uniswapRepository;

  public AddressFiller(ContractDbService contractDbService,
      HardWorkRepository hardWorkRepository,
      HarvestRepository harvestRepository,
      RewardsRepository rewardsRepository,
      TransferRepository transferRepository,
      UniswapRepository uniswapRepository) {
    this.contractDbService = contractDbService;
    this.hardWorkRepository = hardWorkRepository;
    this.harvestRepository = harvestRepository;
    this.rewardsRepository = rewardsRepository;
    this.transferRepository = transferRepository;
    this.uniswapRepository = uniswapRepository;
  }


  public void start() {
    fillHardWork();
  }

  private void fillHardWork() {
    handle(hardWorkRepository.fetchAllWithoutAddresses(),
        dto -> {
          String vaultAddress = contractDbService
              .getAddressByName(dto.getVault(), ContractType.VAULT, dto.getNetwork())
              .orElseThrow();
          dto.setVaultAddress(vaultAddress);
        },
        hardWorkRepository::saveAll);
  }

  private void fillVaultActions() {
    handle(harvestRepository.fetchAllWithoutAddresses(),
        dto -> {
          String vaultAddress = contractDbService
              .getAddressByName(dto.getVault(), ContractType.VAULT, dto.getNetwork())
              .orElseThrow();
          dto.setVaultAddress(vaultAddress);

          LpStat lpStat;
          try {
            lpStat = ObjectMapperFactory.getObjectMapper()
                .readValue(dto.getLpStat(), LpStat.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          lpStat.setCoin1Address(contractDbService
              .getAddressByName(lpStat.getCoin1(), ContractType.TOKEN, dto.getNetwork())
              .orElseThrow());
          lpStat.setCoin2Address(contractDbService
              .getAddressByName(lpStat.getCoin2(), ContractType.TOKEN, dto.getNetwork())
              .orElseThrow());
          try {
            dto.setLpStat(ObjectMapperFactory.getObjectMapper().writeValueAsString(lpStat));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        },
        harvestRepository::saveAll);
  }

  private void fillRewards() {
    handle(rewardsRepository.fetchAllWithoutAddresses(),
        dto -> {
          String vaultAddress = contractDbService
              .getAddressByName(dto.getVault(), ContractType.VAULT, dto.getNetwork())
              .orElseThrow();
          dto.setVaultAddress(vaultAddress);
        },
        rewardsRepository::saveAll);
  }

  private void fillTransfers() {
    handle(transferRepository.fetchAllWithoutAddresses(),
        dto -> {
          String tokenAddress = contractDbService
              .getAddressByName(dto.getName(), ContractType.TOKEN, dto.getNetwork())
              .orElseThrow();
          dto.setTokenAddress(tokenAddress);
        },
        transferRepository::saveAll);
  }

  private void fillUniswap() {
    handle(uniswapRepository.fetchAllWithoutAddresses(),
        dto -> {
          String coinAddress = contractDbService
              .getAddressByName(dto.getCoinAddress(), ContractType.TOKEN, ETH_NETWORK)
              .orElseThrow();
          String otherCoinAddress = contractDbService
              .getAddressByName(dto.getOtherCoinAddress(), ContractType.TOKEN, ETH_NETWORK)
              .orElseThrow();
          dto.setCoinAddress(coinAddress);
          dto.setOtherCoinAddress(otherCoinAddress);
        },
        uniswapRepository::saveAll);
  }

  private <T extends DtoI> void handle(
      List<T> dtos,
      Consumer<T> handler,
      Consumer<List<T>> saver
  ) {
    List<T> handled = new ArrayList<>();
    for (T dto : dtos) {
      handler.accept(dto);

      handled.add(dto);
      if (handled.size() % 1000 == 0) {
        saver.accept(handled);
        handled.clear();
      }
    }
    saver.accept(handled);
  }

}
