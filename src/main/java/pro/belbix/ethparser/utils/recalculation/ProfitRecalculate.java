package pro.belbix.ethparser.utils.recalculation;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;

import org.springframework.stereotype.Service;


@Service
@Log4j2
public class ProfitRecalculate {

    private final HarvestRepository harvestRepository;
    private final HarvestDBService harvestDBService;

    @Value("${profit-recalculate.from:}")
    private Integer from;

    public ProfitRecalculate(HarvestRepository harvestRepository,
                          HarvestDBService harvestDBService) {
        this.harvestRepository = harvestRepository;
        this.harvestDBService = harvestDBService;
    }

    public void start() {
        List<HarvestDTO> harvestDTOList;
        log.info("Loading transactions from database");
        if (from == null) {
            from = 0;
         }
        harvestDTOList = harvestRepository.findAllByMethodNameAndBlockDateGreaterThanOrderByBlockDate("Withdraw", from);
        
        log.info("Loaded " + harvestDTOList.size() + " Withdraw transactions. Starting recalculation..");
        int count = 0;
        for (HarvestDTO harvestDTO : harvestDTOList) {
            harvestDBService.fillProfit(harvestDTO);
            try {
                if (harvestDTO.getProfit() != 0.0) {
                    harvestRepository.save(harvestDTO);
                    log.info("Profit recalculated for " + harvestDTO.print());
                }
            } catch(Exception e) {
                log.error("Error saving " + harvestDTO.print(), e.fillInStackTrace());
            }
            count++;
            if (count % 1000 == 0) {
                log.info(count + " done");
            }
        }

    }
}
