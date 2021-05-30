package pro.belbix.ethparser.service;

import static pro.belbix.ethparser.web3.abi.FunctionsNames.STRATEGY;
import static pro.belbix.ethparser.ws.WsService.STRAT_INFO_TOPIC_NAME;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.StratInfo;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.db.StratInfoDbService;
import pro.belbix.ethparser.web3.harvest.strategy.StratInfoCollector;
import pro.belbix.ethparser.ws.WsService;

@Service
@Log4j2
public class StratInfoGrabber {

  private final StratInfoCollector stratInfoCollector;
  private final StratInfoDbService stratInfoDbService;
  private final ContractDbService contractDbService;
  private final AppProperties appProperties;
  private final FunctionsUtils functionsUtils;
  private final EthBlockService ethBlockService;
  private final WsService wsService;
  private final NetworkProperties networkProperties;

  public StratInfoGrabber(
      StratInfoCollector stratInfoCollector,
      StratInfoDbService stratInfoDbService,
      ContractDbService contractDbService,
      AppProperties appProperties, FunctionsUtils functionsUtils,
      EthBlockService ethBlockService, WsService wsService,
      NetworkProperties networkProperties) {
    this.stratInfoCollector = stratInfoCollector;
    this.stratInfoDbService = stratInfoDbService;
    this.contractDbService = contractDbService;
    this.appProperties = appProperties;
    this.functionsUtils = functionsUtils;
    this.ethBlockService = ethBlockService;
    this.wsService = wsService;
    this.networkProperties = networkProperties;
  }

  @Scheduled(fixedRate = 60 * 60 * 1000)
  public void grab() {
    for (String network : appProperties.getNetworks()) {
      if (!networkProperties.get(network).isGrabStratInfo()) {
        continue;
      }
      List<VaultEntity> vaults = contractDbService.getAllVaults(network);
      for (VaultEntity vault : vaults) {
        try {
          grabStratInfo(vault.getContract().getAddress(), network);
        } catch (Exception e) {
          log.error("Error grab strat info for {}", vault);
          if (appProperties.isStopOnParseError()) {
            System.exit(-1);
          }
        }
      }
    }
  }

  private void grabStratInfo(String vaultAddress, String network) {
    long block = ethBlockService.getLastBlock(network);
    String strategyAddress = functionsUtils.callAddressByName(
        STRATEGY,
        vaultAddress,
        block,
        network
    ).orElseThrow();

    StratInfo stratInfo = stratInfoCollector.collect(strategyAddress, block, network);
    boolean successful = stratInfoDbService.save(stratInfo);
    if (successful) {
      wsService.send(STRAT_INFO_TOPIC_NAME, stratInfo);
    }
  }
}
