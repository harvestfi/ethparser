package pro.belbix.ethparser.utils.recalculation;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.repositories.RewardsRepository;
import pro.belbix.ethparser.web3.harvest.db.RewardsDBService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class RewardRecalculate {

    private final RewardsDBService rewardsDBService;
    private final RewardsRepository rewardsRepository;
    private final PriceProvider priceProvider;

    @Value("${rewards-recalculate.from:}")
    private Integer from;
    @Value("${rewards-recalculate.to:}")
    private Integer to;

    public RewardRecalculate(RewardsDBService rewardsDBService,
                             RewardsRepository rewardsRepository, PriceProvider priceProvider) {
        this.rewardsDBService = rewardsDBService;
        this.rewardsRepository = rewardsRepository;
        this.priceProvider = priceProvider;
    }

    public void start() {
        if (from == null) {
            from = 0;
        }
        if (to == null) {
            to = Integer.MAX_VALUE;
        }
        priceProvider.setUpdateBlockDifference(1);
        List<RewardDTO> rewards = rewardsRepository.fetchAllByRange(from, to);
        int count = 0;
        for (RewardDTO dto : rewards) {
            try {
                rewardsDBService.fillApy(dto);
                rewardsDBService.fillWeeklyApy(dto);
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
