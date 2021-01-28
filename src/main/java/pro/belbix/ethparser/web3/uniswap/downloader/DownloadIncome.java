package pro.belbix.ethparser.web3.uniswap.downloader;

import static pro.belbix.ethparser.web3.uniswap.db.IncomeDBService.DO_HARD_WORK;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.repositories.UniswapRepository;
import pro.belbix.ethparser.web3.uniswap.db.IncomeDBService;

@Service
@Log4j2
public class DownloadIncome {

    private final UniswapRepository uniswapRepository;
    private final IncomeDBService incomeDBService;

    public DownloadIncome(UniswapRepository uniswapRepository,
                          IncomeDBService incomeDBService) {
        this.uniswapRepository = uniswapRepository;
        this.incomeDBService = incomeDBService;
    }

    public void start() {
        List<UniswapDTO> trades = uniswapRepository.findAllByOwnerAndCoinOrderByBlockDate(DO_HARD_WORK, "FARM");
        for (UniswapDTO trade : trades) {
            incomeDBService.saveIncome(trade);
            log.info("Save income for " + trade.print());
        }
    }

}
