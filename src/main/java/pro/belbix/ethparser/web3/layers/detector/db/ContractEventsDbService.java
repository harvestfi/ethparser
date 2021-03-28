package pro.belbix.ethparser.web3.layers.detector.db;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.entity.b_layer.FunctionHashEntity;
import pro.belbix.ethparser.entity.b_layer.LogHashEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.repositories.a_layer.EthTxRepository;
import pro.belbix.ethparser.repositories.b_layer.ContractEventRepository;
import pro.belbix.ethparser.repositories.b_layer.ContractTxRepository;
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
  private final ContractTxRepository contractTxRepository;

  public ContractEventsDbService(
      ContractEventRepository contractEventRepository,
      EthBlockRepository ethBlockRepository,
      LogHexRepository logHexRepository,
      EthTxRepository ethTxRepository,
      FunctionHashRepository functionHashRepository,
      ContractTxRepository contractTxRepository) {
    this.contractEventRepository = contractEventRepository;
    this.ethBlockRepository = ethBlockRepository;
    this.logHexRepository = logHexRepository;
    this.ethTxRepository = ethTxRepository;
    this.functionHashRepository = functionHashRepository;
    this.contractTxRepository = contractTxRepository;
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
    return contractEventRepository.saveAndFlush(event);
  }

  private void persistChildren(ContractEventEntity event) {
    Set<ContractTxEntity> persistedTxs = new LinkedHashSet<>();
    for (ContractTxEntity tx : event.getTxs()) {
      tx.setTx(ethTxRepository.findById(tx.getTx().getId())
          .orElseThrow(() -> new IllegalStateException(
              "Not found tx " + tx.getTx().getId())));
      persistedTxs.add(saveOrGetContractTx(tx));
    }
    event.setTxs(persistedTxs);
  }

  private ContractTxEntity saveOrGetContractTx(ContractTxEntity contractTx) {
    if (contractTx == null) {
      return null;
    }
    ContractTxEntity contractTxPersisted = contractTxRepository
        .findFirstByTx(contractTx.getTx());
    if (contractTxPersisted == null) {
      contractTx.setFuncHash(saveOrGetFuncHash(contractTx.getFuncHash()));
      for (ContractLogEntity cLog : contractTx.getLogs()) {
        cLog.setTopic(saveOrGetLogHex(cLog.getTopic()));
      }
      return contractTxRepository.save(contractTx);
    }
    return contractTxPersisted;
  }

  private FunctionHashEntity saveOrGetFuncHash(FunctionHashEntity funcHash) {
    if (funcHash == null) {
      return null;
    }
    FunctionHashEntity funcHashPersisted = functionHashRepository
        .findById(funcHash.getMethodId()).orElse(null);
    if (funcHashPersisted == null) {
      funcHashPersisted = functionHashRepository.save(funcHash);
    }
    return funcHashPersisted;
  }

  private LogHashEntity saveOrGetLogHex(LogHashEntity logHex) {
    if (logHex == null) {
      return null;
    }
    LogHashEntity logHexPersisted = logHexRepository
        .findById(logHex.getMethodId()).orElse(null);
    if (logHexPersisted == null) {
      logHexPersisted = logHexRepository.save(logHex);
    }
    return logHexPersisted;
  }
}
