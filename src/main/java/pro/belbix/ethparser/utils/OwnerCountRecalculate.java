package pro.belbix.ethparser.utils;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@Service
@Log4j2
public class OwnerCountRecalculate {

    private final HarvestRepository harvestRepository;
    private final HarvestDBService harvestDBService;
    private final UniswapDbService uniswapDbService;
    private final UniswapRepository uniswapRepository;

    @Value("${owners-count-recalculate.from:}")
    private Integer from;
    @Value("${owners-count-recalculate.empty:}")
    private Boolean empty;

    public OwnerCountRecalculate(HarvestRepository harvestRepository,
                                 HarvestDBService harvestDBService,
                                 UniswapDbService uniswapDbService,
                                 UniswapRepository uniswapRepository) {
        this.harvestRepository = harvestRepository;
        this.harvestDBService = harvestDBService;
        this.uniswapDbService = uniswapDbService;
        this.uniswapRepository = uniswapRepository;
    }

    public void start() {
        int count = 0;
        List<HarvestDTO> harvestDTOList;
        if (empty != null) {
            harvestDTOList = harvestRepository.fetchAllWithoutCounts();
        } else if (from == null) {
            harvestDTOList = harvestRepository.findAllByOrderByBlockDate();
        } else {
            harvestDTOList = harvestRepository.findAllByBlockDateGreaterThanOrderByBlockDate(from);
        }
        for (HarvestDTO harvestDTO : harvestDTOList) {
            harvestDBService.fillOwnersCount(harvestDTO);
            harvestRepository.save(harvestDTO);
            count++;
            if (count % 100 == 0) {
                log.info("Harvest Recalculated " + count + ", last " + harvestDTO.print());
            }
        }

        List<UniswapDTO> uniswapDTOS;
        if (from == null) {
            uniswapDTOS = uniswapRepository.findAllByOrderByBlockDate();
        } else {
            uniswapDTOS = uniswapRepository.findAllByBlockDateGreaterThanOrderByBlockDate(from);
        }

        count = 0;
        for (UniswapDTO uniswapDTO : uniswapDTOS) {
            uniswapDbService.fillOwnersCount(uniswapDTO);
            uniswapRepository.save(uniswapDTO);
            count++;
            if (count % 100 == 0) {
                log.info("Uniswap Recalculated " + count + ", last " + uniswapDTO.print());
            }
        }
    }
}
