package pro.belbix.ethparser.web3.uniswap.db;

import static java.time.temporal.ChronoUnit.DAYS;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.UniswapDTO;
import pro.belbix.ethparser.entity.IncomeEntity;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.HarvestRepository;
import pro.belbix.ethparser.repositories.UniswapRepository;

@Service
@Log4j2
public class UniswapDbService {

    private final UniswapRepository uniswapRepository;
    private final AppProperties appProperties;
    private final IncomeDBService incomeDBService;

    public UniswapDbService(UniswapRepository uniswapRepository,
                            AppProperties appProperties,
                            IncomeDBService incomeDBService) {
        this.uniswapRepository = uniswapRepository;
        this.appProperties = appProperties;
        this.incomeDBService = incomeDBService;
    }

    public boolean saveUniswapDto(UniswapDTO dto) {
        if (!appProperties.isOverrideDuplicates() && uniswapRepository.existsById(dto.getId())) {
            log.warn("Duplicate tx " + dto.getId());
            return false;
        }
        uniswapRepository.save(dto);
        uniswapRepository.flush();

        fillOwnersCount(dto);
        uniswapRepository.save(dto);
        uniswapRepository.flush();

        if (incomeDBService.saveIncome(dto)) {
            uniswapRepository.save(dto);
        }
        return true;
    }

    public void fillOwnersCount(UniswapDTO dto) {
        Integer ownerCount = uniswapRepository.fetchOwnerCount(dto.getBlockDate());
        if (ownerCount == null) {
            ownerCount = 0;
        }
        dto.setOwnerCount(ownerCount);
    }

    public BigInteger lastBlock() {
        UniswapDTO dto = uniswapRepository.findFirstByCoinOrderByBlockDesc("FARM");
        if (dto == null) {
            return BigInteger.ONE;
        }
        return dto.getBlock();
    }

    public List<UniswapDTO> fetchUni(String from, String to) {
        if (from == null && to == null) {
            return uniswapRepository.fetchAllFromBlockDate(
                Instant.now().minus(1, DAYS).toEpochMilli() / 1000);
        }
        int fromI = 0;
        int toI = Integer.MAX_VALUE;
        if (from != null) {
            fromI = Integer.parseInt(from);
        }
        if (to != null) {
            toI = Integer.parseInt(to);
        }
        return uniswapRepository.fetchAllByPeriod(fromI, toI);
    }

}
