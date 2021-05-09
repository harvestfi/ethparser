package pro.belbix.ethparser.utils.recalculation;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;

@Service
@Log4j2
public class LpTvlRecalculate {

  private final UniswapRepository uniswapRepository;
  private final UniToHarvestConverter uniToHarvestConverter;
  private final VaultActionsDBService vaultActionsDBService;

  @Value("${lp-tvl-recalculate.from:}")
  private Integer from;

  public LpTvlRecalculate(UniswapRepository uniswapRepository,
      UniToHarvestConverter uniToHarvestConverter,
      VaultActionsDBService vaultActionsDBService) {
    this.uniswapRepository = uniswapRepository;
    this.uniToHarvestConverter = uniToHarvestConverter;
    this.vaultActionsDBService = vaultActionsDBService;
  }

  public void start() {

    List<UniswapDTO> dtos;
    if (from == null) {
      dtos = uniswapRepository.findAllByOrderByBlockDate();
    } else {
      dtos = uniswapRepository.findAllByBlockDateGreaterThanOrderByBlockDate(from);
    }

    for (UniswapDTO dto : dtos) {
      try {
        HarvestDTO harvestDto = uniToHarvestConverter.parse(dto, ETH_NETWORK);

        if (harvestDto != null) {

          boolean success = vaultActionsDBService.saveHarvestDTO(harvestDto);

          if (!success) {
            log.warn("Save failed for " + harvestDto.print());
          }
        }
      } catch (Exception e) {
        log.error("Error " + dto.print(), e);
      }
    }
  }

}
