package pro.belbix.ethparser.utils;

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

    @Value("${tvl-recalculate.from:}")
    private Integer from;

    public TvlRecalculate(HarvestRepository harvestRepository,
                          AppProperties appProperties,
                          HarvestTvlRepository harvestTvlRepository,
                          UniswapRepository uniswapRepository) {
        this.harvestRepository = harvestRepository;
        this.appProperties = appProperties;
        this.harvestTvlRepository = harvestTvlRepository;
        this.uniswapRepository = uniswapRepository;
    }

    public void start() {
        HarvestDBService harvestDBService = new HarvestDBService(
            new HarvestRepositoryCache(harvestVaults),
            appProperties,
            harvestTvlRepository,
            new UniswapRepositoryCache(uniswapDTOTreeMap));

        List<HarvestDTO> harvestDTOList;
        if (from == null) {
            harvestDTOList = harvestRepository.findAllByOrderByBlockDate();
        } else {
            harvestDTOList = harvestRepository.findAllByBlockDateGreaterThanOrderByBlockDate(from);
        }

        harvestDtosToMap(harvestDTOList);
        uniDtosToMap(uniswapRepository.findAll());
        int count = 0;
        List<HarvestTvlEntity> tvls = new ArrayList<>();
        for (HarvestDTO harvestDTO : harvestDTOList) {
            count++;
            HarvestTvlEntity tvl = harvestDBService.saveHarvestTvl(harvestDTO, false);
            tvls.add(tvl);
            if (count % 10000 == 0) {
                harvestTvlRepository.saveAll(tvls);
                log.info("Save for " + harvestDTO.print());
                tvls.clear();
            }
        }
        harvestTvlRepository.saveAll(tvls);
    }

    private void harvestDtosToMap(List<HarvestDTO> harvestDTOList) {
        for (HarvestDTO dto : harvestDTOList) {
            TreeMap<Long, HarvestDTO> map = harvestVaults.get(dto.getVault());
            if (map == null) {
                map = new TreeMap<>();
                map.put(dto.getBlockDate(), dto);
                harvestVaults.put(dto.getVault(), map);
            } else {
                map.put(dto.getBlockDate(), dto);
            }
        }
    }

    private void uniDtosToMap(List<UniswapDTO> uniswapDTOS) {
        for (UniswapDTO dto : uniswapDTOS) {
            if ("FARM".equals(dto.getCoin())) {
                uniswapDTOTreeMap.put(dto.getBlockDate(), dto);
            }
        }
    }
}
