package pro.belbix.ethparser.web3.layers.detector;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class ContractDetectorTest {

  @Autowired
  private ContractDetector contractDetector;
  @Autowired
  private EthBlockParser ethBlockParser;
  @Autowired
  private Web3Service web3Service;

  @Test
  void test() {
    EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
        "0xd196b8cf54b90d1ac25cfb2929c9138dfe3cd83f7e4759d0c7449c90df8982e2",
        true
    ));
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
    assertNotNull(events);
  }
}
