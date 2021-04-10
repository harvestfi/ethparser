package pro.belbix.ethparser.web3.layers.detector;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertTwoArrays;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthAddressEntity;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.b_layer.ContractLogEntity;
import pro.belbix.ethparser.entity.b_layer.ContractStateEntity;
import pro.belbix.ethparser.entity.b_layer.ContractTxEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class EthContractDetectorTest {

  @Autowired
  private ContractDetector contractDetector;
  @Autowired
  private EthBlockParser ethBlockParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private ContractLoader contractLoader;
  @Autowired
  private ContractEventsDbService contractEventsDbService;
  @Autowired
  private EthBlockDbService ethBlockDbService;
  @Autowired
  private EthBlockRepository ethBlockRepository;

  @BeforeEach
  void setUp() {
    contractLoader.load();
  }

  @Test
  void handleBlock_12055610_empty() throws JsonProcessingException {
    EthBlockEntity ethBlockEntity =
        loadBlock("0x8de7a4d9064e0a12b94ab833ca99ac76aecdb61c71e58926d256f92f8fb04dbe");
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity, ETH_NETWORK);
    assertTrue(events.isEmpty());
  }

  @Test
  void handleBlock_10800000() throws JsonProcessingException {
    EthBlockEntity ethBlockEntity =
        loadBlock("0xf0efb2f2c63adf9f09a6fc05808985bb46896c11d83659b51a49d6f96d3053d7");
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity, ETH_NETWORK);
//    System.out.println(new ObjectMapper().writeValueAsString(events));

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
            "0xb4e16d0168e52d35cacd2c6185b44281ec28c9dc".toLowerCase(),
            "0xa2107fa5b38d9bbd2c461d6edf11b11a50f6b974".toLowerCase(),
            "0x514910771af9ca656af840dff83e8264ecf986ca".toLowerCase()
        )));

    assertEvents(events, ContractEventAssertion.builder()
        .eventSize(12)
        .eventContractAddress("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
        .txSize(15)
        .txAddress("0xf9fba9a4ed29d8dfc8895737e62f71957abcfb64f503864fc5f68230efa33abf")
        .stateSize(4)
        .stateName("symbol")
        .stateValue("[\"WETH\"]")
        .logSize(5)
        .logIdx(5)
        .logAddress("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
        .logName("Withdrawal")
        .logValues("[\"0x7a250d5630b4cf539739df2c5dacb4c659f2488d\",\"13134584198476690558\"]")
        .logMethodId("0x7fcf532c")
        .funcHex("0x18cbafe5")
        .funcName("swapExactTokensForETH")
        .funcData("[\"5000000000\",\"13092066704280485888\",\"[\\\"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48\\\",\\\"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2\\\"]\",\"0x19ae14a6aeb13b2bf2307bf010a329831a1cfbfe\",\"1599290553\"]")
        .build()
    );
  }

  @Test
  void handleBlock_SUSHI_HODL() throws JsonProcessingException {
    EthBlockEntity ethBlockEntity =
        loadBlock("0x69d416e65f5997b22cd17dc1f27544407db08901cda211526b4b5a14fd2247c1");

    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity, ETH_NETWORK);
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
            "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599".toLowerCase(),
            "0xa2107fa5b38d9bbd2c461d6edf11b11a50f6b974".toLowerCase(),
            "0x514910771af9ca656af840dff83e8264ecf986ca".toLowerCase()
        )));

    assertEvents(events, ContractEventAssertion.builder()
        .eventSize(12)
        .eventContractAddress("0x274aa8b58e8c57c4e347c8768ed853eb6d375b48")
        .txSize(1)
        .txAddress("0x8b94d75dd5f3e4db2fc1ebcd9752aec88012760ba476ad31e9c216a085fdeddd")
        .stateSize(22)
        .stateName("nextImplementationDelay")
        .stateValue("[\"43200\"]")
        .logIdx(239)
        .logSize(3)
        .logAddress("0x274aa8b58e8c57c4e347c8768ed853eb6d375b48")
        .logName("Withdraw")
        .logMethodId("0x884edad9")
        .logValues("[\"0x1e7e3925012ac4fc2e35fe23415c877979eb6b04\",\"954273586164387783198\"]")
        .funcHex("0x2e1a7d4d")
        .funcName("withdraw")
        .funcData("[\"953887111471288279008\"]")
        .build()
    );
  }

  @Test
  void testEligibleContracts_SUSHI_HODL_12030868() {
    EthBlockEntity ethBlockEntity =
        loadBlock("0x69d416e65f5997b22cd17dc1f27544407db08901cda211526b4b5a14fd2247c1");
    assertTwoArrays(contractDetector.collectEligible(ethBlockEntity, ETH_NETWORK).component1().stream()
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
            "0x274aa8b58e8c57c4e347c8768ed853eb6d375b48".toLowerCase(),
            "0xa2107fa5b38d9bbd2c461d6edf11b11a50f6b974".toLowerCase(),
            "0x514910771af9ca656af840dff83e8264ecf986ca".toLowerCase()
        )));
  }

  private EthBlockEntity loadBlock(String hash) {
    EthBlockEntity ethBlockEntity =
        ethBlockParser.parse(web3Functions.findBlockByHash(hash, true, ETH_NETWORK), ETH_NETWORK);
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
            "Duplicate should be null"),
        () -> ethBlockRepository.deleteById(event.getBlock().getNumber())
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
