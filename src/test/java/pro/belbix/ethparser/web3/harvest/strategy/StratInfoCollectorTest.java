package pro.belbix.ethparser.web3.harvest.strategy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.StratInfo;
import pro.belbix.ethparser.model.StratRewardInfo;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class StratInfoCollectorTest {

  @Autowired
  private StratInfoCollector stratInfoCollector;

  @Test
  void collect_IDLE_USDC() throws Exception {
    String strategyAddress = "0xa5F125c0D571FD67D564C05a234E9a6f4E5d0624";
    long block = 12509892;
    String network = ETH_NETWORK;
    StratInfo stratInfo = stratInfoCollector.collect(strategyAddress, block, network);
    assertAll(
        () -> assertModel(StratInfo.builder()
                .strategyAddress(strategyAddress)
                .block(block)
                .network(network)
                .apr(2.5538694223189324)
                .build(),
            stratInfo),
        () -> assertModel(StratRewardInfo.builder()
            .name("Compound")
            .amount(1.62177931868884E-4)
            .amountUsd(0.07488118119033772)
            .build(), stratInfo.getRewardTokens().get(0)),
        () -> assertModel(StratRewardInfo.builder()
            .name("Aave Token")
            .amount(1.37006599596929E-4)
            .amountUsd(0.053431010887844)
            .build(), stratInfo.getRewardTokens().get(1)),
        () -> assertModel(StratRewardInfo.builder()
            .name("Idle")
            .amount(363.5427787384573)
            .amountUsd(3396.5884878178)
            .build(), stratInfo.getRewardTokens().get(2))
    );
  }

  @Test
  void collect_WETH_COMPOUND() throws Exception {
    String strategyAddress = "0x1Dcaf36c8c4222139899690945f4382f298f8735";
    long block = 12502649;
    String network = ETH_NETWORK;
    StratInfo stratInfo = stratInfoCollector.collect(strategyAddress, block, network);
    assertModel(StratInfo.builder()
            .strategyAddress(strategyAddress)
            .block(block)
            .network(network)
            .apr(0.07059267359362291)
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
            .apr(19.225898342222536)
            .build(),
        stratInfo);
  }


}
