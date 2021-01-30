package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;

@RestController
public class HarvestController {

    private final HarvestRepository harvestRepository;
    private final HarvestDBService harvestDBService;

    public HarvestController(HarvestRepository harvestRepository,
                             HarvestDBService harvestDBService) {
        this.harvestRepository = harvestRepository;
        this.harvestDBService = harvestDBService;
    }

    @RequestMapping(value = "api/transactions/last/harvest", method = RequestMethod.GET)
    public List<HarvestDTO> lastTvl() {
        return harvestRepository.fetchLastTvl();
    }

    @RequestMapping(value = "api/transactions/history/harvest/{name}", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryDataForVault(@PathVariable("name") String name) {
        return harvestRepository.findAllByVaultOrderByBlockDate(name);
    }

    @RequestMapping(value = "api/transactions/history/harvest", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryData(@RequestParam(value = "from", required = false) String from,
                                                   @RequestParam(value = "to", required = false) String to) {
        return harvestDBService.fetchHarvest(from, to);
    }

    @GetMapping("/history/harvest/{address}")
    public List<HarvestDTO> addressHistoryHarvest(@PathVariable("address") String address,
                                                  @RequestParam(value = "from", required = false) String from,
                                                  @RequestParam(value = "to", required = false) String to) {
        return harvestRepository.fetchAllByOwner(address, parseLong(from, 0), parseLong(to, Long.MAX_VALUE));
    }

}
