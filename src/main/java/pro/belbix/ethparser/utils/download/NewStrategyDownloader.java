package pro.belbix.ethparser.utils.download;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.LoopHandler;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@Service
@Log4j2
public class NewStrategyDownloader {

  private final Web3Functions web3Functions;
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

  private List<String> contracts;

  public NewStrategyDownloader(
      Web3Functions web3Functions,
      VaultActionsDownloader vaultActionsDownloader,
      RewardDownloader rewardDownloader,
      HardWorkDownloader hardWorkDownloader,
      AppProperties appProperties) {
    this.web3Functions = web3Functions;
    this.vaultActionsDownloader = vaultActionsDownloader;
    this.rewardDownloader = rewardDownloader;
    this.hardWorkDownloader = hardWorkDownloader;
    this.appProperties = appProperties;
  }

  public void start() {
    ContractUtils cu = ContractUtils.getInstance(appProperties.getUtilNetwork());
    if (vaults == null) {
      vaults = cu.vaultNames().toArray(new String[]{});
    }
    log.info("Start new vault downloading {}", Arrays.toString(vaults));
    contracts = Arrays.stream(vaults)
        .map(vName -> cu.getAddressByName(vName, ContractType.VAULT).orElseThrow())
        .collect(Collectors.toList());

    new LoopHandler(appProperties.getHandleLoopStep(), this::handle)
        .start(from, to);
  }

  private void handle(Integer start, Integer end) {
    List<LogResult> logResults = web3Functions.fetchContractLogs(
        contracts, start, end, appProperties.getUtilNetwork());
    if (logResults.isEmpty()) {
      log.info("Empty log {} {}", start, end);
      return;
    }
    vaultActionsDownloader.handleLogs(logResults);
    rewardDownloader.handleLogs(logResults);
    hardWorkDownloader.handleLogs(logResults);
  }
}
