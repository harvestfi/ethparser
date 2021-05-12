package pro.belbix.ethparser.web3.contracts;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.PoolEntity;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.eth.PoolRepository;
import pro.belbix.ethparser.repositories.eth.VaultRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@Service
@Log4j2
public class ContractUpdater {

  private final AppProperties appProperties;
  private final ContractLoader contractLoader;
  private final ContractDbService contractDbService;
  private final PoolRepository poolRepository;
  private final VaultRepository vaultRepository;
  private final EthBlockService ethBlockService;

  private boolean started = false;

  public ContractUpdater(AppProperties appProperties,
      ContractLoader contractLoader,
      ContractDbService contractDbService,
      PoolRepository poolRepository,
      VaultRepository vaultRepository,
      EthBlockService ethBlockService) {
    this.appProperties = appProperties;
    this.contractLoader = contractLoader;
    this.contractDbService = contractDbService;
    this.poolRepository = poolRepository;
    this.vaultRepository = vaultRepository;
    this.ethBlockService = ethBlockService;
  }

  public synchronized void updateContracts() {
    if (started) {
      return;
    }
    started = true;
    new Thread(() -> {
      for (String network : appProperties.getNetworks()) {
        long block = ethBlockService.getLastBlock(network);
        updateVaults(block, network);
        updatePools(block, network);
      }
      started = false;
    }).start();
  }

  public void checkAndUpdateAddress(String address, long block, String network) {
    if (address == null) {
      return;
    }
    ContractEntity contractEntity =
        contractDbService.getContractByAddress(address, network)
            .orElse(null);
    if (contractEntity == null) {
      return;
    }

    ContractType type = ContractType.valueOfId(contractEntity.getType());
    if (type == ContractType.VAULT) {
      contractDbService.getVaultByAddress(address, network)
          .ifPresent(vault -> {
            contractLoader.enrichVault(vault, block, network);
            vaultRepository.save(vault);
          });
    } else if (type == ContractType.POOL) {
      contractDbService.getPoolByAddress(address, network)
          .ifPresent(pool -> {
            contractLoader.enrichPool(pool, block, network);
            poolRepository.save(pool);
          });
    }
  }


  private void updateVaults(long block, String network) {
    log.info("Start update vaults for {}", network);
    for (VaultEntity vault : contractDbService.getAllVaults(network)) {
      log.debug("Update {}", vault.getContract().getName());
      contractLoader.enrichVault(vault, block, network);
      vaultRepository.save(vault);
    }
  }

  private void updatePools(long block, String network) {
    log.info("Start update pools for {}", network);
    for (PoolEntity pool : contractDbService.getAllPools(network)) {
      log.debug("Update {}", pool.getContract().getName());
      contractLoader.enrichPool(pool, block, network);
      poolRepository.save(pool);
    }
  }
}
