package pro.belbix.ethparser.web3.harvest.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.web3j.protocol.ObjectMapperFactory;
import pro.belbix.ethparser.entity.StratInfo;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.repositories.StratInfoRepository;

@Service
@Log4j2
public class StratInfoDbService {

  private final StratInfoRepository stratInfoRepository;
  private final AppProperties appProperties;

  public StratInfoDbService(
      StratInfoRepository stratInfoRepository,
      AppProperties appProperties) {
    this.stratInfoRepository = stratInfoRepository;
    this.appProperties = appProperties;
  }

  public boolean save(StratInfo stratInfo) {
    if (stratInfoRepository.existsById(stratInfo.getId())
        && !appProperties.isOverrideDuplicates()) {
      log.warn("Duplicate strat info id {}", stratInfo.getId());
      return false;
    }
    try {
      stratInfo.setRewardTokensRaw(ObjectMapperFactory.getObjectMapper().writeValueAsString(
          stratInfo.getRewardTokens()
      ));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    stratInfoRepository.save(stratInfo);
    return true;
  }
}
