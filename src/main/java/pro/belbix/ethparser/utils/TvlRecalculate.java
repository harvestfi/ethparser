package pro.belbix.ethparser.utils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.HarvestDBService;

@Service
public class TvlRecalculate {

    private static final Logger log = LoggerFactory.getLogger(TvlRecalculate.class);
    @Autowired
    private HarvestRepository harvestRepository;
    @Autowired
    private HarvestDBService harvestDBService;

    public void start() {

        List<HarvestDTO> harvestDTOList = harvestRepository.findAll();

        for (HarvestDTO harvestDTO : harvestDTOList) {
            log.info("Save for " + harvestDTO.print());
            harvestDBService.saveHarvestTvl(harvestDTO);
        }


    }

}
