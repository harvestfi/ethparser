package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.RewardDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.RewardsRepository;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;

@Service
@Log4j2
public class RewardRecalculate {

  private final RewardsDBService rewardsDBService;
  private final RewardsRepository rewardsRepository;
  private final AppProperties appProperties;

  @Value("${rewards-recalculate.from:}")
  private Integer from;
  @Value("${rewards-recalculate.to:}")
  private Integer to;

  public RewardRecalculate(
      RewardsDBService rewardsDBService,
      RewardsRepository rewardsRepository,
      AppProperties appProperties) {
    this.rewardsDBService = rewardsDBService;
    this.rewardsRepository = rewardsRepository;
    this.appProperties = appProperties;
  }

  public void start() {
    if (from == null) {
      from = 0;
    }
    if (to == null) {
      to = Integer.MAX_VALUE;
    }
    List<RewardDTO> rewards = rewardsRepository
        .fetchAllByRange(from, to, appProperties.getNetwork());
    int count = 0;
    for (RewardDTO dto : rewards) {
      try {
        rewardsDBService.fillApy(dto);
        rewardsDBService.fillWeeklyApy(dto);
      } catch (Exception e) {
        log.error("Error with " + dto, e);
        break;
      }
      rewardsRepository.save(dto);
      count++;
      if (count % 100 == 0) {
        log.info("Handled " + count);
      }
    }
  }
}
