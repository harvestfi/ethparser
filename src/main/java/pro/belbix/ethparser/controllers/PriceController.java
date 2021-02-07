package pro.belbix.ethparser.controllers;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.PriceDTO;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.PriceRepository;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.contracts.Tokens;
import pro.belbix.ethparser.web3.prices.PriceProvider;
import pro.belbix.ethparser.web3.contracts.LpContracts;

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
    public RestResponse lpUsdAmount(@PathVariable("lp") String lp,
                                    @RequestParam("amount") double amount) {
        try {
            String lpAddress = lp;
            if (!lp.startsWith("0x")) {
                lpAddress = LpContracts.lpNameToHash.get(lp);
                if (lpAddress == null) {
                    return RestResponse.error("LP " + lp + " not supported");
                }
            }
            long block = ethBlockService.getLastBlock();
            double amountUsd = priceProvider.getLpPositionAmountInUsd(
                lpAddress.toLowerCase(), amount, block);
            return RestResponse.ok(String.format("%.8f", amountUsd)).addBlock(block);
        } catch (Exception e) {
            log.error("Error lp request", e);
            return RestResponse.error("Server error");
        }
    }

    @GetMapping(value = "/token/{token}")
    public RestResponse token(@PathVariable("token") String token) {
        try {
            String tokenName = token;
            if (token.startsWith("0x")) {
                tokenName = Tokens.findNameForContract(token.toLowerCase());
                if (tokenName == null) {
                    return RestResponse.error("Token " + token + " not supported");
                }
            }
            long block = ethBlockService.getLastBlock();
            double amountUsd = priceProvider.getPriceForCoin(tokenName, block);
            return RestResponse.ok(String.format("%.8f", amountUsd)).addBlock(block);
        } catch (Exception e) {
            log.error("Error token request", e);
            return RestResponse.error("Server error");
        }
    }

    @RequestMapping(value = "/token/latest", method = RequestMethod.GET)
    public List<PriceDTO> lastReward() {
        return priceRepository.fetchLastPrices();
    }
}
