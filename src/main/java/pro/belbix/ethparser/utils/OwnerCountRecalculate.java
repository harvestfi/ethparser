package pro.belbix.ethparser.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;

@Service
@Log4j2
public class OwnerCountRecalculate {

    private final HarvestRepository harvestRepository;
    private final HarvestDBService harvestDBService;

    public OwnerCountRecalculate(HarvestRepository harvestRepository,
                                 HarvestDBService harvestDBService) {
        this.harvestRepository = harvestRepository;
        this.harvestDBService = harvestDBService;
    }

    public void start() {
        int count = 0;
        for (HarvestDTO harvestDTO : harvestRepository.findAllByOrderByBlockDate()) {
            harvestDBService.fillOwnersCount(harvestDTO);
            harvestRepository.save(harvestDTO);
            count++;
            if (count % 100 == 0) {
                log.info("Recalculated " + count + ", last " + harvestDTO.print());
            }
        }
    }
}
