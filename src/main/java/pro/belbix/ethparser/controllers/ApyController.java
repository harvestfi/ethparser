package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.service.ApyService;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
@RequestMapping(value = "/apy")
@Log4j2
public class ApyController {

    private final ApyService apyService;
    private final ContractDbService contractDbService;

    public ApyController(ApyService apyService,
        ContractDbService contractDbService) {
        this.apyService = apyService;
        this.contractDbService = contractDbService;
    }

    @GetMapping(value = "/average/{pool}")
    public RestResponse psApyAverage(
        @PathVariable("pool") String address,
        @RequestParam(value = "days", required = false) String days,
        @RequestParam(value = "network", required = false, defaultValue = ETH_NETWORK) String network
    ) {
        int daysI = 7;
        if (days != null) {
            try {
                daysI = Integer.parseInt(days);
            } catch (Exception e) {
                log.error("Wrong days value", e);
                return RestResponse.error("Wrong days value");
            }
        }
        try {
            if (!address.startsWith("0x")) {
                address = contractDbService.getAddressByName(address, ContractType.VAULT, network)
                    .orElseThrow();
            }
            return RestResponse.ok(String.format("%.8f",
                apyService.averageApyForPool(address, daysI, network)));
        } catch (Exception e) {
            log.error("Error get average apy for " + address + " with days " + days);
            return RestResponse.error("Server error during calculation average apy");
        }
    }

}
