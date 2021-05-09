package pro.belbix.ethparser.utils.recalculation;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
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
    fillVaultActions();
    fillRewards();
    fillTransfers();
    fillUniswap();
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
          try {
            String vaultAddress = contractDbService
                .getAddressByName(dto.getVault(), ContractType.VAULT, dto.getNetwork())
                .orElse(null);
            if (vaultAddress == null) {
              vaultAddress = contractDbService
                  .getAddressByName(dto.getVault(), ContractType.UNI_PAIR, ETH_NETWORK)
                  .orElseThrow(
                      () -> new IllegalStateException("Not found address for " + dto.getVault())
                  );
            }
            dto.setVaultAddress(vaultAddress);

            if (dto.getLpStat() != null) {
              LpStat lpStat;
              lpStat = ObjectMapperFactory.getObjectMapper()
                  .readValue(dto.getLpStat(), LpStat.class);
              lpStat.setCoin1Address(getTokenAddress(lpStat.getCoin1(), dto.getNetwork()));
              lpStat.setCoin2Address(getTokenAddress(lpStat.getCoin2(), dto.getNetwork()));
              dto.setLpStat(ObjectMapperFactory.getObjectMapper().writeValueAsString(lpStat));
            }
          } catch (Exception e) {
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
              .orElse(null);
          if (vaultAddress == null) {
            vaultAddress = contractDbService
                .getAddressByName(dto.getVault(), ContractType.UNI_PAIR, ETH_NETWORK)
                .orElseThrow(
                    () -> new IllegalStateException("Not found address for " + dto.getVault())
                );
          }
          dto.setVaultAddress(vaultAddress);
        },
        rewardsRepository::saveAll);
  }

  private void fillTransfers() {
    handle(transferRepository.fetchAllWithoutAddresses(),
        dto -> {
          String tokenAddress = getTokenAddress(dto.getName(), dto.getNetwork());
          dto.setTokenAddress(tokenAddress);
        },
        transferRepository::saveAll);
  }

  private void fillUniswap() {
    handle(uniswapRepository.fetchAllWithoutAddresses(),
        dto -> {
          String coinAddress = getTokenAddress(dto.getCoin(), ETH_NETWORK);
          String otherCoinAddress = getTokenAddress(dto.getOtherCoin(), ETH_NETWORK);
          dto.setCoinAddress(coinAddress);
          dto.setOtherCoinAddress(otherCoinAddress);
        },
        uniswapRepository::saveAll);
  }

  private String getTokenAddress(String _name, String network) {
    String name = _name;
    if ("WETH".equals(name)) {
      name = "ETH";
    } else if ("MEME20_ETH".equals(name)) {
      name = "MEME20";
    } else if ("WBTC_KBTC".equals(name)) {
      name = "KBTC";
    } else if ("WBTC_KLON".equals(name)) {
      name = "KLON";
    }
    return contractDbService
        .getAddressByName(name, ContractType.TOKEN, network)
        .orElseThrow(
            () -> new IllegalStateException("Not found address for " + _name)
        );
  }

  private <T extends DtoI> void handle(
      List<T> dtos,
      Consumer<T> handler,
      Consumer<List<T>> saver
  ) {
    log.info("Start handling, size {}", dtos.size());
    List<T> handled = new ArrayList<>();
    int count = 0;
    Instant start = Instant.now();
    for (T dto : dtos) {
      try {
        handler.accept(dto);
      } catch (Exception e) {
        log.info("Error handle {}", dto, e);
        continue;
      }

      handled.add(dto);
      count++;
      if (handled.size() % 100 == 0) {
        log.info("Save butch {} {} for {}, total {}",
            dto.getClass().getSimpleName(), handled.size(),
            Duration.between(start, Instant.now()), count);
        saver.accept(handled);
        handled.clear();
      }
    }
    saver.accept(handled);
  }

}
