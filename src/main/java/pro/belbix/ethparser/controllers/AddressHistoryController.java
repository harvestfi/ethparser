package pro.belbix.ethparser.controllers;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;

@RestController
@RequestMapping(value = "/history")
public class AddressHistoryController {

    private final HarvestRepository harvestRepository;
    private final UniswapRepository uniswapRepository;

    public AddressHistoryController(HarvestRepository harvestRepository,
                                    UniswapRepository uniswapRepository) {
        this.harvestRepository = harvestRepository;
        this.uniswapRepository = uniswapRepository;
    }

    @GetMapping("harvest/{address}")
    public List<HarvestDTO> addressHistoryHarvest(@PathVariable("address") String address,
                                                  @RequestParam("from") String from,
                                                  @RequestParam("to") String to) {
        return harvestRepository.fetchAllByOwner(address, parseFrom(from), parseTo(to));
    }

    @GetMapping("uni/{address}")
    public List<UniswapDTO> addressHistoryUni(@PathVariable("address") String address,
                                              @RequestParam("from") String from,
                                              @RequestParam("to") String to) {
        return uniswapRepository.fetchAllByOwner(address, parseFrom(from), parseTo(to));
    }

    private long parseFrom(String from) {
        if (from != null) {
            return Long.parseLong(from);
        }
        return 0;
    }

    private long parseTo(String to) {
        if (to != null) {
            return Long.parseLong(to);
        }
        return Long.MAX_VALUE;
    }

}
