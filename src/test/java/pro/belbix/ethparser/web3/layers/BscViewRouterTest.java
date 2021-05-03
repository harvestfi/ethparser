package pro.belbix.ethparser.web3.layers;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.ContractDetector;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class BscViewRouterTest {

  @Autowired
  private ContractDetector contractDetector;
  @Autowired
  private EthBlockParser ethBlockParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private ContractEventsDbService contractEventsDbService;
  @Autowired
  private EthBlockDbService ethBlockDbService;
  @Autowired
  private EthBlockRepository ethBlockRepository;
  @Autowired
  private ViewRouter viewRouter;

  @Test
  void test() {
    List<ContractEventEntity> events = loadBlock(5942659);
    for (ContractEventEntity event : events) {
      viewRouter.route(event, BSC_NETWORK);
    }
  }

  private List<ContractEventEntity> loadBlock(int block) {
    EthBlockEntity ethBlockEntity =
        ethBlockParser.parse(
            web3Functions.findBlockByNumber(block, true, BSC_NETWORK),
            BSC_NETWORK);
    long blockNumber = ethBlockEntity.getNumber();
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity);
    if (ethBlockEntity == null) {
      ethBlockEntity = ethBlockRepository.findById(blockNumber).orElseThrow();
    }
    return contractDetector.handleBlock(ethBlockEntity, BSC_NETWORK);
  }
}
