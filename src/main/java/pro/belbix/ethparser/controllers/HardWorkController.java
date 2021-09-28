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
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.model.PaginatedResponse;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.HardWorkRepository;
import pro.belbix.ethparser.service.DtoCache;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.HardWorkCalculator;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
@Log4j2
@Tag(name = "HardWorkController", description = "Obtaining data on HardWork")
public class HardWorkController {

    private final HardWorkRepository hardWorkRepository;
    private final HardWorkCalculator hardWorkCalculator;
    private final ContractDbService contractDbService;
    private final DtoCache dtoCache;

    public HardWorkController(HardWorkRepository hardWorkRepository,
        HardWorkCalculator hardWorkCalculator,
        ContractDbService contractDbService, DtoCache dtoCache) {
        this.hardWorkRepository = hardWorkRepository;
        this.hardWorkCalculator = hardWorkCalculator;
        this.contractDbService = contractDbService;
        this.dtoCache = dtoCache;
    }

    @Operation(summary = "Returns the latest data for HardWorks")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HardWorkDTO.class)))
            })
    })
    @RequestMapping(value = "api/transactions/last/hardwork", method = RequestMethod.GET)
    public List<HardWorkDTO> lastHardWorks(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
            @Parameter(description = "Working network") String network
    ) {
        return hardWorkRepository.fetchLatest(network);
    }

    @Operation(summary = "Returns history HardWork for a given address")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HardWorkDTO.class)))
            })
    })
    @RequestMapping(value = "api/transactions/history/hardwork/{name}", method = RequestMethod.GET)
    public List<HardWorkDTO> historyHardWorkByName(
        @PathVariable("name") @Parameter(description = "Repository address name")  String _address,
        @RequestParam(value = "start", required = false)
            @Parameter(description = "Block creation time from") String start,
        @RequestParam(value = "reduce", required = false, defaultValue = "1")
            @Parameter(description = "Reduces the amount of result data") Integer reduce,
        @RequestParam(value = "end", required = false)
            @Parameter(description = "Block creation time to") String end,
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
            dtoCache.load("findAllByVaultOrderByBlockDate"
                + start + end + address + network, () ->
                hardWorkRepository
                    .findAllByVaultOrderByBlockDate(
                        address,
                        network,
                        parseLong(start, 0),
                        parseLong(end, Long.MAX_VALUE))
            ), reduce);
    }

    @Operation(summary = "Returns whole history for HardWork")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HardWorkDTO.class)))
            })
    })
    @RequestMapping(value = "api/transactions/history/hardwork", method = RequestMethod.GET)
    public List<HardWorkDTO> historyHardWork(
        @RequestParam(value = "reduce", required = false, defaultValue = "1")
            @Parameter(description = "Reduces the amount of result data") Integer reduce,
        @RequestParam(value = "from", required = false, defaultValue = "0")
            @Parameter(description = "Block creation time from") String from,
        @RequestParam(value = "to", required = false, defaultValue = Long.MAX_VALUE + "")
            @Parameter(description = "Block creation time to (inclusive)") String to,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
            @Parameter(description = "Working network") String network
    ) {
        return reduceListElements(
            dtoCache.load("fetchAllInRange" + from + to + network, () ->
                hardWorkRepository
                    .fetchAllInRange(Long.parseLong(from), Long.parseLong(to), network)
            ), reduce);
    }

    @Operation(summary = "Returns the last saved amount of gas")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RestResponse.class)))
            })
    })
    @RequestMapping(value = "last_saved_gas_sum", method = RequestMethod.GET)
    public RestResponse lastSavedGasSum(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
            @Parameter(description = "Working network") String network
    ) {
        try {
            return RestResponse.ok((String.format("%.8f",
                hardWorkRepository.fetchLastGasSaved(network))));
        } catch (Exception e) {
            log.warn("Error get last saved gas sum", e);
            return RestResponse.error("Server error during getting last saved gas");
        }

    }

    @Operation(summary = "Returns the total saved gas fee the ETH address")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RestResponse.class)))
            })
    })
    @RequestMapping(value = "total_saved_gas_fee_by_address", method = RequestMethod.GET)
    public RestResponse totalSavedGasFeeByEthAddress(
        @RequestParam(value = "address") @Parameter(description = "Owner's address")  String address,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
            @Parameter(description = "Working network") String network
    ) {
        try {
            return RestResponse.ok((String.format("%.8f",
                hardWorkCalculator
                    .calculateTotalHardWorksFeeByOwner(address.toLowerCase(), network)
            )
            ));
        } catch (Exception e) {
            String msg = "Error get total saved gas fee for address: " + address;
            log.warn(msg, e);
            return RestResponse.error(msg);
        }
    }

    @Operation(summary = "Returns the latest data for HardWork")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = HardWorkDTO.class)))
            })
    })
    @GetMapping(value = "/last/hardwork")
    public HardWorkDTO lastHardWork(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK)
            @Parameter(description = "Working network") String network
    ) {
        return hardWorkRepository.findFirstByNetworkOrderByBlockDateDesc(network);
    }

    @Operation(summary = "Return whole HardWork history by page pattern")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = {
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RestResponse.class)))
            })
    })
    @GetMapping(value = "/hardwork/pages")
    public RestResponse hardworkPages(
        @RequestParam("pageSize") @Parameter(description = "Number of items per page") String pageSize,
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

            Page<HardWorkDTO> pages;
            if (minAmount == null) {
                minAmount = Integer.MIN_VALUE;
            }
            if (Strings.isBlank(vault)) {
                pages = hardWorkRepository
                    .fetchPages(minAmount, network, PageRequest.of(start, size, sorting));
            } else {
                if (!vault.startsWith("0x")) {
                    vault = contractDbService.getAddressByName(vault, ContractType.VAULT, network)
                        .orElseThrow();
                }
                pages = hardWorkRepository
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


}
