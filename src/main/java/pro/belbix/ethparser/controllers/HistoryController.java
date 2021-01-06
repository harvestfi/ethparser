package pro.belbix.ethparser.controllers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.RewardDTO;
import pro.belbix.ethparser.repositories.RewardsRepository;

@RestController
@RequestMapping(value = "/history")
@Log4j2
public class HistoryController {

    private final RewardsRepository rewardsRepository;

    public HistoryController(RewardsRepository rewardsRepository) {
        this.rewardsRepository = rewardsRepository;
    }

    @GetMapping(value = "/rewards/{pool}")
    List<RewardDTO> rewardsHistory(@PathVariable("pool") String pool,
                                   @RequestParam(value = "days", required = false) String days) {
        int daysI = 10;
        if(days != null) {
            daysI = Integer.parseInt(days);
        }
        return rewardsRepository.fetchRewardsByVaultAfterBlockDate(pool,
            Instant.now().minus(daysI, ChronoUnit.DAYS).getEpochSecond());
    }

}
