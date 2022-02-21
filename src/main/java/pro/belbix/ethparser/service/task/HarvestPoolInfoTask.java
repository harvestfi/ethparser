package pro.belbix.ethparser.service.task;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.Collection;
import java.util.List;
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
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.model.HarvestPoolInfo.HarvestPoolItemInfo;
import pro.belbix.ethparser.repositories.eth.ContractRepository;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
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
public class HarvestPoolInfoTask {
  private static final Long INCREASE_BLOCK_WEIGHT = 1000L;

  HarvestService harvestService;
  ContractLoader contractLoader;
  ContractRepository contractRepository;
  PoolRepository poolRepository;
  EthBlockService ethBlockService;
  FunctionsUtils functionsUtils;
  CovalenthqService covalenthqService;


  // each hour
  @Scheduled(fixedRate = 1000 * 60 * 60)
  public void start() {
    try {
      var response = harvestService.getPools();

      List<CompletableFuture<List<PoolEntity>>> poolFutures = List.of(
          CompletableFuture.supplyAsync(() -> doTaskByAddressAndNetwork(response.getEthereumNetwork(), ETH_NETWORK)),
          CompletableFuture.supplyAsync(() -> doTaskByAddressAndNetwork(response.getMaticNetwork(), MATIC_NETWORK)),
          CompletableFuture.supplyAsync(() -> doTaskByAddressAndNetwork(response.getBscNetwork(), BSC_NETWORK))
      );

      var pools = CompletableFuture.allOf(poolFutures.toArray(new CompletableFuture[0]))
          .thenApply(i ->
              poolFutures.stream().map(CompletableFuture::join)
                  .collect(Collectors.toList())
          )
          .get()
          .stream()
          .flatMap(Collection::stream)
          .collect(Collectors.toList());

      log.info("List of pools: {}", pools);
      poolRepository.saveAll(pools);

    } catch (Exception e) {
      log.error("Error during getting info from harvest", e);
    }
  }

  private List<PoolEntity> doTaskByAddressAndNetwork(List<HarvestPoolItemInfo> items, String network) {
    log.info("Begin find pool and insert in network: {}", network);
    var existPools = contractRepository.findAllByNetworkAndInAddressAndType(network,
        items.stream().map(i -> i.getContractAddress().toLowerCase()).collect(Collectors.toList()), ContractType.POOL.getId());

    var notSavedPools = items.stream()
        .filter(i -> existPools.stream().filter(c -> c.getAddress().equalsIgnoreCase(i.getContractAddress())).findFirst().isEmpty())
        .collect(Collectors.toList());
    log.info("Need insert those {}", notSavedPools);
    return notSavedPools.stream()
        .map(i -> {
          try {
            log.info("Try to create pool {}", i);
            var pool = new PoolEntity();
            var contract = new ContractEntity();
            var block = covalenthqService.getCreatedBlockByLastTransaction(i.getContractAddress(), network);
            var createdBlockDate = ethBlockService.getTimestampSecForBlock(block, network);

            // in some cases created block is incorrect
            var isCorrectBlock = true;
            var name = functionsUtils.callStrByName(FunctionsNames.NAME, i.getContractAddress(), block, network).orElse("");
            if (name.isEmpty()) {
              isCorrectBlock = false;
              name = functionsUtils.callStrByName(FunctionsNames.NAME, i.getContractAddress(), null, network).orElse("");
            }
            contract.setAddress(i.getContractAddress());
            contract.setCreated(block);
            contract.setCreatedDate(createdBlockDate);
            contract.setNetwork(network);
            contract.setName(name);
            contract.setType(ContractType.POOL.getId());
            contract = contractRepository.save(contract);
            pool.setContract(contract);
            if (isCorrectBlock) {
              contractLoader.enrichPool(pool, block, network);
            } else {
              contractLoader.enrichPool(pool, block + INCREASE_BLOCK_WEIGHT, network);
            }
            log.info("Pool: {}", pool);
            return pool;
          } catch (Exception e) {
            log.error("Can not create pool, {}", i, e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
