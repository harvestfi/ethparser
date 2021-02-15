package pro.belbix.ethparser.controllers;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.service.ApyService;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
@RequestMapping(value = "/apy")
@Log4j2
public class ApyController {

    private final ApyService apyService;

    public ApyController(ApyService apyService) {
        this.apyService = apyService;
    }

    @GetMapping(value = "/average/{pool}")
    public RestResponse psApyAverage(@PathVariable("pool") String pool,
                                     @RequestParam(value = "days", required = false) String days
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
            return RestResponse.ok(String.format("%.8f", apyService.averageApyForPool(pool, daysI)));
        } catch (Exception e) {
            log.error("Error get average apy for " + pool + " with days " + days);
            return RestResponse.error("Server error during calculation average apy");
        }
    }

}
