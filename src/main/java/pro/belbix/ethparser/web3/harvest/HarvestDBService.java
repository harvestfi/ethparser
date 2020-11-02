package pro.belbix.ethparser.web3.harvest;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;

@Service
public class HarvestDBService {

    private static final Logger log = LoggerFactory.getLogger(HarvestDBService.class);
    private final HarvestRepository harvestRepository;

    public HarvestDBService(HarvestRepository harvestRepository) {
        this.harvestRepository = harvestRepository;
    }

    public boolean saveHarvestDTO(HarvestDTO dto) {
        Double tvl = harvestRepository.fetchTVL(dto.getVault());
        if (tvl == null) {
            tvl = 0.0;
        }
        dto.setLastTVL(tvl);
        Integer ownerCount = harvestRepository.fetchOwnerCount(dto.getVault());
        if (ownerCount == null) {
            ownerCount = 0;
        }
        dto.setOwnerCount(ownerCount);
        if (harvestRepository.existsById(dto.getHash())) {
            log.info("Duplicate Harvest entry " + dto.getHash());
            return false;
        }
        harvestRepository.save(dto);
        return true;
    }

    public BigInteger lastBlock() {
        HarvestDTO dto = harvestRepository.findFirstByOrderByBlockDesc();
        if (dto == null) {
            return new BigInteger("0");
        }
        return dto.getBlock();
    }
}
