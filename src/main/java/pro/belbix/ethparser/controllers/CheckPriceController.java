package pro.belbix.ethparser.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.web3.PriceProvider;

@RestController
@RequestMapping(value = "/check/price")
public class CheckPriceController {

    private final static Logger log = LoggerFactory.getLogger(CheckPriceController.class);

    private final PriceProvider priceProvider;

    public CheckPriceController(PriceProvider priceProvider) {
        this.priceProvider = priceProvider;
    }

    @GetMapping(value = "/lp")
    public String checkLpPrice(@RequestParam("lp") String lpAddress,
                        @RequestParam("amount") double amount,
                        @RequestParam(value = "block", required = false) Long block) {
        double amountUsd =  priceProvider.getLpPositionAmountInUsd(lpAddress, amount, block);
        return String.format("%.8f", amountUsd);
    }
}
