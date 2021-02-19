package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.v0.HardWorkDTO;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.repositories.v0.HardWorkRepository;

@ConditionalOnExpression("!${ethparser.onlyParse:false}")
@RestController
@Log4j2
public class HardWorkController {

    private final HardWorkRepository hardWorkRepository;

    public HardWorkController(HardWorkRepository hardWorkRepository) {
        this.hardWorkRepository = hardWorkRepository;
    }

    @RequestMapping(value = "api/transactions/last/hardwork", method = RequestMethod.GET)
    public List<HardWorkDTO> lastHardWork() {
        return hardWorkRepository.fetchLatest();
    }

    @RequestMapping(value = "api/transactions/history/hardwork/{name}", method = RequestMethod.GET)
    public List<HardWorkDTO> historyHardWork(@PathVariable("name") String name,
                                             @RequestParam(value = "start", required = false) String start,
                                             @RequestParam(value = "end", required = false) String end) {
        return hardWorkRepository.findAllByVaultOrderByBlockDate(name, parseLong(start, 0), parseLong(end, Long.MAX_VALUE));
    }

    @RequestMapping(value = "api/transactions/history/hardwork", method = RequestMethod.GET)
    public List<HardWorkDTO> historyHardWork(@RequestParam(value = "from", required = false) String from,
                                             @RequestParam(value = "to", required = false) String to) {
        long fromL = 0L;
        long toL = Long.MAX_VALUE;
        if (from != null) {
            fromL = Long.parseLong(from);
        }
        if (to != null) {
            toL = Long.parseLong(to);
        }

        return hardWorkRepository.fetchAllInRange(fromL, toL);
    }

    @RequestMapping(value = "last_saved_gas_sum", method = RequestMethod.GET)
    public RestResponse lastSavedGasSum() {
        try {
            return RestResponse.ok((String.format("%.8f", hardWorkRepository.fetchLastGasSaved())));
        } catch (Exception e) {
            log.error("Error get last saved gas sum", e);
            return RestResponse.error("Server error during getting last saved gas");
        }

    }


}
