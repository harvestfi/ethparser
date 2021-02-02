package pro.belbix.ethparser.controllers;

import static pro.belbix.ethparser.utils.CommonUtils.parseLong;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.repositories.UniswapRepository.OhlcProjection;
import pro.belbix.ethparser.web3.uniswap.db.UniswapDbService;

@RestController
public class UniController {

    private final UniswapRepository uniswapRepository;
    private final UniswapDbService uniswapDbService;

    public UniController(UniswapRepository uniswapRepository,
                         UniswapDbService uniswapDbService) {
        this.uniswapRepository = uniswapRepository;
        this.uniswapDbService = uniswapDbService;
    }

    @RequestMapping(value = "api/transactions/history/uni", method = RequestMethod.GET)
    public Iterable<UniswapDTO> uniswapHistoryData(@RequestParam(value = "from", required = false) String from,
                                                   @RequestParam(value = "to", required = false) String to) {
        return uniswapDbService.fetchUni(from, to);
    }

    @RequestMapping(value = "api/transactions/history/uni/ohcl/{name}", method = RequestMethod.GET)
    public Iterable<OhlcProjection> ohclUniswapTx(@PathVariable("name") String name,
                                                  @RequestParam(value = "start", required = false) String start,
                                                  @RequestParam(value = "end", required = false) String end) {
        return uniswapRepository.fetchOHLCTransactions(name, parseLong(start, 0), parseLong(end, Long.MAX_VALUE), 3600);
    }

    @GetMapping("/history/uni/{address}")
    public List<UniswapDTO> addressHistoryUni(@PathVariable("address") String address,
                                              @RequestParam(value = "from", required = false) String from,
                                              @RequestParam(value = "to", required = false) String to) {
        return uniswapRepository.fetchAllByOwner(address, parseLong(from, 0), parseLong(to, Long.MAX_VALUE));
    }

}
