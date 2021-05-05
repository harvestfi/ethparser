package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
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
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
public class HarvestController {

    private final HarvestRepository harvestRepository;
    private final VaultActionsDBService vaultActionsDBService;
    private final ContractDbService contractDbService;

    public HarvestController(HarvestRepository harvestRepository,
        VaultActionsDBService vaultActionsDBService,
        ContractDbService contractDbService) {
        this.harvestRepository = harvestRepository;
        this.vaultActionsDBService = vaultActionsDBService;
        this.contractDbService = contractDbService;
    }

    @RequestMapping(value = "api/transactions/last/harvest", method = RequestMethod.GET)
    public List<HarvestDTO> lastTvl(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        return harvestRepository.fetchLastTvl(network);
    }

    @RequestMapping(value = "api/transactions/history/harvest/{name}", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryDataForVault(
        @PathVariable("name") String address,
        @RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        if (!address.startsWith("0x")) {
            address = contractDbService.getAddressByName(address, ContractType.VAULT, network)
                .orElseThrow();
        }
        return harvestRepository.findAllByVaultOrderByBlockDate(address, parseLong(start, 0),
            parseLong(end, Long.MAX_VALUE), network);
    }

    @RequestMapping(value = "api/transactions/history/harvest", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryData(
        @RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        return vaultActionsDBService.fetchHarvest(from, to, network);
    }

    @GetMapping("/history/harvest/{address}")
    public List<HarvestDTO> addressHistoryHarvest(
        @PathVariable("address") String address,
        @RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        return harvestRepository.fetchAllByOwner(address.toLowerCase(), parseLong(from, 0),
            parseLong(to, Long.MAX_VALUE), network);
    }

    @GetMapping("/user_balances")
    public List<UserBalance> userBalances(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        return harvestRepository.fetchOwnerBalances(network);
    }

}
