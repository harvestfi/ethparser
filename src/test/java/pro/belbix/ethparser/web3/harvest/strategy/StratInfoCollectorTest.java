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
  void collect_WETH_COMPOUND() throws Exception {
    String strategyAddress = "0x1Dcaf36c8c4222139899690945f4382f298f8735";
    long block = 12517513;
    String network = ETH_NETWORK;
    StratInfo stratInfo = stratInfoCollector.collect(strategyAddress, block, network);
    assertModel(StratInfo.builder()
            .strategyAddress(strategyAddress)
            .block(block)
            .network(network)
            .claimableTokens(0.14907161525799628)
            .apr(0.08648392641207807)
            .build(),
        stratInfo);
  }

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

  @Test
  void collect_CRV_EURS_shouldBeZero() throws Exception {
    String strategyAddress = "0x807a637A1C82ca37d4bac0aB2684b33600c4a60A";
    long block = 12474706;
    String network = ETH_NETWORK;
    StratInfo stratInfo = stratInfoCollector.collect(strategyAddress, block, network);
    assertModel(StratInfo.builder()
            .strategyAddress(strategyAddress)
            .block(block)
            .network(network)
            .apr(0.0)
            .build(),
        stratInfo);
  }

  @Test
  void collect_CRV_EURS() throws Exception {
    String strategyAddress = "0x807a637A1C82ca37d4bac0aB2684b33600c4a60A";
    long block = 12504539;
    String network = ETH_NETWORK;
    StratInfo stratInfo = stratInfoCollector.collect(strategyAddress, block, network);
    assertModel(StratInfo.builder()
            .strategyAddress(strategyAddress)
            .block(block)
            .network(network)
            .rewardTokenPrice(1.5288226293846845)
            .rewardTokenName("Curve DAO Token")
            .rewardTokenAddress("0xd533a949740bb3306d119cc777fa900ba034cd52")
            .apr(19.225898342222536)
            .build(),
        stratInfo);
  }


}
