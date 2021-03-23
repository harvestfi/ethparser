package pro.belbix.ethparser.web3.layers.detector.db;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.entity.b_layer.FunctionHashEntity;
import pro.belbix.ethparser.entity.b_layer.LogHexEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.repositories.a_layer.EthTxRepository;
import pro.belbix.ethparser.repositories.b_layer.ContractEventRepository;
import pro.belbix.ethparser.repositories.b_layer.FunctionHashRepository;
import pro.belbix.ethparser.repositories.b_layer.LogHexRepository;

@Service
@Log4j2
public class ContractEventsDbService {

  private final ContractEventRepository contractEventRepository;
  private final EthBlockRepository ethBlockRepository;
  private final LogHexRepository logHexRepository;
  private final EthTxRepository ethTxRepository;
  private final FunctionHashRepository functionHashRepository;

  public ContractEventsDbService(
      ContractEventRepository contractEventRepository,
      EthBlockRepository ethBlockRepository,
      LogHexRepository logHexRepository,
      EthTxRepository ethTxRepository,
      FunctionHashRepository functionHashRepository) {
    this.contractEventRepository = contractEventRepository;
    this.ethBlockRepository = ethBlockRepository;
    this.logHexRepository = logHexRepository;
    this.ethTxRepository = ethTxRepository;
    this.functionHashRepository = functionHashRepository;
  }

  @Transactional
  public ContractEventEntity save(ContractEventEntity event) {
    if (contractEventRepository
        .findByContractAndBlock(event.getContract(), event.getBlock()) != null) {
      log.warn("Duplicate contract event {} on block {}",
          event.getContract().getAddress(), event.getBlock().getNumber());
      return null;
    }
    log.info("Start persist event {} {}",
        event.getContract().getAddress(), event.getBlock().getNumber());

    long blockNumber = event.getBlock().getNumber();
    if (!ethBlockRepository.existsById(blockNumber)) {
      throw new IllegalStateException(
          "Try to save event from not persisted block " + blockNumber);
    }
    persistChildren(event);
    return contractEventRepository.save(event);
  }

  private void persistChildren(ContractEventEntity event) {
    for (ContractTxEntity tx : event.getTxs()) {
      tx.setTx(ethTxRepository.findById(tx.getTx().getId())
          .orElseThrow(() -> new IllegalStateException("Not found tx " + tx.getTx().getId())));
      tx.setFuncHash(saveOrGetFuncHash(tx.getFuncHash()));
      for (ContractLogEntity cLog : tx.getLogs()) {
        cLog.setTopic(saveOrGetLogHex(cLog.getTopic()));
      }
    }
  }

  private FunctionHashEntity saveOrGetFuncHash(FunctionHashEntity funcHash) {
    FunctionHashEntity funcHashPersisted = functionHashRepository
        .findById(funcHash.getMethodId()).orElse(null);
    if (funcHashPersisted == null) {
      funcHashPersisted = functionHashRepository.save(funcHash);
    }
    return funcHashPersisted;
  }

  private LogHexEntity saveOrGetLogHex(LogHexEntity logHex) {
    LogHexEntity logHexPersisted = logHexRepository
        .findById(logHex.getMethodId()).orElse(null);
    if (logHexPersisted == null) {
      logHexPersisted = logHexRepository.save(logHex);
    }
    return logHexPersisted;
  }
}
