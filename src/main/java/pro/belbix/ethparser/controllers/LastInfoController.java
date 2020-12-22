package pro.belbix.ethparser.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.HardWorkDTO;
import pro.belbix.ethparser.repositories.HardWorkRepository;

@RestController
@RequestMapping(value = "/last")
public class LastInfoController {

    private final HardWorkRepository hardWorkRepository;

    public LastInfoController(HardWorkRepository hardWorkRepository) {
        this.hardWorkRepository = hardWorkRepository;
    }

    @GetMapping(value = "/hardwork")
    public HardWorkDTO lastHardWork() {
        return hardWorkRepository.findFirstByOrderByBlockDateDesc();
    }

}
