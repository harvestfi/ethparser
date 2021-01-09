package pro.belbix.ethparser.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.entity.HarvestTvlEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;

@Service
public class TvlRecalculate {

    private static final Logger log = LoggerFactory.getLogger(TvlRecalculate.class);
    private final Map<String, TreeMap<Long, HarvestDTO>> harvestVaults = new HashMap<>();
    private final TreeMap<Long, UniswapDTO> uniswapDTOTreeMap = new TreeMap<>();

    private final HarvestRepository harvestRepository;
    private final AppProperties appProperties;
    private final HarvestTvlRepository harvestTvlRepository;
    private final UniswapRepository uniswapRepository;
    private final PriceProvider priceProvider;
    private final HarvestDBService harvestDBService;

    @Value("${tvl-recalculate.from:}")
    private Integer from;

    public TvlRecalculate(HarvestRepository harvestRepository,
                          AppProperties appProperties,
                          HarvestTvlRepository harvestTvlRepository,
                          UniswapRepository uniswapRepository, PriceProvider priceProvider,
                          HarvestDBService harvestDBService) {
        this.harvestRepository = harvestRepository;
        this.appProperties = appProperties;
        this.harvestTvlRepository = harvestTvlRepository;
        this.uniswapRepository = uniswapRepository;
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
                    harvestDTO.setPrices(priceProvider.getAllPrices(harvestDTO.getBlock().longValue()));
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
