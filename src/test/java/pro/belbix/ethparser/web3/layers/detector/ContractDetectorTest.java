package pro.belbix.ethparser.web3.layers.detector;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertTwoArrays;
import static pro.belbix.ethparser.web3.layers.detector.ContractDetector.collectEligibleContracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
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
  void handleBlock_12055610_empty() throws JsonProcessingException {
    EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
        "0x8de7a4d9064e0a12b94ab833ca99ac76aecdb61c71e58926d256f92f8fb04dbe",
        true
    ));
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity).join();
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
    assertTrue(events.isEmpty());
  }

  @Test
  void handleBlock_10800000() throws JsonProcessingException {
    EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
        "0xf0efb2f2c63adf9f09a6fc05808985bb46896c11d83659b51a49d6f96d3053d7",
        true
    ));
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity).join();
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
//    System.out.println(new ObjectMapper().writeValueAsString(events));
    assertEquals(10, collectEligibleContracts(ethBlockEntity).size(),
        "eligible contracts");
    assertTwoArrays(events.stream()
            .map(e -> e.getContract().getAddress())
            .collect(Collectors.toList())
        , new ArrayList<>(Set.of(
            "0xdac17f958d2ee523a2206206994597c13d831ec7".toLowerCase(),
            "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase(),
            "0x6b175474e89094c44da98b954eedeac495271d0f".toLowerCase(),
            "0x514906fc121c7878424a5c928cad1852cc545892".toLowerCase(),
            "0xa0246c9032bc3a600820415ae600c6388619a14d".toLowerCase(),
            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase(),
            "0x0000000000085d4780b73119b644ae5ecd22b376".toLowerCase(),
            "0x6b3595068778dd592e39a122f4f5a5cf09c90fe2".toLowerCase(),
            "0x0d4a11d5eeaac28ec3f61d100daf4d40471f1852".toLowerCase(),
            "0xb4e16d0168e52d35cacd2c6185b44281ec28c9dc".toLowerCase()
        )));

    assertEvents(events, AssertData.builder()
        .eventSize(10)
        .eventNum(1)
        .eventContractAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48")
        .txSize(8)
        .txNum(1)
        .txAddress("0xd997fdbf9ca2d31699214b73e3172ff187f83785cba262ced03129a83216b4dd")
        .stateSize(18)
        .stateNum(17)
        .stateName("DECREASE_ALLOWANCE_WITH_AUTHORIZATION_TYPEHASH")
        .stateValue("b70559e94cbda91958ebec07f9b65b3b490097c8d25c8dacd71105df1015b6d8")
        .logSize(1)
        .logNum(0)
        .logAddress("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48")
        .logTopic("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")
        .logTopics("0x000000000000000000000000b493da310d29d1354096f76b3c2e81cdc9642ab3,0x000000000000000000000000ee44145a12ad57fb9723e05d51c1d0b3fe1ced3d")
        .build()
    );
  }

  @Test
  void handleBlock_SUSHI_HODL() throws JsonProcessingException {
    EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
        "0x69d416e65f5997b22cd17dc1f27544407db08901cda211526b4b5a14fd2247c1",
        true
    ));
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity).join();

    assertEquals(10, collectEligibleContracts(ethBlockEntity).size(),
        "eligible contracts");

    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity);
//    System.out.println(new ObjectMapper().writeValueAsString(events));

    assertTwoArrays(events.stream()
            .map(e -> e.getContract().getAddress())
            .collect(Collectors.toList()),
        new ArrayList<>(Set.of(
            "0xdac17f958d2ee523a2206206994597c13d831ec7".toLowerCase(),
            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase(),
            "0x06da0fd433c1a5d7a4faa01111c044910a184553".toLowerCase(),
            "0x6b175474e89094c44da98b954eedeac495271d0f".toLowerCase(),
            "0x274aa8b58e8c57c4e347c8768ed853eb6d375b48".toLowerCase(),
            "0x6b3595068778dd592e39a122f4f5a5cf09c90fe2".toLowerCase(),
            "0xb4e16d0168e52d35cacd2c6185b44281ec28c9dc".toLowerCase(),
            "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase(),
            "0x0d4a11d5eeaac28ec3f61d100daf4d40471f1852".toLowerCase(),
            "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599".toLowerCase()
        )));

    assertEvents(events, AssertData.builder()
        .eventSize(10)
        .eventNum(4)
        .eventContractAddress("0x274aa8b58e8c57c4e347c8768ed853eb6d375b48")
        .txSize(1)
        .txNum(1)
        .txAddress("0x8b94d75dd5f3e4db2fc1ebcd9752aec88012760ba476ad31e9c216a085fdeddd")
        .stateSize(22)
        .stateNum(1)
        .stateName("nextImplementationDelay")
        .stateValue("43200")
        .logSize(3)
        .logNum(1)
        .logAddress("0x274aa8b58e8c57c4e347c8768ed853eb6d375b48")
        .logTopic("0x884edad9ce6fa2440d8a54cc123490eb96d2768479d49ff9c7366125a9424364")
        .logTopics("0x0000000000000000000000001e7e3925012ac4fc2e35fe23415c877979eb6b04")
        .build()
    );
  }

  @Test
  void testEligibleContracts_SUSHI_HODL_12030868() {
    EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
        "0x69d416e65f5997b22cd17dc1f27544407db08901cda211526b4b5a14fd2247c1",
        true
    ));
    Map<EthAddressEntity, Map<String, EthTxEntity>> eligible =
        ContractDetector.collectEligibleContracts(ethBlockEntity);
//    assertEquals(9, eligible.size(), "eligible contracts size");

    assertTwoArrays(eligible.keySet().stream()
            .map(EthAddressEntity::getAddress)
            .collect(Collectors.toList()),
        new ArrayList<>(Set.of(
            "0xdac17f958d2ee523a2206206994597c13d831ec7".toLowerCase(),
            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2".toLowerCase(),
            "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48".toLowerCase(),
            "0x06da0fd433c1a5d7a4faa01111c044910a184553".toLowerCase(),
            "0x6b175474e89094c44da98b954eedeac495271d0f".toLowerCase(),
            "0xb4e16d0168e52d35cacd2c6185b44281ec28c9dc".toLowerCase(),
            "0x0d4a11d5eeaac28ec3f61d100daf4d40471f1852".toLowerCase(),
            "0x6b3595068778dd592e39a122f4f5a5cf09c90fe2".toLowerCase(),
            "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599".toLowerCase(),
            "0x274aa8b58e8c57c4e347c8768ed853eb6d375b48".toLowerCase()
        )));
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
            "eventContractAddress"),
        () -> assertEquals(data.txSize, event.getTxs().size(), "txSize"),
        () -> assertEquals(data.stateSize, event.getStates().size(), "stateSize"),
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
        () -> assertEquals(data.txAddress, tx.getTx().getHash().getHash(), "txAddress"),
        () -> assertEquals(data.logSize, tx.getTx().getLogs().size(), "logSize"),
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
