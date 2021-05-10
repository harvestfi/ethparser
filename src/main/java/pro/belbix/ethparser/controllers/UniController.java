package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

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
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.model.PaginatedResponse;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;
import pro.belbix.ethparser.repositories.v0.UniswapRepository.OhlcProjection;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
@Log4j2
public class UniController {

    private final UniswapRepository uniswapRepository;
    private final UniswapDbService uniswapDbService;
    private final ContractDbService contractDbService;

    public UniController(UniswapRepository uniswapRepository,
        UniswapDbService uniswapDbService,
        ContractDbService contractDbService) {
        this.uniswapRepository = uniswapRepository;
        this.uniswapDbService = uniswapDbService;
        this.contractDbService = contractDbService;
    }

    @RequestMapping(value = "api/transactions/history/uni", method = RequestMethod.GET)
    public Iterable<UniswapDTO> uniswapHistoryData(@RequestParam(value = "from", required = false) String from,
                                                   @RequestParam(value = "to", required = false) String to) {
        return uniswapDbService.fetchUni(from, to);
    }

    @RequestMapping(value = "api/transactions/history/uni/ohcl/{name}", method = RequestMethod.GET)
    public Iterable<OhlcProjection> ohclUniswapTx(
        @PathVariable("name") String address,
        @RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end) {
        if (!address.startsWith("0x")) {
            address = contractDbService.getAddressByName(address, ContractType.TOKEN, ETH_NETWORK)
                .orElseThrow();
        }
        return uniswapRepository
            .fetchOHLCTransactions(address, parseLong(start, 0), parseLong(end, Long.MAX_VALUE), 3600);
    }

    @GetMapping("/history/uni/{address}")
    public List<UniswapDTO> addressHistoryUni(@PathVariable("address") String address,
                                              @RequestParam(value = "from", required = false) String from,
                                              @RequestParam(value = "to", required = false) String to) {
        return uniswapRepository.fetchAllByOwner(
            address.toLowerCase(), parseLong(from, 0), parseLong(to, Long.MAX_VALUE));
    }

    @GetMapping(value = "/uni/pages")
    public RestResponse uniPages(
        @RequestParam("pageSize") String pageSize,
        @RequestParam("page") String page,
        @RequestParam(value = "ordering", required = false) String ordering,
        @RequestParam(value = "token", required = false) String token,
        @RequestParam(value = "minAmount", required = false) Integer minAmount,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        try {
            int start = Integer.parseInt(page);
            int size = Integer.parseInt(pageSize);
            Sort sorting = Sort.by("blockDate");
            if (!Strings.isBlank(ordering) && "desc".equals(ordering)) {
                sorting = sorting.descending();
            }

            Page<UniswapDTO> pages;
            if (minAmount == null) {
                minAmount = Integer.MIN_VALUE;
            }
            if (Strings.isBlank(token)) {
                pages = uniswapRepository
                    .fetchPages(minAmount, PageRequest.of(start, size, sorting));
            } else {
                if (!token.startsWith("0x")) {
                    token = contractDbService.getAddressByName(token, ContractType.TOKEN, network)
                        .orElseThrow();
                }
                pages = uniswapRepository
                    .fetchPagesByToken(token, minAmount,
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
