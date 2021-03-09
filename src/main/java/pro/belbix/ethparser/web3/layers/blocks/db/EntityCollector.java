package pro.belbix.ethparser.web3.layers.blocks.db;

import java.util.HashMap;
import java.util.Map;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;

public class EntityCollector {

  private final EthBlockEntity block;
  private final Map<String, EthHashEntity> hashes = new HashMap<>();
  private final Map<String, EthAddressEntity> addresses = new HashMap<>();

  public EntityCollector(EthBlockEntity block) {
    this.block = block;
  }

  public void collectFromBlock() {
    saveHash(block.getHash());
    saveHash(block.getParentHash());
    saveAddress(block.getMiner());

    block.getTransactions().forEach(this::collectFromTx);
  }

  private void collectFromTx(EthTxEntity tx) {
    saveHash(tx.getHash());
    saveAddress(tx.getFromAddress());
    saveAddress(tx.getToAddress());

    tx.getLogs().forEach(this::collectFromLog);
  }

  private void collectFromLog(EthLogEntity log) {
    saveHash(log.getFirstTopic());
  }

  private void saveHash(EthHashEntity hash) {
    if (hash == null || hash.getHash() == null) {
      return;
    }
    hashes.put(hash.getHash(), hash);
  }

  private void saveAddress(EthAddressEntity address) {
    if (address == null || address.getAddress() == null) {
      return;
    }
    addresses.put(address.getAddress(), address);
  }

  public Map<String, EthHashEntity> getHashes() {
    return hashes;
  }

  public Map<String, EthAddressEntity> getAddresses() {
    return addresses;
  }
}
