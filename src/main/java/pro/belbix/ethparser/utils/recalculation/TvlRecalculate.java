package pro.belbix.ethparser.utils.recalculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.entity.HarvestTvlEntity;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@Service
@Log4j2
public class TvlRecalculate {

    private final HarvestRepository harvestRepository;
    private final HarvestTvlRepository harvestTvlRepository;
    private final PriceProvider priceProvider;
    private final HarvestDBService harvestDBService;

    @Value("${tvl-recalculate.from:}")
    private Integer from;

    public TvlRecalculate(HarvestRepository harvestRepository,
                          HarvestTvlRepository harvestTvlRepository,
                          PriceProvider priceProvider,
                          HarvestDBService harvestDBService) {
        this.harvestRepository = harvestRepository;
        this.harvestTvlRepository = harvestTvlRepository;
        this.priceProvider = priceProvider;
        this.harvestDBService = harvestDBService;
    }

    public void start() {
        List<HarvestDTO> harvestDTOList;
        if (from == null) {
            harvestDTOList = harvestRepository.findAllByOrderByBlockDate();
        } else {
            harvestDTOList = harvestRepository.findAllByBlockDateGreaterThanOrderByBlockDate(from);
        }
        int count = 0;
        List<HarvestTvlEntity> tvls = new ArrayList<>();
        for (HarvestDTO harvestDTO : harvestDTOList) {
            count++;
            if (harvestDTO.getPrices() == null || harvestDTO.getPrices().contains("NaN")) {
                try {
                    harvestDTO.setPrices(priceProvider.getAllPrices(harvestDTO.getBlock()));
                } catch (JsonProcessingException e) {
                    log.error("Error parse prices", e);
                }
                harvestRepository.save(harvestDTO);
            }
            HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(harvestDTO, false);
            tvls.add(tvl);
            if (count % 100 == 0) {
                harvestTvlRepository.saveAll(tvls);
                log.info("Save for " + harvestDTO.print());
                tvls.clear();
            }
        }
        harvestTvlRepository.saveAll(tvls);
    }
}
