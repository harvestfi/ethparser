package pro.belbix.ethparser.web3.harvest;

import java.math.BigInteger;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;

@Service
public class HarvestDBService {

    private final HarvestRepository harvestRepository;

    public HarvestDBService(HarvestRepository harvestRepository) {
        this.harvestRepository = harvestRepository;
    }

    public void saveHarvestDTO(HarvestDTO dto) {
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
        harvestRepository.save(dto);
    }

    public BigInteger lastBlock() {
        HarvestDTO dto = harvestRepository.findFirstByOrderByBlockDesc();
        if (dto == null) {
            return new BigInteger("0");
        }
        return dto.getBlock();
    }
}
