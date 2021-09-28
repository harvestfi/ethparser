package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.utils.CommonUtils.parseLong;
import static pro.belbix.ethparser.utils.CommonUtils.reduceListElements;

import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.entity.v0.HarvestTvlEntity;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.model.TvlHistory;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.HarvestTvlRepository;
import pro.belbix.ethparser.service.DtoCache;
import pro.belbix.ethparser.service.HarvestTvlDBService;
import pro.belbix.ethparser.utils.Caller;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@Log4j2
@RestController
public class TvlController {

    private final HarvestTvlDBService harvestTvlDBService;
    private final HarvestTvlRepository harvestTvlRepository;
    private final ContractDbService contractDbService;
    private final DtoCache dtoCache;
    private final HarvestRepository harvestRepository;

    public TvlController(HarvestTvlDBService harvestTvlDBService,
        HarvestTvlRepository harvestTvlRepository,
        ContractDbService contractDbService, DtoCache dtoCache,
        HarvestRepository harvestRepository) {
        this.harvestTvlDBService = harvestTvlDBService;
        this.harvestTvlRepository = harvestTvlRepository;
        this.contractDbService = contractDbService;
        this.dtoCache = dtoCache;
        this.harvestRepository = harvestRepository;
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

    @RequestMapping(value = "/api/transactions/history/average_tvl", method = RequestMethod.GET)
    public RestResponse averageTvl(
        @RequestParam(value = "vault") String vault,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network,
        @RequestParam(value = "from", required = false)
        @Parameter(description = "Block creation time from (inclusive)") String from,
        @RequestParam(value = "to", required = false)
        @Parameter(description = "Block creation time to") String to
    ) {
        try {
            List<Double> avgTvlD = harvestRepository
                .fetchAverageTvl(
                    vault,
                    parseLong(from, 0),
                    parseLong(to, Long.MAX_VALUE),
                    network,
                    PageRequest.of(0, 1));
            if(!Caller.isNotEmptyList(avgTvlD)){
                return RestResponse.error("Not found average tvl for period from " + from +  " to " + to);
            }

            return RestResponse.ok((String.format("%.8f", avgTvlD.get(0))));
        } catch (Exception e) {
            log.warn("Error average TVL", e);
            return RestResponse.error("Server error during getting average TVL");
        }
    }

    @RequestMapping(value = "/api/transactions/history/last_tvl/{name}", method = RequestMethod.GET)
    public RestResponse lastTvl(
        @PathVariable("name") String _address,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network,
        @RequestParam(value = "limitToDate", required = false)
        @Parameter(description = "Block creation time from (inclusive)") String limitToDate
    ) {
        String address;
        if (!_address.startsWith("0x")) {
            address = contractDbService.getAddressByName(_address, ContractType.VAULT, network)
                .orElseThrow();
        } else {
            address = _address;
        }

        try {
            HarvestDTO harvestDTO = harvestRepository.fetchLastByVaultAndDateNotZero(address, network,
                parseLong(limitToDate, Long.MAX_VALUE));
            if(harvestDTO==null){
                return RestResponse.error("Not found last TVL for address" + address);
            }

            return RestResponse.ok((String.format("%.8f", harvestDTO.getLastUsdTvl())));
        } catch (Exception e) {
            log.warn("Error get last TVL", e);
            return RestResponse.error("Server error during get last TVL");
        }
    }


}
