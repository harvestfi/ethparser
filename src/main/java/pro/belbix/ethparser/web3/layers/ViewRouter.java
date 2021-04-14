package pro.belbix.ethparser.web3.layers;

import static pro.belbix.ethparser.ws.WsService.UNI_PRICES_TOPIC_NAME;
import static pro.belbix.ethparser.ws.WsService.VAULT_ACTIONS_TOPIC_NAME;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.entity.b_layer.LogHashEntity;
import pro.belbix.ethparser.repositories.c_layer.UniPriceViewRepository;
import pro.belbix.ethparser.repositories.c_layer.VaultActionsViewRepository;
import pro.belbix.ethparser.repositories.c_layer.ViewI;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.ws.WsService;

@Service
@Log4j2
public class ViewRouter {

  private final WsService wsService;
  private final UniPriceViewRepository uniPriceViewRepository;
  private final VaultActionsViewRepository vaultActionsViewRepository;

  private final static Sort DESC_SORT = Sort.by("blockNumber").descending();

  public ViewRouter(
      WsService wsService, UniPriceViewRepository uniPriceViewRepository,
      VaultActionsViewRepository vaultActionsViewRepository) {
    this.wsService = wsService;
    this.uniPriceViewRepository = uniPriceViewRepository;
    this.vaultActionsViewRepository = vaultActionsViewRepository;
  }

  public void route(ContractEventEntity event, String network) {

    if (isUniPrice(event, network)) {
      sendToWs(
          uniPriceViewRepository.findByAddresses(
              List.of(event.getContract().getAddress()),
              event.getBlock().getNumber(),
              event.getBlock().getNumber(),
              PageRequest.of(0, 1, DESC_SORT)
          ),
          UNI_PRICES_TOPIC_NAME);

    } else if (isVaultAction(event, network)) {
      sendToWs(
          vaultActionsViewRepository.findByAddresses(
              List.of(event.getContract().getAddress()),
              event.getBlock().getNumber(),
              event.getBlock().getNumber(),
              PageRequest.of(0, 1, DESC_SORT)
          ),
          VAULT_ACTIONS_TOPIC_NAME);
    }

  }

  private void sendToWs(List<? extends ViewI> views, String topic) {
    if (views == null) {
      log.error("Empty view for " + topic);
      return;
    }
    views.forEach(v -> wsService.send(topic, v));
  }

  private boolean isUniPrice(ContractEventEntity event, String network) {
    return event.getTxs().stream()
        .map(ContractTxEntity::getTx)
        .map(EthTxEntity::getNotNullToAddress)
        .map(EthAddressEntity::getAddress)
        .anyMatch(ContractUtils.getInstance(network)::isUniPairAddress)
        &&
        event.getTxs().stream()
            .anyMatch(tx -> tx.getLogs().stream()
                .map(ContractLogEntity::getTopic)
                .map(LogHashEntity::getMethodName)
                .anyMatch("Swap"::equals)
            );
  }

  private boolean isVaultAction(ContractEventEntity event, String network) {
    return event.getTxs().stream()
        .map(ContractTxEntity::getTx)
        .map(EthTxEntity::getNotNullToAddress)
        .map(EthAddressEntity::getAddress)
        .anyMatch(ContractUtils.getInstance(network)::isVaultAddress)
        &&
        event.getTxs().stream()
            .anyMatch(tx -> tx.getLogs().stream()
                .map(ContractLogEntity::getTopic)
                .map(LogHashEntity::getMethodName)
                .anyMatch("Transfer"::equals)
            )
        ;
  }

}
