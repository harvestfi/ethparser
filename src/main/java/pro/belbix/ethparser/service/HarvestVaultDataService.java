package pro.belbix.ethparser.service;


import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.HarvestVaultData;
import pro.belbix.ethparser.model.HarvestVaultInfo.HarvestVaultItemInfo;
import pro.belbix.ethparser.repositories.HarvestVaultDataRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HarvestVaultDataService {
  private final static Map<Integer, String> CHAIN_BY_NETWORK = Map.of(
      137, MATIC_NETWORK,
      1, ETH_NETWORK,
      56, BSC_NETWORK
  );

  HarvestVaultDataRepository harvestVaultDataRepository;

  public List<HarvestVaultData> saveAll(List<HarvestVaultItemInfo> items) {
    return harvestVaultDataRepository.saveAll(
        items.stream()
            .map(this::toHarvestVaultData)
            .collect(Collectors.toList())
    );
  }

  public HarvestVaultData save(HarvestVaultItemInfo harvestVaultInfo) {
    return harvestVaultDataRepository.save(toHarvestVaultData(harvestVaultInfo));
  }

  public List<HarvestVaultData> getAllVaultInfo() {
    return harvestVaultDataRepository.findAll();
  }

  private HarvestVaultData toHarvestVaultData(HarvestVaultItemInfo harvestVaultInfo) {
    return HarvestVaultData.builder()
        .id(harvestVaultInfo.getId())
        .vaultAddress(harvestVaultInfo.getVaultAddress())
        .displayName(harvestVaultInfo.getDisplayName())
        .rewardPool(harvestVaultInfo.getRewardPool())
        .apy(harvestVaultInfo.getEstimatedApy())
        .totalSupply(harvestVaultInfo.getTotalSupply())
        .tvl(harvestVaultInfo.getTotalValueLocked())
        .network(CHAIN_BY_NETWORK.get(harvestVaultInfo.getChain()))
        .build();
  }
}
