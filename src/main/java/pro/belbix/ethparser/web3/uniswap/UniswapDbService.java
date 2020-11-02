package pro.belbix.ethparser.web3.uniswap;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.model.UniswapDTO;
import pro.belbix.ethparser.repositories.UniswapRepository;

@Service
public class UniswapDbService {

    private static final Logger log = LoggerFactory.getLogger(UniswapDbService.class);
    private final UniswapRepository uniswapRepository;

    public UniswapDbService(
        UniswapRepository uniswapRepository) {
        this.uniswapRepository = uniswapRepository;
    }

    public boolean saveUniswapDto(UniswapDTO dto) {
        Integer ownerCount = uniswapRepository.fetchOwnerCount();
        if (ownerCount == null) {
            ownerCount = 0;
        }
        dto.setOwnerCount(ownerCount);
        if (uniswapRepository.existsById(dto.getId())) {
            log.info("Duplicate tx " + dto.getId());
            return false;
        }
        uniswapRepository.save(dto);
        return true;
    }

    public BigInteger lastBlock() {
        UniswapDTO dto = uniswapRepository.findFirstByOrderByBlockDesc();
        if (dto == null) {
            return BigInteger.ONE;
        }
        return dto.getBlock();
    }

}
