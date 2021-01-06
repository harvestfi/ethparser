package pro.belbix.ethparser.utils;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.repositories.RewardsRepository;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;

@Service
@Log4j2
public class RewardRecalculate {

    private final RewardsDBService rewardsDBService;
    private final RewardsRepository rewardsRepository;
    private final PriceProvider priceProvider;

    public RewardRecalculate(RewardsDBService rewardsDBService,
                             RewardsRepository rewardsRepository, PriceProvider priceProvider) {
        this.rewardsDBService = rewardsDBService;
        this.rewardsRepository = rewardsRepository;
        this.priceProvider = priceProvider;
    }

    public void start() {
        priceProvider.setUpdateTimeout(0);
        List<RewardDTO> rewards = rewardsRepository.getAllByOrderByBlockDate();
        int count = 0;
        for (RewardDTO dto : rewards) {
            try {
                rewardsDBService.fillApy(dto);
            } catch (Exception e) {
                log.error("Error with " + dto, e);
                break;
            }
            rewardsRepository.save(dto);
            count++;
            if (count % 100 == 0) {
                log.info("Handled " + count);
            }
        }
    }
}
