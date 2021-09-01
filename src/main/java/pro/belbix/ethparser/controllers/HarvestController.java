package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.utils.CommonUtils.parseLong;
import static pro.belbix.ethparser.utils.CommonUtils.reduceListElements;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.model.PaginatedResponse;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.HarvestRepository;
import pro.belbix.ethparser.repositories.v0.HarvestRepository.UserBalance;
import pro.belbix.ethparser.service.DtoCache;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
@Log4j2
@Tag(name = "HarvestController", description = "Obtaining data on Harvest")
public class HarvestController {

    private final HarvestRepository harvestRepository;
    private final VaultActionsDBService vaultActionsDBService;
    private final ContractDbService contractDbService;
    private final DtoCache dtoCache;

    public HarvestController(HarvestRepository harvestRepository,
        VaultActionsDBService vaultActionsDBService,
        ContractDbService contractDbService, DtoCache dtoCache) {
        this.harvestRepository = harvestRepository;
        this.vaultActionsDBService = vaultActionsDBService;
        this.contractDbService = contractDbService;
        this.dtoCache = dtoCache;
    }

    @Operation(summary = "Returns the latest data for each vault",
        description = "Obtaining a list of data of unique 'vault_address' in the same network."
            + " The list is sorted by 'vault_address' and 'block date'")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HarvestDTO.class)))
            })
    })
    @RequestMapping(value = "api/transactions/last/harvest", method = RequestMethod.GET)
    public List<HarvestDTO> fetchLatest(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
        @Parameter(description = "Working network") String network
    ) {
        return harvestRepository.fetchLatest(network);
    }

    @Operation(summary = "Returns history data of vault for a given address", description = "")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HarvestDTO.class)))
            })
    })
    @RequestMapping(value = "api/transactions/history/harvest/{name}", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryDataForVault(
        @PathVariable("name") @Parameter(description = "Repository address name") String _address,
        @RequestParam(value = "reduce", required = false, defaultValue = "1")
        @Parameter(description = "Reduces the amount of result data")Integer reduce,
        @RequestParam(value = "start", required = false, defaultValue = "0")
        @Parameter(description = "Block creation time from") Long start,
        @RequestParam(value = "end", required = false, defaultValue = Long.MAX_VALUE + "")
        @Parameter(description = "Block creation time to") Long end,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
        @Parameter(description = "Working network") String network
    ) {
        String address;
        if (!_address.startsWith("0x")) {
            address = contractDbService.getAddressByName(_address, ContractType.VAULT, network)
                .orElseThrow();
        } else {
            address = _address;
        }
        return reduceListElements(
            dtoCache.load("findAllByVaultOrderByBlockDate" +
                address + start + end + network, () ->
                harvestRepository.findAllByVaultOrderByBlockDate(
                    address,
                    start,
                    end,
                    network)), reduce);
    }

    @Operation(summary = "Returns whole history for all vaults", description = "")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HarvestDTO.class)))
            })
    })
    @RequestMapping(value = "api/transactions/history/harvest", method = RequestMethod.GET)
    public Iterable<HarvestDTO> harvestHistoryData(
        @RequestParam(value = "from", required = false)
        @Parameter(description = "Block creation time from") String from,
        @RequestParam(value = "to", required = false)
        @Parameter(description = "Block creation time to (inclusive)") String to,
        @RequestParam(value = "reduce", required = false, defaultValue = "1")
        @Parameter(description = "Reduces the amount of result data") Integer reduce,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
        @Parameter(description = "Working network") String network
    ) {
        return reduceListElements(
            dtoCache.load("findAllByVaultOrderByBlockDate" +
                from + to + network, () ->
                vaultActionsDBService.fetchHarvest(from, to, network)), reduce);
    }

    @Operation(summary = "Returns vault history for a given owner address", description = "")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HarvestDTO.class)))
            })
    })
    @GetMapping("/history/harvest/{address}")
    public List<HarvestDTO> addressHistoryHarvest(
        @PathVariable("address") @Parameter(description = "Owner's address") String address,
        @RequestParam(value = "from", required = false)
        @Parameter(description = "Block creation time from (inclusive)") String from,
        @RequestParam(value = "to", required = false)
        @Parameter(description = "Block creation time to") String to,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
        @Parameter(description = "Working network")String network
    ) {
        return harvestRepository.fetchAllByOwner(address.toLowerCase(), parseLong(from, 0),
            parseLong(to, Long.MAX_VALUE), network);
    }

    @Operation(summary = "Returns balances of owners", description = "")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserBalance.class)))
            })
    })
    @GetMapping("/user_balances")
    public List<UserBalance> userBalances(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
        @Parameter(description = "Working network") String network
    ) {
        return harvestRepository.fetchOwnerBalances(network);
    }

    @Operation(summary = "Return whole vaults history by page pattern", description = "")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RestResponse.class))
            })
    })
    @GetMapping(value = "/harvest/pages")
    public RestResponse harvestPages(
        @RequestParam("pageSize")
        @Parameter(description = "Number of items per page") String pageSize,
        @RequestParam("page") @Parameter(description = "Page number") String page,
        @RequestParam(value = "ordering", required = false)
        @Parameter(description = "Sorting (asc/desc)") String ordering,
        @RequestParam(value = "vault", required = false)
        @Parameter(description = "Vault address") String vault,
        @RequestParam(value = "minAmount", required = false)
        @Parameter(description = "Minimum amount in dollars") Integer minAmount,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
        @Parameter(description = "Working network") String network
    ) {
        try {
            int start = Integer.parseInt(page);
            int size = Integer.parseInt(pageSize);
            Sort sorting = Sort.by("blockDate");
            if (!Strings.isBlank(ordering) && "desc".equals(ordering)) {
                sorting = sorting.descending();
            }

            Page<HarvestDTO> pages;
            if (minAmount == null) {
                minAmount = Integer.MIN_VALUE;
            }
            if (Strings.isBlank(vault)) {
                pages = harvestRepository
                    .fetchPages(minAmount, network, PageRequest.of(start, size, sorting));
            } else {
                if (!vault.startsWith("0x")) {
                    vault = contractDbService.getAddressByName(vault, ContractType.VAULT, network)
                        .orElseThrow();
                }
                pages = harvestRepository
                    .fetchPagesByVault(vault, network, minAmount,
                        PageRequest.of(start, size, sorting));
            }

            if (!pages.hasContent()) {
                return RestResponse.error("Data not found");
            }
            return RestResponse.ok(
                ObjectMapperFactory.getObjectMapper().writeValueAsString(
                    PaginatedResponse.builder()
                        .currentPage(start)
                        .previousPage(pages.hasPrevious() ? start - 1 : -1)
                        .nextPage(pages.hasNext() ? start + 1 : -1)
                        .totalPages(pages.getTotalPages())
                        .data(pages.getContent())
                        .build()
                )
            );

        } catch (Exception e) {
            String msg = "Error get hardwork pages";
            log.warn(msg, e);
            return RestResponse.error(msg);
        }
    }

    @Operation(summary = "Returns period of work in seconds for vault", description = "")
    @GetMapping("api/transactions/history/harvest/period_of_work/{address}")
    public RestResponse vaultPeriodOfWork(
        @PathVariable("address") @Parameter(description = "Vault address") String _address,
        @RequestParam(value = "toBlockDate", required = false)
        @Parameter(description = "Block creation time to (inclusive)") String limitToDate,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
        @Parameter(description = "Working network")String network
    ) {
        String address;
        if (!_address.startsWith("0x")) {
            address = contractDbService.getAddressByName(_address, ContractType.VAULT, network)
                .orElseThrow();
        } else {
            address = _address;
        }
        List<Long> periods = harvestRepository.fetchPeriodOfWork(address, parseLong(limitToDate,
            Long.MAX_VALUE), network, PageRequest.of(0, 1));

        if(periods.isEmpty() || periods.get(0) == null) {
            return RestResponse.error("Not found period for address " + address);
        }
        return RestResponse.ok(periods.get(0).toString());
    }

}
