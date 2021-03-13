package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;

@Service
@Log4j2
public class LpTvlRecalculate {

  private final UniswapRepository uniswapRepository;
  private final UniToHarvestConverter uniToHarvestConverter;
  private final HarvestDBService harvestDBService;

  @Value("${lp-tvl-recalculate.from:}")
  private Integer from;

  public LpTvlRecalculate(UniswapRepository uniswapRepository,
      UniToHarvestConverter uniToHarvestConverter,
      HarvestDBService harvestDBService) {
    this.uniswapRepository = uniswapRepository;
    this.uniToHarvestConverter = uniToHarvestConverter;
    this.harvestDBService = harvestDBService;
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
        HarvestDTO harvestDto = uniToHarvestConverter.convert(dto);

        if (harvestDto != null) {

          boolean success = harvestDBService.saveHarvestDTO(harvestDto);

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
