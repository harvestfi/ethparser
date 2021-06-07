package pro.belbix.ethparser.web3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;
import static pro.belbix.ethparser.TestAddresses.USDC;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.CONTROLLERS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.ETH_BLOCK_NUMBER_30_AUGUST_2020;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint112;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.StratInfo;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.harvest.decoder.HardWorkLogDecoder;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class Web3FunctionsTest {

  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private FunctionsUtils functionsUtils;
  @Autowired
  private AppProperties appProperties;
  @Autowired
  private ContractDbService contractDbService;

  private final HardWorkLogDecoder hardWorkLogDecoder = new HardWorkLogDecoder();


  @Test
  public void fetchDataForTxSwapWETHtoFARM() throws ClassNotFoundException {
    TransactionReceipt transactionReceipt = web3Functions
        .fetchTransactionReceipt(
            "0x266519b5e5756ea500d505afdfaa7d8cbb1fa0acc895fb9b9e6dbfefd3e7ce48", ETH_NETWORK);
    assertNotNull(transactionReceipt);
    List<Log> logs = transactionReceipt.getLogs();
    for (Log log : logs) {
      System.out.println(log.toString());
    }
    Log lastLog = logs.get(logs.size() - 1);
    assertEquals("0x56feaccb7f750b997b36a68625c7c596f0b41a58", lastLog.getAddress().toLowerCase());
    String data = lastLog.getData();

    List<Type> types = FunctionReturnDecoder.decode(data,
        Arrays.asList(
            TypeReference.makeTypeReference("uint"),
            TypeReference.makeTypeReference("uint"),
            TypeReference.makeTypeReference("uint"),
            TypeReference.makeTypeReference("uint")
        )
    );

    assertNotNull(types);
    assertEquals(new BigInteger("0"), types.get(0).getValue());
    assertEquals(new BigInteger("3369976790396557"), types.get(1).getValue());
    assertEquals(new BigInteger("11966348304870486"), types.get(2).getValue());
    assertEquals(new BigInteger("0"), types.get(3).getValue());
  }

  @Test
  public void testFetchBlock() {
    Block block = web3Functions.findBlockByHash(
        "0x185e7b9fa5700b045cb319472b2e7e73540aa56392389d7789d1d6b6e72dd832"
        , false, ETH_NETWORK)
        .getBlock();
    assertNotNull(block);
    Instant date = Instant.ofEpochSecond(block.getTimestamp().longValue());
    assertEquals(Instant.ofEpochSecond(1603810501L), date);
  }

  @Test
  void testFetchBlockByNumber() {
    Block block = web3Functions.findBlockByNumber(9000000, false, ETH_NETWORK)
        .getBlock();
    assertNotNull(block);
    assertEquals(BigInteger.valueOf(1574706444L), block.getTimestamp());
  }

  @Test
  public void ethCallGET_PRICE_PER_FULL_SHARE_WBTC() {
    List<Type> types = web3Functions.callFunction(new Function(
        "getPricePerFullShare",
        Collections.emptyList(),
        Collections.singletonList(new TypeReference<Uint256>() {
        })), "0x5d9d25c7C457dD82fc8668FFC6B9746b674d4EcB", LATEST, ETH_NETWORK);
    assertNotNull(types);
    assertFalse(types.isEmpty());
    assertTrue(functionsUtils.parseAmount((BigInteger) types.get(0).getValue(),
        "0x5d9d25c7C457dD82fc8668FFC6B9746b674d4EcB", ETH_NETWORK) > 0);
  }

  @Test
  public void ethCallGET_RESERVESTestUNI_LP_ETH_DAI() {
    List<Type> types = web3Functions.callFunction(new Function(
            "getReserves",
            Collections.emptyList(),
            Arrays.asList(new TypeReference<Uint112>() {
                          },
                new TypeReference<Uint112>() {
                },
                new TypeReference<Uint32>() {
                }
            )), "0xA478c2975Ab1Ea89e8196811F51A7B7Ade33eB11", ETH_BLOCK_NUMBER_30_AUGUST_2020,
        ETH_NETWORK);
    assertNotNull(types);
    assertEquals(3, types.size());
    assertTrue(((Uint112) types.get(0)).getValue()
        .divide(new BigInteger("1000000000000000000")).longValue() > 0);
    assertTrue(((Uint112) types.get(1)).getValue()
        .divide(new BigInteger("1000000000000000000")).longValue() > 0);
    assertTrue(((Uint32) types.get(2)).getValue().longValue() > 0);
  }

  @Test
  public void getReceiptShouldWork() {
    TransactionReceipt transactionReceipt = web3Functions.fetchTransactionReceipt(
        "0x18c4470ae45ac9183e4fd47335e7c4cbd97e76a631abec13334891818fe06101", ETH_NETWORK);
    assertNotNull(transactionReceipt);
  }

  @Test
  void testRequestWithTopics() {
    List<EthLog.LogResult> results = web3Functions.fetchContractLogs(
        List.of(USDC),
        12317814,
        12317814,
        ETH_NETWORK,
        "0xbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b"
    );
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertNotNull(results.get(0).get());
    EthLog.LogObject log = (EthLog.LogObject) results.get(0).get();
    System.out.println(log);
  }

  @Test
  void testTransactionBatchForOneBlock() {
    List<Block> blocks = web3Functions.findBlocksByBlockBatch(7574801, 7574801, BSC_NETWORK)
        .collect(Collectors.toList());
    assertNotNull(blocks, "block not null");
    Assertions.assertEquals(1, blocks.size(), "block size");
  }

  @Test
  void testTransactionBatchForTwoBlock() {
    List<Block> blocks = web3Functions.findBlocksByBlockBatch(7574801, 7574802, BSC_NETWORK)
        .collect(Collectors.toList());
    assertNotNull(blocks, "block not null");
    Assertions.assertEquals(2, blocks.size(), "block size");
  }

  @Test
  void ethLogBatchOneBlock() {
    int block = 12_000_000;
    List<LogResult> results =
        web3Functions.fetchContractLogsBatch(List.of(USDC), block, block, ETH_NETWORK, new String[0], new String[0]);
    Assertions.assertEquals(7, results.size());
    Set<Integer> blocks = new HashSet<>();
    results.forEach(l -> blocks.add(((Log) l.get()).getBlockNumber().intValue()));
    Assertions.assertTrue(blocks.contains(block), "result contains " + block);
    Assertions.assertEquals(1, blocks.size(), "blocks contains only our range");
  }

  @Test
  void ethLogBatchTwoBlock() {
    int block = 12_000_000;
    List<LogResult> results =
        web3Functions.fetchContractLogsBatch(List.of(USDC), block, block + 1, ETH_NETWORK, new String[0], new String[0]);
    Assertions.assertEquals(13, results.size());
    Set<Integer> blocks = new HashSet<>();
    results.forEach(l -> blocks.add(((Log) l.get()).getBlockNumber().intValue()));
    Assertions.assertTrue(blocks.contains(block), "result contains " + block);
    Assertions.assertEquals(2, blocks.size(), "blocks contains only our range");
  }

  @Test
  void ethLogBatchLimitX2() {
    int block = 12_000_000;
    int step = appProperties.getHandleLoopStep() * 2;
    List<LogResult> results =
        web3Functions.fetchContractLogsBatch(List.of(USDC), block,
            block + step, ETH_NETWORK, new String[0], new String[0]);
    Assertions.assertEquals(18649, results.size());
    Set<Integer> blocks = new HashSet<>();
    results.forEach(l -> blocks.add(((Log) l.get()).getBlockNumber().intValue()));
    Assertions.assertTrue(blocks.contains(block), "result contains first block" + block);
    Assertions.assertTrue(blocks.contains(block + step), "result contains last block" + block);
    Assertions.assertEquals(1967, blocks.size(), "blocks contains only our range");
  }

  @Test
  void ethLogBatchMandatoryTopics() {
    String eventSignatureHash =
        "0x43ffccb0abea5304f42c5d67d56e479f21f76ecc142c39a770725e99125243bc";
    String vault =
        "0x000000000000000000000000145f39b3c6e6a885aa6a8fade4ca69d64bab69c8";

    List<LogResult> results =
        web3Functions.fetchContractLogsBatch(
            List.of(CONTROLLERS.get(ETH_NETWORK)),
            12589294, 12589335, ETH_NETWORK,
            new String[0],
            new String[]{ eventSignatureHash, vault});
    Assertions.assertEquals(1, results.size());

    List<String> topics = ((LogObject) results.get(0)).getTopics();
    Assertions.assertEquals(eventSignatureHash, topics.get(0));
    Assertions.assertEquals(vault, topics.get(1));
  }

  @Test
  void ethLogBatchOptionalTopics() {
    String eventSignatureHash =
        "0x43ffccb0abea5304f42c5d67d56e479f21f76ecc142c39a770725e99125243bc";
    String vault =
        "0x000000000000000000000000145f39b3c6e6a885aa6a8fade4ca69d64bab69c8";

    List<LogResult> results =
        web3Functions.fetchContractLogsBatch(
            List.of(CONTROLLERS.get(ETH_NETWORK)),
            12589294,12589335, ETH_NETWORK,
            new String[]{ eventSignatureHash, vault},
            new String[0]);
    Assertions.assertEquals(2, results.size());
    List<String> topics0 = ((LogObject) results.get(0)).getTopics();

    Assertions.assertEquals(eventSignatureHash, topics0.get(0));
    Assertions.assertNotEquals(vault, topics0.get(1));

    List<String> topics1 = ((LogObject) results.get(1)).getTopics();
    Assertions.assertEquals(eventSignatureHash, topics1.get(0));
    Assertions.assertEquals(vault, topics1.get(1));
  }

}
