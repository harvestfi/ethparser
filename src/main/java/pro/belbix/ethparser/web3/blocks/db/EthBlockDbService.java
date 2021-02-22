package pro.belbix.ethparser.web3.blocks.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import pro.belbix.ethparser.repositories.a_layer.EthLogRepository;
import pro.belbix.ethparser.service.SequenceService;

@Service
@Log4j2
public class EthBlockDbService {

    private static final int MAX_TASKS = 1; // DON'T TURN ON! mt broken
    private final static long MAX_SEQ = 100000;

    private final EthBlockRepository ethBlockRepository;
    private final EthHashRepository ethHashRepository;
    private final EthAddressRepository ethAddressRepository;
    private final EthLogRepository ethLogRepository;
    private final SequenceService sequenceService;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        0,
        MAX_TASKS,
        1L, TimeUnit.SECONDS,
        new SynchronousQueue<>()
    );

    public EthBlockDbService(EthBlockRepository ethBlockRepository,
                             EthHashRepository ethHashRepository,
                             EthAddressRepository ethAddressRepository,
                             EthLogRepository ethLogRepository,
                             SequenceService sequenceService) {
        this.ethBlockRepository = ethBlockRepository;
        this.ethHashRepository = ethHashRepository;
        this.ethAddressRepository = ethAddressRepository;
        this.ethLogRepository = ethLogRepository;
        this.sequenceService = sequenceService;
    }

    public CompletableFuture<EthBlockEntity> save(EthBlockEntity block) {
        if (ethBlockRepository.existsById(block.getNumber())) {
            log.warn("Duplicate eth block " + block.getNumber());
            return null;
        }
        waitFreeExecutors(executor);
        return CompletableFuture.supplyAsync(() -> startWorker(block), executor);
    }

    @Transactional
    EthBlockEntity startWorker(EthBlockEntity block) {
        try {
            Thread.currentThread().setName(block.getNumber() + " block saver");

            AtomicLong seq = new AtomicLong(sequenceService.releaseRange(MAX_SEQ));
            long startSeq = seq.get();
            EntityCollector collector = persistChildEntities(block, seq, startSeq);
            new EntityUpdater(block, collector).update();
            ethBlockRepository.save(block);
            return block;
        } catch (Exception e) {
            log.error("Error handle {}", block.getNumber(), e);
            throw e;
        }
    }

    private void waitFreeExecutors(ThreadPoolExecutor executor) {
        log.debug("Active block executors {}", executor.getActiveCount());
        while (executor.getActiveCount() == MAX_TASKS) {
            log.warn("Block task queue is full, wait a second");
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private EntityCollector persistChildEntities(EthBlockEntity block, AtomicLong seq, long startSeq) {
        EntityCollector collector = new EntityCollector(block);
        collector.collectFromBlock();
        persistHashes(collector.getHashes(), seq, startSeq);
        persistAddresses(collector.getAddresses(), seq, startSeq);
        return collector;
    }

    private void persistHashes(Map<String, EthHashEntity> hashes, AtomicLong seq, long startSeq) {
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
            checkSeq(seq, startSeq);
            notPersistent.add(entry.getValue());
        }
        if (!notPersistent.isEmpty()) {
            ethHashRepository.saveAll(notPersistent);
            ethHashRepository.flush();
        }
    }

    private void persistAddresses(Map<String, EthAddressEntity> addresses, AtomicLong seq, long startSeq) {
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
            checkSeq(seq, startSeq);
            notPersistent.add(entry.getValue());
        }
        if (!notPersistent.isEmpty()) {
            ethAddressRepository.saveAll(notPersistent);
            ethAddressRepository.flush();
        }
    }

    private void checkSeq(AtomicLong seq, long startSeq) {
        if ((seq.get() - startSeq) > MAX_SEQ) {
            throw new IllegalStateException("Sequence more than " + MAX_SEQ);
        }
    }

}
