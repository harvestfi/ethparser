package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@Service
@Log4j2
public class OwnerCountRecalculate {

  private final HarvestRepository harvestRepository;
  private final VaultActionsDBService vaultActionsDBService;
  private final UniswapDbService uniswapDbService;
  private final UniswapRepository uniswapRepository;
  private final AppProperties appProperties;

  @Value("${owners-count-recalculate.from:}")
  private Integer from;
  @Value("${owners-count-recalculate.empty:}")
  private Boolean empty;
  @Value("${owners-count-recalculate.hv:true}")
  private boolean hv;
  @Value("${owners-count-recalculate.uni:true}")
  private boolean uni;

  public OwnerCountRecalculate(HarvestRepository harvestRepository,
      VaultActionsDBService vaultActionsDBService,
      UniswapDbService uniswapDbService,
      UniswapRepository uniswapRepository,
      AppProperties appProperties) {
    this.harvestRepository = harvestRepository;
    this.vaultActionsDBService = vaultActionsDBService;
    this.uniswapDbService = uniswapDbService;
    this.uniswapRepository = uniswapRepository;
    this.appProperties = appProperties;
  }

  public void start() {
    int count = 0;
    if (hv) {
      List<HarvestDTO> harvestDTOList;
      if (empty != null) {
        harvestDTOList = harvestRepository.fetchAllWithoutCounts(appProperties.getUtilNetwork());
      } else if (from == null) {
        harvestDTOList = harvestRepository
            .findAllByNetworkOrderByBlockDate(appProperties.getUtilNetwork());
      } else {
        harvestDTOList = harvestRepository
            .findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(from, appProperties.getUtilNetwork());
      }
      for (HarvestDTO harvestDTO : harvestDTOList) {
        vaultActionsDBService.fillOwnersCount(harvestDTO);
        harvestRepository.save(harvestDTO);
        count++;
        if (count % 100 == 0) {
          log.info("Harvest Recalculated " + count + ", last " + harvestDTO.print());
        }
      }
    }

    if (uni) {
      List<UniswapDTO> uniswapDTOS;
      if (from == null) {
        uniswapDTOS = uniswapRepository.findAllByOrderByBlockDate();
      } else {
        uniswapDTOS = uniswapRepository.findAllByBlockDateGreaterThanOrderByBlockDate(from);
      }

      count = 0;
      for (UniswapDTO uniswapDTO : uniswapDTOS) {
        uniswapDbService.fillOwnersCount(uniswapDTO);
        uniswapRepository.save(uniswapDTO);
        count++;
        if (count % 100 == 0) {
          log.info("Uniswap Recalculated " + count + ", last " + uniswapDTO.print());
        }
      }
    }
  }
}
