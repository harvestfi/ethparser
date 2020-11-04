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


        Integer ownerCount = harvestRepository.fetchOwnerCount(dto.getVault());
        if (ownerCount == null) {
            ownerCount = 0;
        }
        dto.setOwnerCount(ownerCount);
        if (harvestRepository.existsById(dto.getId())) {
            log.info("Duplicate Harvest entry " + dto.getId());
            return false;
        }
        harvestRepository.save(dto);
        fillTVL(dto);
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

    private void fillTVL(HarvestDTO dto) {
        Double tvl = harvestRepository.fetchTVL(dto.getVault());
        if (tvl == null) {
            dto.setLastUsdTvl(0.0);
            return;
        }
        dto.setLastTvl(tvl);
    }


}
