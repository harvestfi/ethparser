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
import pro.belbix.ethparser.web3.Web3Service;
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
  private Web3Service web3Service;
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
            List.of("Swap"),
            0,
            999999999,
            PageRequest.of(0, 1)
        );
    assertNotNull(uniPrices);
    assertEquals(uniPrices.size(), 1);
    UniPriceViewEntity uniPriceView = uniPrices.get(0);
    assertAll(
        () -> assertEquals("0xf4705727dccf0735ffcdc0d573b259e8c7b63d6c7ea4560561b0586a2f0b758a",
            uniPriceView.getBlockHash(), "block hash"),
        () -> assertEquals(12080691, uniPriceView.getBlockNumber().longValue(), "block number"),
        () -> assertEquals("0x0d4a11d5eeaac28ec3f61d100daf4d40471f1852", uniPriceView.getAddress(),
            "address"),
        () -> assertEquals("UNI_LP_ETH_USDT", uniPriceView.getName(), "name"),
        () -> assertEquals("0x3f34b81aa0086d8f1ee93faeb622ee727b1f01f73afff600c1a80e77c482d73b", uniPriceView.getTxHash(), "tx hash"),
        () -> assertEquals("swapExactTokensForTokens", uniPriceView.getFuncName(), "getFuncName"),
        () -> assertEquals("Swap", uniPriceView.getLogName(), "getLogName"),
        () -> assertEquals("0x7a250d5630b4cf539739df2c5dacb4c659f2488d", uniPriceView.getSender(),
            "getSender"),
        () -> assertEquals("0xa986f2a12d85c44429f574ba50c0e21052b18ba1", uniPriceView.getToAdr(), "getToAdr"),
        () -> assertEquals("0", uniPriceView.getAmount0In(), "getAmount0In"),
        () -> assertEquals("430797240854423828", uniPriceView.getAmount0Out(), "getAmount0Out"),
        () -> assertEquals("770016113", uniPriceView.getAmount1In(), "getAmount1In"),
        () -> assertEquals("0", uniPriceView.getAmount1Out(), "getAmount1Out")
    );
  }

  private void loadBlock(long blockNumber) {
    EthBlockEntity ethBlockEntity =
        ethBlockParser.parse(web3Service.findBlockByNumber(blockNumber, true));
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity);
    if (ethBlockEntity == null) {
      ethBlockEntity = ethBlockRepository.findById(blockNumber).orElseThrow();
    }
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
    assertFalse(events.isEmpty(), "Not found eligible blocks");
    events.forEach(e -> contractEventsDbService.save(e));
  }
}
