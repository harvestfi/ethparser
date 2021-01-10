package pro.belbix.ethparser.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.erc20.Tokens;
import pro.belbix.ethparser.web3.uniswap.contracts.LpContracts;

@RestController
@RequestMapping(value = "/price")
public class PriceController {

    private final static Logger log = LoggerFactory.getLogger(PriceController.class);

    private final PriceProvider priceProvider;
    private final EthBlockService ethBlockService;

    public PriceController(PriceProvider priceProvider, EthBlockService ethBlockService) {
        this.priceProvider = priceProvider;
        this.ethBlockService = ethBlockService;
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
}
