package pro.belbix.ethparser.web3.harvest.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.repositories.RewardsRepository;

@Service
public class RewardsDBService {

    private static final Logger log = LoggerFactory.getLogger(RewardsDBService.class);
    private final RewardsRepository rewardsRepository;

    public RewardsDBService(RewardsRepository rewardsRepository) {
        this.rewardsRepository = rewardsRepository;
    }

    public boolean saveRewardDTO(RewardDTO dto) {
        if (rewardsRepository.existsById(dto.getId())) {
            log.warn("Duplicate reward " + dto);
        }
        rewardsRepository.save(dto);
        return true;
    }
}
