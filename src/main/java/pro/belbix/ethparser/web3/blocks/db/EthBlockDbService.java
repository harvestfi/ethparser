package pro.belbix.ethparser.web3.blocks.db;

import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthReceiptEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.repositories.a_layer.EthAddressRepository;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.repositories.a_layer.EthHashRepository;
import pro.belbix.ethparser.repositories.a_layer.EthLogRepository;
import pro.belbix.ethparser.repositories.a_layer.EthReceiptRepository;
import pro.belbix.ethparser.repositories.a_layer.EthTxRepository;

@Service
@Log4j2
public class EthBlockDbService {

    private final EthBlockRepository ethBlockRepository;
    private final EthHashRepository ethHashRepository;
    private final EthAddressRepository ethAddressRepository;
    private final EthTxRepository ethTxRepository;
    private final EthReceiptRepository ethReceiptRepository;
    private final EthLogRepository ethLogRepository;

    public EthBlockDbService(EthBlockRepository ethBlockRepository,
                             EthHashRepository ethHashRepository,
                             EthAddressRepository ethAddressRepository,
                             EthTxRepository ethTxRepository,
                             EthReceiptRepository ethReceiptRepository,
                             EthLogRepository ethLogRepository) {
        this.ethBlockRepository = ethBlockRepository;
        this.ethHashRepository = ethHashRepository;
        this.ethAddressRepository = ethAddressRepository;
        this.ethTxRepository = ethTxRepository;
        this.ethReceiptRepository = ethReceiptRepository;
        this.ethLogRepository = ethLogRepository;
    }

    @Transactional
    public boolean save(EthBlockEntity entity) {
        if (ethBlockRepository.existsById(entity.getNumber())) {
            log.warn("Duplicate eth block " + entity.getNumber());
            return false;
        }

        persistFields(entity);

        ethBlockRepository.save(entity);
        return true;
    }

    // use it only for new block
    // unique fields persistent without checking for optimization
    private void persistFields(EthBlockEntity entity) {
        entity.setHash(ethHashRepository.save(entity.getHash()));
        entity.setParentHash(getOrCreateHash(entity.getParentHash()));
        entity.setTransactionsRoot(getOrCreateHash(entity.getTransactionsRoot()));
        entity.setStateRoot(getOrCreateHash(entity.getStateRoot()));
        entity.setReceiptsRoot(ethHashRepository.save(entity.getReceiptsRoot()));
        entity.setMiner(getOrCreateAddress(entity.getMiner()));
        entity.setMixHash(getOrCreateHash(entity.getMixHash()));

        entity.getTransactions().forEach(this::persistTransaction);
    }

    private void persistTransaction(EthTxEntity t) {
        t.setHash(ethHashRepository.save(t.getHash()));
        t.setBlockHash(getOrCreateHash(t.getBlockHash()));
        t.setFromAddress(getOrCreateAddress(t.getFromAddress()));
        t.setToAddress(getOrCreateAddress(t.getToAddress()));
        t.setR(getOrCreateHash(t.getR()));
        t.setS(getOrCreateHash(t.getS()));

        t.setReceipt(persistReceipt(t.getReceipt()));
//        ethTxRepository.save(t);
    }

    private EthReceiptEntity persistReceipt(EthReceiptEntity receipt) {
        receipt.setHash(getOrCreateHash(receipt.getHash()));
        receipt.setBlockHash(getOrCreateHash(receipt.getBlockHash()));
        receipt.setFromAddress(getOrCreateAddress(receipt.getFromAddress()));
        receipt.setToAddress(getOrCreateAddress(receipt.getToAddress()));

        receipt.getLogs().forEach(this::persistLog);

        return ethReceiptRepository.save(receipt);
    }

    private void persistLog(EthLogEntity l) {
        l.setHash(getOrCreateHash(l.getHash()));
        l.setBlockHash(getOrCreateHash(l.getBlockHash()));
        l.setAddress(getOrCreateAddress(l.getAddress()));
        l.setFirstTopic(getOrCreateHash(l.getFirstTopic()));

        ethLogRepository.save(l);
    }

    private EthHashEntity getOrCreateHash(EthHashEntity ethHashEntity) {
        return Optional.ofNullable(ethHashRepository.findFirstByHash(ethHashEntity.getHash()))
            .orElseGet(() -> ethHashRepository.save(ethHashEntity));
    }

    private EthAddressEntity getOrCreateAddress(EthAddressEntity ethAddressEntity) {
        return Optional.ofNullable(ethAddressRepository.findFirstByAddress(ethAddressEntity.getAddress()))
            .orElseGet(() -> ethAddressRepository.save(ethAddressEntity));
    }

}
