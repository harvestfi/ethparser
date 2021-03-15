package pro.belbix.ethparser.web3.layers.detector;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class ContractDetectorTest {

  @Autowired
  private ContractDetector contractDetector;
  @Autowired
  private EthBlockParser ethBlockParser;
  @Autowired
  private Web3Service web3Service;
  @Autowired
  private ContractLoader contractLoader;
  @Autowired
  private ContractEventsDbService contractEventsDbService;
  @Autowired
  private EthBlockDbService ethBlockDbService;

  @BeforeEach
  void setUp() {
    contractLoader.load();
  }

  @Test
  void handleBlockTest() {
    EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
        "0x69d416e65f5997b22cd17dc1f27544407db08901cda211526b4b5a14fd2247c1",
        true
    ));
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity).join();
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
    assertNotNull(events);
    assertEquals(10, events.size());
    ContractEventEntity event = events.get(0);
    assertEvent(event);
    contractEventsDbService.save(event);

    assertNull(contractEventsDbService.save(event), "Duplicate should be null");
  }

  private void assertEvent(ContractEventEntity event) {
    assertAll(
        () -> assertEquals("0xdac17f958d2ee523a2206206994597c13d831ec7",
            event.getContract().getAddress(), "contract Address"),
        () -> assertEquals(17, event.getTxs().size(), "txs"),
        () -> assertEquals(13, event.getStates().size(), "states")
    );
  }
}
