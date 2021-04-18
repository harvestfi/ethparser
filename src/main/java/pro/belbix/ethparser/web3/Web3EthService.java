package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pro.belbix.ethparser.properties.AppProperties;

@Service
@Log4j2
public class Web3EthService extends Web3Service {

  public Web3EthService(AppProperties appProperties) {
    super(ETH_NETWORK, appProperties);
  }

  @PreDestroy
  private void disconnect() {
    super.close();
  }
}
