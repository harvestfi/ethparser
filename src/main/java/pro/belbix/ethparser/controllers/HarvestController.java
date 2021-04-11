package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.HarvestRepository.UserBalance;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
public class HarvestController {

    private final HarvestRepository harvestRepository;
    private final VaultActionsDBService vaultActionsDBService;

    public HarvestController(HarvestRepository harvestRepository,
                             VaultActionsDBService vaultActionsDBService) {
        this.harvestRepository = harvestRepository;
        this.vaultActionsDBService = vaultActionsDBService;
    }

    @RequestMapping(value = "api/transactions/last/harvest", method = RequestMethod.GET)
    public List<HarvestDTO> lastTvl() {
        return harvestRepository.fetchLastTvl();
    }

    @RequestMapping(value = "api/transactions/history/harvest/{name}", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryDataForVault(@PathVariable("name") String name,
                                                           @RequestParam(value = "start", required = false) String start,
                                                           @RequestParam(value = "end", required = false) String end) {
        return harvestRepository.findAllByVaultOrderByBlockDate(name, parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
    }

    @RequestMapping(value = "api/transactions/history/harvest", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryData(@RequestParam(value = "from", required = false) String from,
                                                   @RequestParam(value = "to", required = false) String to) {
        return vaultActionsDBService.fetchHarvest(from, to);
    }

    @GetMapping("/history/harvest/{address}")
    public List<HarvestDTO> addressHistoryHarvest(@PathVariable("address") String address,
                                                  @RequestParam(value = "from", required = false) String from,
                                                  @RequestParam(value = "to", required = false) String to) {
        return harvestRepository.fetchAllByOwner(address.toLowerCase(), parseLong(from, 0), parseLong(to, Long.MAX_VALUE));
    }

    @GetMapping("/user_balances")
    public List<UserBalance> userBalances() {
        return harvestRepository.fetchOwnerBalances();
    }

}
