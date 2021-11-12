package pro.belbix.ethparser.service;

import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.repositories.v0.PriceRepository;

@Log4j2
@Service
public class PriceCleanerService {

  private Integer SECONDS_IN_WEEK  = 604800;

  private final PriceRepository priceRepository;

  public PriceCleanerService(PriceRepository priceRepository) {
    this.priceRepository = priceRepository;
  }

  /*
  Will run every twelve hours
  https://www.freeformatter.com/cron-expression-generator-quartz.html
   */
  @Scheduled(cron="0 0 */12 ? * *")
  public void startPriceCleaner() {
    log.info("-------------------startPriceCleaner--------------- :");
    priceRepository.deleteAllBefore(Instant.now().getEpochSecond()- SECONDS_IN_WEEK);
  }

}

