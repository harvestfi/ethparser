package pro.belbix.ethparser.service;

import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.entity.LayerSeqEntity;
import pro.belbix.ethparser.repositories.LayerSeqRepository;

@Service
public class SequenceService {

    private final LayerSeqRepository layerSeqRepository;
    long seq;

    public SequenceService(LayerSeqRepository layerSeqRepository) {
        this.layerSeqRepository = layerSeqRepository;
    }

    @PostConstruct
    private void init() {
        seq = loadSeq().getSeq();
    }

    private LayerSeqEntity loadSeq() {
        List<LayerSeqEntity> seqL = layerSeqRepository.findAll();
        if (seqL.isEmpty()) {
            LayerSeqEntity seq = new LayerSeqEntity();
            seq.setSeq(0L);
            layerSeqRepository.save(seq);
            return seq;
        }
        return seqL.get(0);
    }

    // todo replace on the DB sequence
    public synchronized long releaseRange(long amount) {
        long oldSeq = seq;
        seq = oldSeq + amount + 1;
        LayerSeqEntity seqEntity = new LayerSeqEntity();
        seqEntity.setSeq(seq);
        layerSeqRepository.deleteAll();
        layerSeqRepository.save(seqEntity);
        return oldSeq;
    }

}
