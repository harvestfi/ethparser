package pro.belbix.ethparser.web3.layers;

import static pro.belbix.ethparser.ws.WsService.UNI_PRICES_TOPIC_NAME;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.entity.b_layer.LogHashEntity;
import pro.belbix.ethparser.entity.contracts.ContractEntity;
import pro.belbix.ethparser.entity.contracts.UniPairEntity;
import pro.belbix.ethparser.repositories.c_layer.UniPriceViewRepository;
import pro.belbix.ethparser.repositories.c_layer.ViewI;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.ws.WsService;

@Service
@Log4j2
public class ViewRouter {

  private final WsService wsService;
  private final UniPriceViewRepository uniPriceViewRepository;

  public ViewRouter(
      WsService wsService, UniPriceViewRepository uniPriceViewRepository) {
    this.wsService = wsService;
    this.uniPriceViewRepository = uniPriceViewRepository;
  }

  public void route(ContractEventEntity event) {

    if (isUniPrice(event)) {
      sendToWs(
          uniPriceViewRepository.findByAddressesAndLogNames(
              ContractUtils.getAllUniPairs().stream()
                  .map(UniPairEntity::getContract)
                  .map(ContractEntity::getAddress)
                  .collect(Collectors.toList()),
              List.of("Swap"),
              event.getBlock().getNumber(),
              event.getBlock().getNumber(),
              PageRequest.of(0, Integer.MAX_VALUE)
          ),
          UNI_PRICES_TOPIC_NAME);

    }

  }

  private void sendToWs(List<? extends ViewI> views, String topic) {
    if (views == null) {
      return;
    }
    views.forEach(v -> wsService.send(topic, v));
  }

  private boolean isUniPrice(ContractEventEntity event) {
    return event.getTxs().stream()
        .map(ContractTxEntity::getTx)
        .map(EthTxEntity::getToAddress)
        .map(EthAddressEntity::getAddress)
        .anyMatch(ContractUtils::isUniPairAddress)
        &&
        event.getTxs().stream()
            .anyMatch(tx -> tx.getLogs().stream()
                .map(ContractLogEntity::getTopic)
                .map(LogHashEntity::getMethodName)
                .anyMatch("Swap"::equals)
            );
  }

}
