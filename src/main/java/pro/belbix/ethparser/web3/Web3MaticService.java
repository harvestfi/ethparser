package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.properties.NetworkProperties;

@Service
@Log4j2
public class Web3MaticService extends Web3Service {

  public Web3MaticService(AppProperties appProperties, NetworkProperties networkProperties) {
    super(MATIC_NETWORK, appProperties, networkProperties);
  }

  @PreDestroy
  private void disconnect() {
    super.close();
  }
}
