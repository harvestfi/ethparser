package pro.belbix.ethparser.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.entity.HarvestTvlEntity;
import pro.belbix.ethparser.model.TvlHistory;
import pro.belbix.ethparser.repositories.HarvestTvlRepository;
import pro.belbix.ethparser.service.HarvestTvlDBService;

@RestController
public class TvlController {

    private final HarvestTvlDBService harvestTvlDBService;
    private final HarvestTvlRepository harvestTvlRepository;

    public TvlController(HarvestTvlDBService harvestTvlDBService,
                         HarvestTvlRepository harvestTvlRepository) {
        this.harvestTvlDBService = harvestTvlDBService;
        this.harvestTvlRepository = harvestTvlRepository;
    }

    @RequestMapping(value = "api/transactions/history/tvl/{name}", method = RequestMethod.GET)
    public Iterable<TvlHistory> tvlHistoryByVault(@PathVariable("name") String name) {
        return harvestTvlDBService.fetchTvlByVault(name);
    }

    @RequestMapping(value = "api/transactions/history/alltvl", method = RequestMethod.GET)
    public Iterable<HarvestTvlEntity> allTvlHistoryData() {
        return harvestTvlRepository.getHistoryOfAllTvl();
    }

}
