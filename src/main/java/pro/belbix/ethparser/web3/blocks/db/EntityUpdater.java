package pro.belbix.ethparser.web3.blocks.db;

import java.util.Map;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthReceiptEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;

public class EntityUpdater {

    private final EthBlockEntity block;
    private final Map<String, EthHashEntity> hashes;
    private final Map<String, EthAddressEntity> addresses;

    public EntityUpdater(EthBlockEntity block,
                         Map<String, EthHashEntity> hashes,
                         Map<String, EthAddressEntity> addresses) {
        this.block = block;
        this.hashes = hashes;
        this.addresses = addresses;
    }

    public void update() {
        block.setHash(findHash(block.getHash()));
        block.setParentHash(findHash(block.getParentHash()));
        block.setTransactionsRoot(findHash(block.getTransactionsRoot()));
        block.setStateRoot(findHash(block.getStateRoot()));
        block.setReceiptsRoot(findHash(block.getReceiptsRoot()));
        block.setMiner(findAddress(block.getMiner()));
        block.setMixHash(findHash(block.getMixHash()));

        block.getTransactions().forEach(this::persistTransaction);
    }

    private void persistTransaction(EthTxEntity t) {
        t.setHash(findHash(t.getHash()));
        t.setBlockHash(findHash(t.getBlockHash()));
        t.setFromAddress(findAddress(t.getFromAddress()));
        t.setToAddress(findAddress(t.getToAddress()));
        t.setR(findHash(t.getR()));
        t.setS(findHash(t.getS()));

        persistReceipt(t.getReceipt());
    }

    private void persistReceipt(EthReceiptEntity receipt) {
        receipt.setHash(findHash(receipt.getHash()));
        receipt.setBlockHash(findHash(receipt.getBlockHash()));
        receipt.setFromAddress(findAddress(receipt.getFromAddress()));
        receipt.setToAddress(findAddress(receipt.getToAddress()));

        receipt.getLogs().forEach(this::persistLog);
    }

    private void persistLog(EthLogEntity l) {
        l.setHash(findHash(l.getHash()));
        l.setBlockHash(findHash(l.getBlockHash()));
        l.setAddress(findAddress(l.getAddress()));
        l.setFirstTopic(findHash(l.getFirstTopic()));
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
