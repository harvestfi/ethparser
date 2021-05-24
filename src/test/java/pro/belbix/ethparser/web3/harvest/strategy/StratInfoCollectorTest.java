package pro.belbix.ethparser.web3.harvest.strategy;

import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.StratInfo;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class StratInfoCollectorTest {

  @Autowired
  private StratInfoCollector stratInfoCollector;

  @Test
  void collect_CRV_GUSD() throws Exception {
    String strategyAddress = "0xA505917C1326670451EfF9ea75FE0d49a3853acF";
    long block = 12402140;
    String network = ETH_NETWORK;
    StratInfo stratInfo = stratInfoCollector.collect(strategyAddress, block, network);
    assertModel(StratInfo.builder()
            .strategyAddress(strategyAddress)
            .block(block)
            .network(network)
            .apr(12.197759919575764)
            .build(),
        stratInfo);
  }


}
