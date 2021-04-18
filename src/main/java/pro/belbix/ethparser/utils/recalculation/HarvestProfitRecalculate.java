package pro.belbix.ethparser.utils.recalculation;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;

@Service
@Log4j2
public class HarvestProfitRecalculate {

  private final HarvestRepository harvestRepository;
  private final VaultActionsDBService vaultActionsDBService;
  private final AppProperties appProperties;

  @Value("${profit-recalculate.from:}")
  private Integer from;

  public HarvestProfitRecalculate(HarvestRepository harvestRepository,
      VaultActionsDBService vaultActionsDBService,
      AppProperties appProperties) {
    this.harvestRepository = harvestRepository;
    this.vaultActionsDBService = vaultActionsDBService;
    this.appProperties = appProperties;
  }

  public void start() {
    List<HarvestDTO> harvestDTOList;
    log.info("Loading transactions from database");
    if (from == null) {
      from = 0;
    }
    harvestDTOList = harvestRepository
        .findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
            "Withdraw", from, appProperties.getNetwork());

    log.info(
        "Loaded " + harvestDTOList.size() + " Withdraw transactions. Starting recalculation..");
    List<HarvestDTO> results = new ArrayList<>();
    for (HarvestDTO harvestDTO : harvestDTOList) {
      try {
        vaultActionsDBService.fillProfit(harvestDTO);
        if (harvestDTO.getProfit() != null && harvestDTO.getProfit() != 0.0) {
          results.add(harvestDTO);
        }
        if (results.size() % 10 == 0) {
          harvestRepository.saveAll(results);
          log.info("Bunch profits recalculated, last " + harvestDTO.print());
          results.clear();
        }
      } catch (Exception e) {
        log.error("Error saving " + harvestDTO.print(), e);
        break;
      }
    }
    harvestRepository.saveAll(results);
  }
}
