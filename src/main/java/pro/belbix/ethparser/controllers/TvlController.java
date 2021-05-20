package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.utils.CommonUtils.parseLong;
import static pro.belbix.ethparser.utils.CommonUtils.reduceListElements;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.entity.v0.HarvestTvlEntity;
import pro.belbix.ethparser.model.TvlHistory;
import pro.belbix.ethparser.repositories.v0.HarvestTvlRepository;
import pro.belbix.ethparser.service.DtoCache;
import pro.belbix.ethparser.service.HarvestTvlDBService;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
public class TvlController {

    private final HarvestTvlDBService harvestTvlDBService;
    private final HarvestTvlRepository harvestTvlRepository;
    private final ContractDbService contractDbService;
    private final DtoCache dtoCache;

    public TvlController(HarvestTvlDBService harvestTvlDBService,
        HarvestTvlRepository harvestTvlRepository,
        ContractDbService contractDbService, DtoCache dtoCache) {
        this.harvestTvlDBService = harvestTvlDBService;
        this.harvestTvlRepository = harvestTvlRepository;
        this.contractDbService = contractDbService;
        this.dtoCache = dtoCache;
    }

    @RequestMapping(value = "api/transactions/history/tvl/{name}", method = RequestMethod.GET)
    public Iterable<TvlHistory> tvlHistoryByVault(
        @PathVariable("name") String _address,
        @RequestParam(value = "reduce", required = false, defaultValue = "1") Integer reduce,
        @RequestParam(value = "start", required = false, defaultValue = "0") Long start,
        @RequestParam(value = "end", required = false, defaultValue = Long.MAX_VALUE + "") Long end,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        String address;
        if (!_address.startsWith("0x")) {
            address = contractDbService.getAddressByName(_address, ContractType.VAULT, network)
                .orElseThrow();
        } else {
            address = _address;
        }
        return reduceListElements(dtoCache.load(
            "fetchTvlByVault" + address + start + end + network, () ->
                harvestTvlDBService.fetchTvlByVault(address, start, end, network)
        ), reduce);
    }

    @RequestMapping(value = "api/transactions/history/alltvl", method = RequestMethod.GET)
    public Iterable<HarvestTvlEntity> allTvlHistoryData(
        @RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        return harvestTvlRepository
            .getHistoryOfAllTvl(parseLong(start, 0), parseLong(end, Long.MAX_VALUE), network);
    }

}
