package pro.belbix.ethparser.utils.recalculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.v0.HarvestTvlEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.HarvestTvlRepository;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;

@Service
@Log4j2
public class TvlRecalculate {

  private final HarvestRepository harvestRepository;
  private final HarvestTvlRepository harvestTvlRepository;
  private final VaultActionsDBService vaultActionsDBService;
  private final AppProperties appProperties;

  @Value("${tvl-recalculate.from:}")
  private Integer from;
  @Value("${tvl-recalculate.createNew:false}")
  private boolean createNew;
  @Value("${tvl-recalculate.recalculate:false}")
  private boolean recalculate;

  public TvlRecalculate(HarvestRepository harvestRepository,
      HarvestTvlRepository harvestTvlRepository,
      VaultActionsDBService vaultActionsDBService,
      AppProperties appProperties) {
    this.harvestRepository = harvestRepository;
    this.harvestTvlRepository = harvestTvlRepository;
    this.vaultActionsDBService = vaultActionsDBService;
    this.appProperties = appProperties;
  }

  public void start() {
    if (createNew) {
      createNew();
    } else if (recalculate) {
      recalculate();
    }
  }

  private void createNew() {
    List<HarvestDTO> harvestDTOList;
    if (from == null) {
      harvestDTOList = harvestRepository
          .findAllByNetworkOrderByBlockDate(appProperties.getUtilNetwork());
    } else {
      harvestDTOList = harvestRepository
          .findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(
              from, appProperties.getUtilNetwork());
    }
    int count = 0;
    List<HarvestTvlEntity> tvls = new ArrayList<>();
    for (HarvestDTO harvestDTO : harvestDTOList) {
      count++;
      HarvestTvlEntity tvl = vaultActionsDBService.calculateHarvestTvl(harvestDTO, false);
      tvls.add(tvl);
      if (count % 100 == 0) {
        harvestTvlRepository.saveAll(tvls);
        log.info("Save for " + harvestDTO.print());
        tvls.clear();
      }
    }
    harvestTvlRepository.saveAll(tvls);
  }

  private void recalculate() {
    List<HarvestDTO> harvestDTOs = harvestRepository
        .findAllByNetworkOrderByBlockDate(appProperties.getUtilNetwork());
    List<HarvestTvlEntity> harvestTvlEntities =
        harvestTvlRepository.findAllByNetworkOrderByCalculateTime(appProperties.getUtilNetwork());

    Map<String, HarvestTvlEntity> harvestTvlEntitiesMap = new HashMap<>();

    harvestTvlEntities.forEach(t -> harvestTvlEntitiesMap.put(t.getCalculateHash(), t));
    int count = 0;
    List<HarvestTvlEntity> tvls = new ArrayList<>();
    for (HarvestDTO harvestDTO : harvestDTOs) {
      HarvestTvlEntity harvestTvlEntity = harvestTvlEntitiesMap.get(harvestDTO.getId());
      // we used hash as id, replace it
      if (harvestTvlEntity == null) {
        harvestTvlEntity = harvestTvlEntitiesMap.get(harvestDTO.getHash());
        if (harvestTvlEntity == null) {
          log.error("Not found harvest tvl for " + harvestDTO.print());
          continue;
        }
        harvestTvlEntity.setCalculateHash(harvestDTO.getId());
      }
      // todo type of recalculations
      vaultActionsDBService.fillSimpleDataFromDto(harvestDTO, harvestTvlEntity);

      tvls.add(harvestTvlEntity);
      count++;
      if (count % 100 == 0) {
        harvestTvlRepository.saveAll(tvls);
        log.info("Save for " + harvestDTO.print());
        tvls.clear();
      }
    }
    harvestTvlRepository.saveAll(tvls);
  }
}
