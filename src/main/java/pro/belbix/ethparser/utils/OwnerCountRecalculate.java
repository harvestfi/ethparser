package pro.belbix.ethparser.utils;

import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;

@Service
public class OwnerCountRecalculate {

    private final HarvestRepository harvestRepository;
    private final HarvestDBService harvestDBService;

    public OwnerCountRecalculate(HarvestRepository harvestRepository,
                                 HarvestDBService harvestDBService) {
        this.harvestRepository = harvestRepository;
        this.harvestDBService = harvestDBService;
    }

    public void start() {
        for (HarvestDTO harvestDTO : harvestRepository.findAllByOrderByBlockDate()) {
            harvestDBService.fillOwnersCount(harvestDTO);
            harvestRepository.save(harvestDTO);
        }
    }
}
