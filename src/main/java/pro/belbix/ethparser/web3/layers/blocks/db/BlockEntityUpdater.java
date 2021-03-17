package pro.belbix.ethparser.web3.layers.blocks.db;

import java.util.Map;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;

public class BlockEntityUpdater {

  private final EthBlockEntity block;
  private final Map<String, EthHashEntity> hashes;
  private final Map<String, EthAddressEntity> addresses;

  public BlockEntityUpdater(EthBlockEntity block, BlockEntityCollector collector) {
    this.block = block;
    this.hashes = collector.getHashes();
    this.addresses = collector.getAddresses();
  }

  public void update() {
    block.setHash(findHash(block.getHash()));
    block.setParentHash(findHash(block.getParentHash()));
    block.setMiner(findAddress(block.getMiner()));

    block.getTransactions().forEach(this::persistTransaction);
  }

  private void persistTransaction(EthTxEntity tx) {
    tx.setHash(findHash(tx.getHash()));
    tx.setFromAddress(findAddress(tx.getFromAddress()));
    tx.setToAddress(findAddress(tx.getToAddress()));
    tx.setContractAddress(findAddress(tx.getContractAddress()));

    tx.getLogs().forEach(this::persistLog);
  }

  private void persistLog(EthLogEntity l) {
    l.setFirstTopic(findHash(l.getFirstTopic()));
    l.setAddress(findAddress(l.getAddress()));
  }

  private EthHashEntity findHash(EthHashEntity ethHashEntity) {
    if (ethHashEntity == null || ethHashEntity.getHash() == null) {
      return null;
    }
    return hashes.get(ethHashEntity.getHash());
  }

  private EthAddressEntity findAddress(EthAddressEntity ethAddressEntity) {
    if (ethAddressEntity == null || ethAddressEntity.getAddress() == null) {
      return null;
    }
    return addresses.get(ethAddressEntity.getAddress());
  }

}
