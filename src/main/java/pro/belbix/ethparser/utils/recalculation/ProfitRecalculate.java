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
        if (from == null) {
            harvestDTOList = harvestRepository.findAllByMethodNameOrderByBlockDate("Withdraw");
        } else {
            harvestDTOList = harvestRepository.findAllByMethodNameAndBlockDateGreaterThanOrderByBlockDate("Withdraw", from);
        }
        for (HarvestDTO harvestDTO : harvestDTOList) {
            harvestDBService.fillProfit(harvestDTO);
            try {
                if (harvestDTO.getProfit() != null && !harvestDTO.getProfit().equals(0D)) {
                    harvestRepository.save(harvestDTO);
                    log.info("Profit recalculated for " + harvestDTO.getVault() + " " + harvestDTO.print());
                }
            } catch(Exception e) {
                log.error("Error saving " + harvestDTO.print(), e.fillInStackTrace());
            }
        }

    }
}
