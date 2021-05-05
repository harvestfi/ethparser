package pro.belbix.ethparser.web3.uniswap.db;

import static java.time.temporal.ChronoUnit.DAYS;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.v0.UniswapRepository;

@Service
@Log4j2
public class UniswapDbService {

  private final UniswapRepository uniswapRepository;
  private final AppProperties appProperties;

  public UniswapDbService(UniswapRepository uniswapRepository, AppProperties appProperties) {
    this.uniswapRepository = uniswapRepository;
    this.appProperties = appProperties;
  }

  public boolean saveUniswapDto(UniswapDTO dto) {
    if (!appProperties.isOverrideDuplicates() && uniswapRepository.existsById(dto.getId())) {
      log.warn("Duplicate tx " + dto.getId());
      return false;
    }
    uniswapRepository.saveAndFlush(dto);
    fillOwnersCount(dto);
    uniswapRepository.saveAndFlush(dto);
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
    UniswapDTO dto = uniswapRepository.findFirstByOrderByBlockDesc();
    if (dto == null) {
      return BigInteger.valueOf(10765094L);
    }
    return dto.getBlock();
  }

  public List<UniswapDTO> fetchUni(String from, String to) {
    if (from == null && to == null) {
      return uniswapRepository.fetchAllFromBlockDate(
          Instant.now().minus(1, DAYS).toEpochMilli() / 1000);
    }
    long fromI = 0;
    long toI = Integer.MAX_VALUE;
    if (from != null) {
      fromI = Long.parseLong(from);
    }
    if (to != null) {
      toI = Long.parseLong(to);
    }
    return uniswapRepository.fetchAllByPeriod(fromI, toI);
  }

}
