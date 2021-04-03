package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.v0.PriceDTO;
import pro.belbix.ethparser.entity.contracts.VaultEntity;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.PriceRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@RestController
@RequestMapping(value = "/price")
@Log4j2
public class PriceController {
    private final PriceProvider priceProvider;
    private final EthBlockService ethBlockService;
    private final PriceRepository priceRepository;

    public PriceController(PriceProvider priceProvider,
                           EthBlockService ethBlockService,
                           PriceRepository priceRepository) {
        this.priceProvider = priceProvider;
        this.ethBlockService = ethBlockService;
        this.priceRepository = priceRepository;
    }

    @GetMapping(value = "/lp/{lp}")
    public RestResponse lpUsdAmount(
        @PathVariable("lp") String lp,
        @RequestParam("amount") double amount,
        @RequestParam(value = "block", required = false) Long block,
        @RequestParam(value = "network", required = false) String network
    ) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            ContractUtils contractUtils = new ContractUtils(network);
            String lpAddress = lp;
            if (!lp.startsWith("0x")) {
                if (!contractUtils.isUniPairName(lp)) {
                    return RestResponse.error("Not UniPair address");
                }
                lpAddress = contractUtils.getAddressByName(lp, ContractType.UNI_PAIR).orElse(null);
                if (lpAddress == null) {
                    return RestResponse.error("LP " + lp + " not supported");
                }
            }
            if (block == null) {
                block = ethBlockService.getLastBlock();
            }
            double amountUsd = priceProvider.getLpTokenUsdPrice(
                lpAddress.toLowerCase(), amount, block);
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
        @RequestParam(value = "network", required = false) String network
    ) {
        try {
            if (network == null || Strings.isBlank(network)) {
                network = ETH_NETWORK;
            }
            String tokenName = token;
            if (block == null) {
                block = ethBlockService.getLastBlock(network);
            }
            ContractUtils contractUtils = new ContractUtils(network);
            if (token.startsWith("0x")) {
                //shortcut for LP tokens from the dashboard
                if (contractUtils.isUniPairAddress(token)) {
                    return RestResponse.ok(String.format("%.8f",
                        priceProvider.getLpTokenUsdPrice(
                            token.toLowerCase(), 1, block)
                        )
                    ).addBlock(block);
                }

                //shortcut for fTokens tokens for the dashboard
                if (contractUtils.isPoolAddress(token)) {
                    Optional<VaultEntity> vaultO = contractUtils.vaultByPoolAddress(token);
                    if (vaultO.isEmpty()) {
                        return RestResponse.error("Not found underlying token");
                    }
                    return RestResponse.ok(String.format("%.8f",
                        priceProvider.getPriceForCoin(
                            vaultO.get().getContract().getName(),
                            block)
                        )
                    ).addBlock(block);
                }

                tokenName = contractUtils.getNameByAddress(token).orElse(null);
                if (tokenName == null) {
                    return RestResponse.error("Token " + token + " not supported");
                }
            }
            double usdPrice = priceProvider.getPriceForCoin(tokenName, block);
            return RestResponse.ok(String.format("%.8f", usdPrice)).addBlock(block);
        } catch (Exception e) {
            log.warn("Error token request", e);
            return RestResponse.error("Server error");
        }
    }

    @RequestMapping(value = "/token/latest", method = RequestMethod.GET)
    public List<PriceDTO> lastReward() {
        return priceRepository.fetchLastPrices();
    }
}
