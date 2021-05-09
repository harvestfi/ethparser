package pro.belbix.ethparser.web3.layers.detector;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static pro.belbix.ethparser.TestUtils.assertTwoArrays;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractStateEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class BscContractDetectorTest {

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

  @Test
  @Disabled("Returns missing trie node error")
  void handleBlock_6101208_PC_BUSD_BNB() {
    EthBlockEntity ethBlockEntity = loadBlock(6101208);
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity, BSC_NETWORK);

    assertTwoArrays(events.stream()
            .map(e -> e.getContract().getAddress())
            .collect(Collectors.toList())
        , new ArrayList<>(Set.of(
            "0xbb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c",
            "0x55d398326f99059ff775485246999027b3197955",
            "0xe9e7cea3dedca5984780bafc599bd69add087d56",
            "0xa527a61703d82139f8a06bc30097cc9caa2df5a6",
            "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82",
            "0x20bcc3b8a0091ddac2d0bc30f68e6cbb97de59cd",
            "0x2170ed0880ac9a755fd29b2688956bd959f933f8",
            "0x7561eee90e24f3b348e1087a005f78b4c8453524",
            "0x1b96b92314c44b159149f7e0303511fb2fc4774f",
            "0x7130d2a12b9bcbfae4f2634d864a1ee1ce3ead9c",
            "0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34",
            "0x41182c32f854dd97ba0e0b1816022e0acb2fc0bb",
            "0xcf6bb5389c92bdda8a3747ddb454cb7a64626c63",
            "0x3ab77e40340ab084c3e23be8e5a6f7afed9d41dc",
            "0x1af3f329e8be154074d8769d1ffa4ee058b1dbc3",
            "0x0abd3e3502c15ec252f90f64341cba74a24fba06",
            "0x6b936c5c1fd7de08e03684b0588a87dbd8ce6b63",
            "0xaf4de8e872131ae328ce21d909c74705d3aaf452"
        )));

    assertEvents(events, ContractEventAssertion.builder()
        .eventSize(18)
        .eventContractAddress("0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34")
        .txSize(1)
        .txAddress("0x2beda2c174968f2f7170d1a1722d29f0a15d29104cb1ff3ae8a63247a9656af2")
        .stateSize(22)
        .stateName("symbol")
        .stateValue("[\"bfCake-LP\"]")
        .logSize(3)
        .logIdx(137)
        .logAddress("0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34")
        .logName("Transfer")
        .logValues("[\"0x0000000000000000000000000000000000000000\",\"0x7f4ac7a8b18d7dc76c5962aa1aacf968eac3ac67\",\"266204189249832085\"]")
        .logMethodId("0xddf252ad")
        .funcHex("0xb6b55f25")
        .funcName("deposit")
        .funcData("[\"266204189249832085\"]")
        .build()
    );
  }


  private EthBlockEntity loadBlock(int block) {
    EthBlockEntity ethBlockEntity =
        ethBlockParser.parse(
            web3Functions.findBlockByNumber(block, true, BSC_NETWORK),
            BSC_NETWORK);
    long blockNumber = ethBlockEntity.getNumber();
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity);
    if (ethBlockEntity == null) {
      ethBlockEntity = ethBlockRepository.findById(blockNumber).orElseThrow();
    }
    return ethBlockEntity;
  }

  private void assertEvents(List<ContractEventEntity> events, ContractEventAssertion data) {
    ContractEventEntity event = events.stream()
        .filter(e -> e.getContract().getAddress().equals(data.eventContractAddress))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("event not found " + data.eventContractAddress));
    assertAll(
        () -> assertNotNull(events, "events is null"),
        () -> assertEquals(data.eventSize, events.size(), "event size"),
        () -> assertEvent(event, data),
        () -> assertNotNull(contractEventsDbService.save(event),
            "save result"),
        () -> assertNull(contractEventsDbService.save(event),
            "Duplicate should be null")
//        () -> ethBlockRepository.deleteById(event.getBlock().getNumber())
    );
  }

  private void assertEvent(ContractEventEntity event, ContractEventAssertion data) {
    assertAll(
        () -> assertEquals(data.eventContractAddress, event.getContract().getAddress(),
            "eventContractAddress"),
        () -> assertEquals(data.txSize, event.getTxs().size(), "txSize"),
        () -> assertEquals(data.stateSize, event.getStates().size(), "stateSize"),
        () -> assertTx(event.getTxs().stream()
            .filter(t -> t.getTx().getHash().getHash().equals(data.txAddress))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("tx not found " + data.txAddress)), data),
        () -> assertState(event.getStates().stream()
            .filter(s -> s.getName().equals(data.stateName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("state not found " + data.stateName)), data)
    );
  }

  private void assertState(ContractStateEntity state, ContractEventAssertion data) {
    assertAll(
        () -> assertEquals(data.stateName, state.getName(), "state name"),
        () -> assertEquals(data.stateValue, state.getValue(), "state value")
    );
  }

  private void assertTx(ContractTxEntity tx, ContractEventAssertion data) {
    assertAll(
        () -> assertEquals(data.txAddress, tx.getTx().getHash().getHash(), "txAddress"),
        () -> assertEquals(data.funcHex, tx.getFuncHash().getMethodId(), "funcHex"),
        () -> assertEquals(data.funcName, tx.getFuncHash().getName(), "funcName"),
        () -> assertEquals(data.funcData, tx.getFuncData(), "funcData"),
        () -> assertEquals(data.logSize, tx.getTx().getLogs().size(), "logSize"),
        () -> assertLog(tx.getLogs().stream()
            .filter(l -> l.getLogIdx() == data.logIdx)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("log not found " + data.logIdx)), data)
    );
  }

  private void assertLog(ContractLogEntity ethLog, ContractEventAssertion data) {
    assertAll(
        () -> assertEquals(data.logAddress, ethLog.getAddress().getAddress(), "log address"),
        () -> assertEquals(data.logName, ethLog.getTopic().getMethodName(), "log name"),
        () -> assertEquals(data.logMethodId, ethLog.getTopic().getMethodId(), "logMethodId"),
        () -> assertEquals(data.logValues, ethLog.getLogs(), "log values")
    );
  }
}
