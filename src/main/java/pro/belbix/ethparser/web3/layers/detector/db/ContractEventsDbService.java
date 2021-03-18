package pro.belbix.ethparser.web3.layers.detector.db;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.entity.b_layer.LogHexEntity;
import pro.belbix.ethparser.repositories.a_layer.EthAddressRepository;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.repositories.a_layer.EthHashRepository;
import pro.belbix.ethparser.repositories.a_layer.EthTxRepository;
import pro.belbix.ethparser.repositories.b_layer.ContractEventRepository;
import pro.belbix.ethparser.repositories.b_layer.LogHexRepository;

@Service
@Log4j2
public class ContractEventsDbService {

  private final ContractEventRepository contractEventRepository;
  private final EthBlockRepository ethBlockRepository;
  private final LogHexRepository logHexRepository;
  private final EthTxRepository ethTxRepository;
  private final EthHashRepository ethHashRepository;
  private final EthAddressRepository ethAddressRepository;

  public ContractEventsDbService(
      ContractEventRepository contractEventRepository,
      EthBlockRepository ethBlockRepository,
      LogHexRepository logHexRepository,
      EthTxRepository ethTxRepository,
      EthHashRepository ethHashRepository,
      EthAddressRepository ethAddressRepository) {
    this.contractEventRepository = contractEventRepository;
    this.ethBlockRepository = ethBlockRepository;
    this.logHexRepository = logHexRepository;
    this.ethTxRepository = ethTxRepository;
    this.ethHashRepository = ethHashRepository;
    this.ethAddressRepository = ethAddressRepository;
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
      for (ContractLogEntity cLog : tx.getLogs()) {
        cLog.setTopic(saveOrGetLogHex(cLog.getTopic()));
      }
    }
  }

  private LogHexEntity saveOrGetLogHex(LogHexEntity logHex) {
    LogHexEntity logHexPersisted = logHexRepository
        .findById(logHex.getMethodId()).orElse(null);
    if (logHexPersisted == null) {
      logHexPersisted = logHexRepository.save(logHex);
    }
    return logHexPersisted;
  }

  private EthAddressEntity saveOrGetAddress(EthAddressEntity hash) {
    EthAddressEntity hashPersisted = ethAddressRepository
        .findById(hash.getAddress()).orElse(null);
    if (hashPersisted == null) {
      hashPersisted = ethAddressRepository.save(hash);
    }
    return hashPersisted;
  }

  private EthHashEntity saveOrGetHash(EthHashEntity hash) {
    EthHashEntity hashPersisted = ethHashRepository
        .findById(hash.getHash()).orElse(null);
    if (hashPersisted == null) {
      hashPersisted = ethHashRepository.save(hash);
    }
    return hashPersisted;
  }

//  private void persist() {
//    BlockEntityCollector collector = persistChildEntities(block, seq, startSeq);
//    new BlockEntityUpdater(block, collector).update();
//  }

}
