package pro.belbix.ethparser.service.task;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.service.HarvestVaultDataService;
import pro.belbix.ethparser.service.external.HarvestService;

@Service
@RequiredArgsConstructor
@Slf4j
public class HarvestUpdateInfoTask {
  @Value("${task.info.enable}")
  private Boolean enable;
  private final HarvestService harvestService;
  private final HarvestVaultDataService harvestVaultDataService;

  @Scheduled(fixedRateString = "${task.info.fixedRate}")
  public void start() {
    if (enable == null || !enable) {
      log.info("HarvestUpdateInfoTask disabled");
      return;
    }

    log.info("Start save vaults info from harvest API");
    var response = harvestService.getVaults();

    var vaults = Stream.of(
            response.getEthereumNetwork().values(),
            response.getBscNetwork().values(),
            response.getMaticNetwork().values()
        )
        .flatMap(Collection::stream)
        .collect(Collectors.toList());


    harvestVaultDataService.saveAll(vaults);
    log.info("Success saved vaults info from harvest API");
  }
}
