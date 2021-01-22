package pro.belbix.ethparser.controllers;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.repositories.HardWorkRepository;

@RestController
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
    public List<HardWorkDTO> historyHardWork(@PathVariable("name") String name) {
        return hardWorkRepository.findAllByVaultOrderByBlockDate(name);
    }

    @RequestMapping(value = "api/transactions/history/hardwork", method = RequestMethod.GET)
    public List<HardWorkDTO> historyHardWork() {
        return hardWorkRepository.findAllByOrderByBlockDate();
    }


}
