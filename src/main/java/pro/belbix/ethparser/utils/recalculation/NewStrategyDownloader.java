package pro.belbix.ethparser.utils.recalculation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.utils.download.HardWorkDownloader;
import pro.belbix.ethparser.utils.download.RewardDownloader;
import pro.belbix.ethparser.utils.download.VaultActionsDownloader;

@Service
@Log4j2
public class NewStrategyDownloader {

  private final VaultActionsDownloader vaultActionsDownloader;
  private final RewardDownloader rewardDownloader;
  private final HardWorkDownloader hardWorkDownloader;
  private final AppProperties appProperties;

  @Value("${new-strategy-download.vaults:}")
  private String[] vaults;
  @Value("${new-strategy-download.from:}")
  private Integer from;
  @Value("${new-strategy-download.to:}")
  private Integer to;

  public NewStrategyDownloader(
      VaultActionsDownloader vaultActionsDownloader,
      RewardDownloader rewardDownloader,
      HardWorkDownloader hardWorkDownloader,
      AppProperties appProperties) {
    this.vaultActionsDownloader = vaultActionsDownloader;
    this.rewardDownloader = rewardDownloader;
    this.hardWorkDownloader = hardWorkDownloader;
    this.appProperties = appProperties;
  }

  public void start() {
    if (vaults == null) {
      vaults = ContractUtils.getInstance(appProperties.getUtilNetwork())
          .vaultNames().toArray(new String[]{});
    }
    vaultActionsDownloader.setContracts(vaults);
    vaultActionsDownloader.setFrom(from);
    vaultActionsDownloader.setTo(to);
    vaultActionsDownloader.start();

    rewardDownloader.setVaultNames(vaults);
    rewardDownloader.setFrom(from);
    rewardDownloader.setTo(to);
    rewardDownloader.start();

    hardWorkDownloader.setFrom(from);
    hardWorkDownloader.setTo(to);
    hardWorkDownloader.start();
  }
}
