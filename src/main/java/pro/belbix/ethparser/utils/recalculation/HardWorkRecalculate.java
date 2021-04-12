package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.HardWorkRepository;
import pro.belbix.ethparser.web3.harvest.db.HardWorkDbService;

@Service
@Log4j2
public class HardWorkRecalculate {

  private final HardWorkRepository hardWorkRepository;
  private final HardWorkDbService hardWorkDbService;
  private final AppProperties appProperties;

  @Value("${hardwork-recalculate.from:}")
  private Integer from;
  @Value("${hardwork-recalculate.to:}")
  private Integer to;

  public HardWorkRecalculate(HardWorkRepository hardWorkRepository,
      HardWorkDbService hardWorkDbService,
      AppProperties appProperties) {
    this.hardWorkRepository = hardWorkRepository;
    this.hardWorkDbService = hardWorkDbService;
    this.appProperties = appProperties;
  }

  public void start() {
    if (from == null) {
      from = 0;
    }
    if (to == null) {
      to = Integer.MAX_VALUE;
    }
    List<HardWorkDTO> dtos = hardWorkRepository
        .fetchAllInRange(from, to, appProperties.getNetwork());
    for (HardWorkDTO dto : dtos) {
      hardWorkDbService.enrich(dto);
      hardWorkRepository.saveAndFlush(dto);
      log.info("Save hardwork for " + dto.print());
    }
  }


}
