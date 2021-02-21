package pro.belbix.ethparser.web3.blocks.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthHashEntity;
import pro.belbix.ethparser.repositories.a_layer.EthAddressRepository;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.repositories.a_layer.EthHashRepository;
import pro.belbix.ethparser.service.SequenceService;

@Service
@Log4j2
public class EthBlockDbService {

    private final static long MAX_SEQ = 100000;

    private final EthBlockRepository ethBlockRepository;
    private final EthHashRepository ethHashRepository;
    private final EthAddressRepository ethAddressRepository;
    private final SequenceService sequenceService;

    public EthBlockDbService(EthBlockRepository ethBlockRepository,
                             EthHashRepository ethHashRepository,
                             EthAddressRepository ethAddressRepository,
                             SequenceService sequenceService) {
        this.ethBlockRepository = ethBlockRepository;
        this.ethHashRepository = ethHashRepository;
        this.ethAddressRepository = ethAddressRepository;
        this.sequenceService = sequenceService;
    }

    public EthBlockEntity save(EthBlockEntity block) {
        if (ethBlockRepository.existsById(block.getNumber())) {
            log.warn("Duplicate eth block " + block.getNumber());
            return null;
        }
        AtomicLong seq = new AtomicLong(sequenceService.releaseRange(MAX_SEQ));
        long startSeq = seq.get();
        EntityCollector collector = collectEntities(block, seq);
        new EntityUpdater(block, collector.getHashes(), collector.getAddresses()).update();
        if (seq.get() - startSeq > MAX_SEQ) {
            throw new IllegalStateException("Sequence more than " + MAX_SEQ);
        }
        ethBlockRepository.save(block);
        return block;
    }

    private EntityCollector collectEntities(EthBlockEntity block, AtomicLong seq) {
        EntityCollector collector = new EntityCollector(block);
        collector.collectFromBlock();
        persistHashes(collector.getHashes(), seq);
        persistAddresses(collector.getAddresses(), seq);
        return collector;
    }

    @Transactional
    void persistHashes(Map<String, EthHashEntity> hashes, AtomicLong seq) {
        List<EthHashEntity> persistent = ethHashRepository.findAllById(hashes.keySet());
        Map<String, EthHashEntity> persistentMap = persistent.stream()
            .collect(Collectors.toMap(EthHashEntity::getHash, item -> item));
        List<EthHashEntity> notPersistent = new ArrayList<>();
        for (Entry<String, EthHashEntity> entry : hashes.entrySet()) {
            EthHashEntity persist = persistentMap.get(entry.getKey());
            if (persist != null) {
                hashes.put(entry.getKey(), persist);
                continue;
            }
            if (entry.getValue().getHash() == null) {
                log.warn("Empty hash");
                continue;
            }
            entry.getValue().setIndex(seq.incrementAndGet());
            notPersistent.add(entry.getValue());
        }
        if (!notPersistent.isEmpty()) {
            ethHashRepository.saveAll(notPersistent);
            ethHashRepository.flush();
        }
    }

    @Transactional
    void persistAddresses(Map<String, EthAddressEntity> addresses, AtomicLong seq) {
        List<EthAddressEntity> persistent = ethAddressRepository.findAllById(addresses.keySet());
        Map<String, EthAddressEntity> persistentMap = persistent.stream()
            .collect(Collectors.toMap(EthAddressEntity::getAddress, item -> item));
        List<EthAddressEntity> notPersistent = new ArrayList<>();
        for (Entry<String, EthAddressEntity> entry : addresses.entrySet()) {
            EthAddressEntity persist = persistentMap.get(entry.getKey());
            if (persist != null) {
                addresses.put(entry.getKey(), persist);
                continue;
            }
            if (entry.getValue().getAddress() == null) {
                log.warn("Empty address");
                continue;
            }
            entry.getValue().setIndex(seq.incrementAndGet());
            notPersistent.add(entry.getValue());
        }
        if (!notPersistent.isEmpty()) {
            ethAddressRepository.saveAll(notPersistent);
            ethAddressRepository.flush();
        }
    }

}
