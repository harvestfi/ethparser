package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractType.UNI_PAIR;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@RestController
@RequestMapping(value = "/price")
@Log4j2
public class PriceController {
    private final PriceProvider priceProvider;
    private final EthBlockService ethBlockService;
    private final PriceRepository priceRepository;
    private final ContractDbService contractDbService;

    public PriceController(PriceProvider priceProvider,
        EthBlockService ethBlockService,
        PriceRepository priceRepository,
        ContractDbService contractDbService) {
        this.priceProvider = priceProvider;
        this.ethBlockService = ethBlockService;
        this.priceRepository = priceRepository;
        this.contractDbService = contractDbService;
    }

    @GetMapping(value = "/lp/{lp}")
    public RestResponse lpUsdAmount(
        @PathVariable("lp") String lp,
        @RequestParam("amount") double amount,
        @RequestParam(value = "block", required = false) Long block,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        try {
            String lpAddress = lp;
            if (!lp.startsWith("0x")) {
                if (contractDbService.getAddressByName(lp, ContractType.UNI_PAIR, network)
                    .isEmpty()) {
                    return RestResponse.error("Not LP name");
                }
                lpAddress = contractDbService.getAddressByName(lp, ContractType.UNI_PAIR, network)
                    .orElse(null);
                if (lpAddress == null) {
                    return RestResponse.error("LP " + lp + " not supported");
                }
            }
            if (block == null) {
                block = ethBlockService.getLastBlock(network);
            }
            double amountUsd = priceProvider.getLpTokenUsdPrice(
                lpAddress.toLowerCase(), amount, block, network);
            return RestResponse.ok(String.format("%.8f", amountUsd)).addBlock(block);
        } catch (Exception e) {
            log.warn("Error lp request", e);
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/token/{token}")
    public RestResponse token(
        @PathVariable("token") String token,
        @RequestParam(value = "block", required = false) Long block,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        try {
            if (block == null) {
                block = ethBlockService.getLastBlock(network);
            }
            if (token.startsWith("0x")) {
                //shortcut for LP tokens from the dashboard
                if (contractDbService
                    .getContractByAddressAndType(token, UNI_PAIR, network)
                    .isPresent()) {
                    return RestResponse.ok(String.format("%.8f",
                        priceProvider.getLpTokenUsdPrice(
                            token.toLowerCase(), 1, block, network)
                        )
                    ).addBlock(block);
                }
            }

            double usdPrice = priceProvider.getPriceForCoin(token, block, ETH_NETWORK);
            return RestResponse.ok(String.format("%.8f", usdPrice)).addBlock(block);
        } catch (Exception e) {
            log.warn("Error token request", e);
            return RestResponse.error("Server error");
        }
    }

    @RequestMapping(value = "/token/latest", method = RequestMethod.GET)
    public List<PriceDTO> lastPrices(
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        return priceRepository.fetchLastPrices(network);
    }
}
