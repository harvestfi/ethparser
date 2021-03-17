package pro.belbix.ethparser.web3.layers.detector;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractStateEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
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
  void handleBlock_SUSHI_HODL() throws JsonProcessingException {
    EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
        "0x69d416e65f5997b22cd17dc1f27544407db08901cda211526b4b5a14fd2247c1",
        true
    ));
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity).join();
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
//    System.out.println(new ObjectMapper().writeValueAsString(events));
    assertEvents(events, AssertData.builder()
        .eventSize(10)
        .eventNum(7)
        .eventContractAddress("0x274aa8b58e8c57c4e347c8768ed853eb6d375b48")
        .txSize(1)
        .txNum(1)
        .txAddress("0x8b94d75dd5f3e4db2fc1ebcd9752aec88012760ba476ad31e9c216a085fdeddd")
        .stateSize(22)
        .stateNum(1)
        .stateName("shouldUpgrade")
        .stateValue("Tuple2{value1=false, value2=0x0000000000000000000000000000000000000000}")
        .logSize(3)
        .logNum(1)
        .logAddress("0x6b3595068778dd592e39a122f4f5a5cf09c90fe2")
        .logTopic("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")
        .logTopics("0x000000000000000000000000274aa8b58e8c57c4e347c8768ed853eb6d375b48,0x0000000000000000000000001e7e3925012ac4fc2e35fe23415c877979eb6b04")
        .build()
    );
  }

  private void assertEvents(List<ContractEventEntity> events, AssertData data) {
    assertAll(
        () -> assertNotNull(events, "events is null"),
        () -> assertEquals(data.eventSize, events.size(), "event size"),
        () -> assertEvent(events.get(data.eventNum), data),
        () -> assertNotNull(contractEventsDbService.save(events.get(data.eventNum)),
            "save result"),
        () -> assertNull(contractEventsDbService.save(events.get(data.eventNum)),
            "Duplicate should be null")
    );
  }

  private void assertEvent(ContractEventEntity event, AssertData data) {
    assertAll(
        () -> assertEquals(data.eventContractAddress, event.getContract().getAddress(),
            "contract Address"),
        () -> assertEquals(data.txSize, event.getTxs().size(), "txs"),
        () -> assertEquals(data.stateSize, event.getStates().size(), "states"),
        () -> assertTx(new ArrayList<>(event.getTxs()).get(0), data),
        () -> assertState(new ArrayList<>(event.getStates()).get(data.stateNum), data)
    );
  }

  private void assertState(ContractStateEntity state, AssertData data) {
    assertAll(
        () -> assertEquals(data.stateName, state.getName(), "state name"),
        () -> assertEquals(data.stateValue, state.getValue(), "state value")
    );
  }

  private void assertTx(ContractTxEntity tx, AssertData data) {
    assertAll(
        () -> assertEquals(data.txAddress, tx.getTx().getHash().getHash(), "tx hash"),
        () -> assertEquals(data.logSize, tx.getTx().getLogs().size(), "log size"),
        () -> assertLog(new ArrayList<>(tx.getTx().getLogs()).get(data.logNum), data)
    );
  }

  private void assertLog(EthLogEntity ethLog, AssertData data) {
    assertAll(
        () -> assertEquals(data.logAddress, ethLog.getAddress().getAddress(), "log adr"),
        () -> assertEquals(data.logTopic, ethLog.getFirstTopic().getHash(), "log topic"),
        () -> assertEquals(data.logTopics, ethLog.getTopics(), "log topics")
    );
  }

  @Builder
  private static class AssertData {

    int eventSize;
    int eventNum;
    String eventContractAddress;
    int txSize;
    int txNum;
    String txAddress;
    int stateSize;
    int stateNum;
    String stateName;
    String stateValue;
    int logSize;
    int logNum;
    String logAddress;
    String logTopic;
    String logTopics;
  }
}
