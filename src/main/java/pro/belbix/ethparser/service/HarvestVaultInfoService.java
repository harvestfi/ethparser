package pro.belbix.ethparser.service;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.model.HarvestVaultInfo.HarvestVaultItemInfo;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.service.external.CovalenthqService;
import pro.belbix.ethparser.service.external.HarvestService;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionsNames;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractType;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HarvestVaultInfoService {
  private static final Long INCREASE_BLOCK_WEIGHT = 1000L;

  HarvestService harvestService;
  ContractLoader contractLoader;
  ContractRepository contractRepository;
  VaultRepository vaultRepository;
  EthBlockService ethBlockService;
  FunctionsUtils functionsUtils;
  CovalenthqService covalenthqService;


  // each hour
  @Scheduled(fixedRate = 1000 * 60 * 60)
  public void start() {
    try {
      var response = harvestService.getVaults();

      List<CompletableFuture<List<VaultEntity>>> vaultFutures = List.of(
          CompletableFuture.supplyAsync(() -> doTaskByAddressAndNetwork(response.getEthereumNetwork(), ETH_NETWORK)),
          CompletableFuture.supplyAsync(() -> doTaskByAddressAndNetwork(response.getMaticNetwork(), MATIC_NETWORK)),
          CompletableFuture.supplyAsync(() -> doTaskByAddressAndNetwork(response.getBscNetwork(), BSC_NETWORK))
      );

      var vaults = CompletableFuture.allOf(vaultFutures.toArray(new CompletableFuture[0]))
          .thenApply(i ->
              vaultFutures.stream().map(CompletableFuture::join)
                  .collect(Collectors.toList())
          )
          .get()
          .stream()
          .flatMap(Collection::stream)
          .collect(Collectors.toList());

      log.info("List of vaults: {}", vaults);
      vaultRepository.saveAll(vaults);

    } catch (Exception e) {
      log.error("Error during getting info from harvest", e);
    }
  }

  private List<VaultEntity> doTaskByAddressAndNetwork(Map<String, HarvestVaultItemInfo> items, String network) {
    log.info("Begin find vault and insert in network: {}", network);
    var existVaults = contractRepository.findAllByNetworkAndInAddressAndType(network,
        items.values().stream().map(i -> i.getVaultAddress().toLowerCase()).collect(Collectors.toList()), ContractType.VAULT.getId());

    var notSavedVaults = items.values().stream()
        .filter(i -> existVaults.stream().filter(c -> c.getAddress().equalsIgnoreCase(i.getVaultAddress())).findFirst().isEmpty())
        .collect(Collectors.toList());
    log.info("Need insert those {}", notSavedVaults);
    return notSavedVaults.stream()
        .map(i -> {
          try {
            log.info("Try to create pool {}", i);
            var vault = new VaultEntity();
            var contract = new ContractEntity();
            var block = covalenthqService.getCreatedBlockByLastTransaction(i.getVaultAddress(), network);
            var createdBlockDate = ethBlockService.getTimestampSecForBlock(block, network);
            // in some cases created block is incorrect
            var isCorrectBlock = true;
            var name = functionsUtils.callStrByName(FunctionsNames.NAME, i.getVaultAddress(), block, network).orElse("");
            if (name.isEmpty()) {
              isCorrectBlock = false;
              name = functionsUtils.callStrByName(FunctionsNames.NAME, i.getVaultAddress(), null, network).orElse("");
            }
            contract.setAddress(i.getVaultAddress());
            contract.setCreated(block);
            contract.setCreatedDate(createdBlockDate);
            contract.setNetwork(network);
            contract.setName(name);
            contract.setType(ContractType.VAULT.getId());
            contract = contractRepository.save(contract);
            vault.setContract(contract);

            if (isCorrectBlock) {
              contractLoader.enrichVault(vault, block, network);
            } else {
              contractLoader.enrichVaultWithLatestBlock(vault, block + INCREASE_BLOCK_WEIGHT, network);
            }
            log.info("Vault: {}", vault);
            return vault;

          } catch (Exception e) {
            log.error("Can not create pool, {}", i, e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

}
