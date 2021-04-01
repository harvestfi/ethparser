package pro.belbix.ethparser.repositories.c_layer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.c_layer.UniPriceViewEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.ContractDetector;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class UniPriceViewRepositoryTest {

  @Autowired
  private UniPriceViewRepository uniPriceViewRepository;
  @Autowired
  private EthBlockRepository ethBlockRepository;
  @Autowired
  private EthBlockParser ethBlockParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private EthBlockDbService ethBlockDbService;
  @Autowired
  private ContractDetector contractDetector;
  @Autowired
  private ContractEventsDbService contractEventsDbService;
  @Autowired
  private ContractLoader contractLoader;

  @BeforeEach
  void setUp() {
    contractLoader.load();
  }

  @Test
  void testUniPriceView_UNI_LP_ETH_USDT() {
    loadBlock(12080691);
    List<UniPriceViewEntity> uniPrices =
        uniPriceViewRepository.findByAddressesAndLogNames(
            List.of("0x0d4a11d5eeaac28ec3f61d100daf4d40471f1852".toLowerCase()),
            0,
            999999999,
            PageRequest.of(0, 1)
        );
    assertNotNull(uniPrices);
    assertEquals(uniPrices.size(), 1);
    UniPriceViewEntity uniPriceView = uniPrices.get(0);
    assertUniPriceView(UniPriceViewEntity.builder()
        .address("0x0d4a11d5eeaac28ec3f61d100daf4d40471f1852")
        .blockNumber(12080691L)
        .blockHash("0xf4705727dccf0735ffcdc0d573b259e8c7b63d6c7ea4560561b0586a2f0b758a")
        .sourceName("UNI_LP_ETH_USDT")
        .txHash("0x3f34b81aa0086d8f1ee93faeb622ee727b1f01f73afff600c1a80e77c482d73b")
        .funcName("swapExactTokensForTokens")
        .sender("0x7a250d5630b4cf539739df2c5dacb4c659f2488d")
        .toAdr("0xa986f2a12d85c44429f574ba50c0e21052b18ba1")
        .amount0In("0")
        .amount1In("770016113")
        .amount0Out("430797240854423828")
        .amount1Out("0")
        .keyTokenName("USDT")
        .keyTokenAddress("0xdac17f958d2ee523a2206206994597c13d831ec7")
        .keyTokenAmount(770.016113)
        .otherTokenName("ETH")
        .otherTokenAddress("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
        .otherTokenAmount(0.43079724085442384)
        .isBuy(0)
        .lpTotalSupply(2.258297259685995)
        .lpToken0Pooled(78832.45578748077)
        .lpToken1Pooled(1.4048485083366E8)
        .build(), uniPriceView);
  }

  void assertUniPriceView(UniPriceViewEntity expected, UniPriceViewEntity actual) {
    assertAll(
        () -> assertEquals(expected.getBlockHash(), actual.getBlockHash(), "block hash"),
        () -> assertEquals(expected.getBlockNumber(), actual.getBlockNumber(), "block number"),
        () -> assertEquals(expected.getAddress(), actual.getAddress(), "address"),
        () -> assertEquals(expected.getSourceName(), actual.getSourceName(), "source name"),
        () -> assertEquals(expected.getTxHash(), actual.getTxHash(), "tx hash"),
        () -> assertEquals(expected.getFuncName(), actual.getFuncName(), "getFuncName"),
        () -> assertEquals(expected.getSender(), actual.getSender(), "getSender"),
        () -> assertEquals(expected.getToAdr(), actual.getToAdr(), "getToAdr"),
        () -> assertEquals(expected.getAmount0In(), actual.getAmount0In(), "getAmount0In"),
        () -> assertEquals(expected.getAmount0Out(), actual.getAmount0Out(), "getAmount0Out"),
        () -> assertEquals(expected.getAmount1In(), actual.getAmount1In(), "getAmount1In"),
        () -> assertEquals(expected.getAmount1Out(), actual.getAmount1Out(), "getAmount1Out"),
        () -> assertEquals(expected.getKeyTokenName(), actual.getKeyTokenName(), "getKeyTokenName"),
        () -> assertEquals(expected.getKeyTokenAddress(), actual.getKeyTokenAddress(),
            "getKeyTokenAddress"),
        () -> assertEquals(expected.getKeyTokenAmount(), actual.getKeyTokenAmount(),
            "getKeyTokenAmount"),
        () -> assertEquals(expected.getOtherTokenName(), actual.getOtherTokenName(),
            "getOtherTokenName"),
        () -> assertEquals(expected.getOtherTokenAddress(), actual.getOtherTokenAddress(),
            "getOtherTokenAddress"),
        () -> assertEquals(expected.getOtherTokenAmount(), actual.getOtherTokenAmount(),
            "getOtherTokenAmount"),
        () -> assertEquals(expected.getIsBuy(), actual.getIsBuy(), "getIsBuy"),
        () -> assertEquals(expected.getLpTotalSupply(), actual.getLpTotalSupply(),
            "getLpTotalSupply"),
        () -> assertEquals(expected.getLpToken0Pooled(), actual.getLpToken0Pooled(),
            "getLpToken0Pooled"),
        () -> assertEquals(expected.getLpToken1Pooled(), actual.getLpToken1Pooled(),
            "getLpToken1Pooled")
    );
  }

  private void loadBlock(long blockNumber) {
    EthBlockEntity ethBlockEntity =
        ethBlockParser.parse(web3Functions.findBlockByNumber(blockNumber, true));
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity);
    if (ethBlockEntity == null) {
      ethBlockEntity = ethBlockRepository.findById(blockNumber).orElseThrow();
    }
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
    assertFalse(events.isEmpty(), "Not found eligible blocks");
    events.forEach(e -> contractEventsDbService.save(e));
  }
}
