package pro.belbix.ethparser.repositories.c_layer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.b_layer.ContractEventEntity;
import pro.belbix.ethparser.entity.c_layer.VaultActionsViewEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;
import pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockParser;
import pro.belbix.ethparser.web3.layers.detector.ContractDetector;
import pro.belbix.ethparser.web3.layers.detector.db.ContractEventsDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class VaultActionsViewRepositoryBscTest {

  @Autowired
  private VaultActionsViewRepository vaultActionsViewRepository;
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
    contractLoader.load(BSC_NETWORK);
  }

  @Test
  void shouldLoadVault_PC_BUSD_BNB() {
    loadBlock(6101208);
    List<VaultActionsViewEntity> results =
        vaultActionsViewRepository.findByAddresses(
            List.of("0xF7A3a95d0f7E8A5EEaE483Cdd7b76aF287283D34".toLowerCase()),
            0,
            999999999,
            PageRequest.of(0, 1, Sort.by("blockNumber"))
        );
    assertNotNull(results);
    assertEquals(results.size(), 1);
    VaultActionsViewEntity vaultActionsViewEntity = results.get(0);
    assertVaultActions(
        VaultActionsViewEntity.builder()
            .address("0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34")
            .blockNumber(6101208L)
            .blockHash("0xd29618f07ac3e0ae8df9af7a1ab49d9225ddd3589975eb08a6779fdd220fb031")
            .sourceName("PC_BUSD_BNB")
            .network("bsc")
            .txHash("0x2beda2c174968f2f7170d1a1722d29f0a15d29104cb1ff3ae8a63247a9656af2")
            .funcName("deposit")
            .logId(137)
            .fromAdr("0x0000000000000000000000000000000000000000")
            .toAdr("0x7f4ac7a8b18d7dc76c5962aa1aacf968eac3ac67")
            .ftokenAmount(0.2662041892498321)
            .opType("Deposit")
            .sharePrice(1.0)
            .ftokenTotalSupply(0.2662041892498321)
            .tvl(0.2662041892498321)
            .underlying("0x1b96b92314c44b159149f7e0303511fb2fc4774f")
            .underlyingName("PC_WBNB_BUSD")
            .underlyingType(2)
            .build(),
        vaultActionsViewEntity
    );
  }

  void assertVaultActions(VaultActionsViewEntity expected, VaultActionsViewEntity actual) {
    assertAll(
        () -> assertEquals(expected.getAddress(), actual.getAddress(), "getAddress"),
        () -> assertEquals(expected.getBlockNumber(), actual.getBlockNumber(), "getBlockNumber"),
        () -> assertEquals(expected.getBlockHash(), actual.getBlockHash(), "getBlockHash"),
        () -> assertEquals(expected.getSourceName(), actual.getSourceName(), "getSourceName"),
        () -> assertEquals(expected.getNetwork(), actual.getNetwork(), "getNetwork"),
        () -> assertEquals(expected.getTxHash(), actual.getTxHash(), "getTxHash"),
        () -> assertEquals(expected.getFuncName(), actual.getFuncName(), "getFuncName"),
        () -> assertEquals(expected.getLogId(), actual.getLogId(), "getLogId"),
        () -> assertEquals(expected.getFromAdr(), actual.getFromAdr(), "getFromAdr"),
        () -> assertEquals(expected.getToAdr(), actual.getToAdr(), "getToAdr"),
        () -> assertEquals(expected.getFtokenAmount(), actual.getFtokenAmount(), "getFtokenAmount"),
        () -> assertEquals(expected.getOpType(), actual.getOpType(), "getOpType"),
        () -> assertEquals(expected.getSharePrice(), actual.getSharePrice(), "getSharePrice"),
        () -> assertEquals(expected.getFtokenTotalSupply(), actual.getFtokenTotalSupply(),
            "getFtokenTotalSupply"),
        () -> assertEquals(expected.getTvl(), actual.getTvl(), "getTvl"),
        () -> assertEquals(expected.getUnderlying(), actual.getUnderlying(), "getUnderlying"),
        () -> assertEquals(expected.getUnderlyingName(), actual.getUnderlyingName(),
            "getUnderlyingName"),
        () -> assertEquals(expected.getUnderlyingType(), actual.getUnderlyingType(),
            "getUnderlyingType")
    );
  }

  private void loadBlock(long blockNumber) {
    EthBlockEntity ethBlockEntity =
        ethBlockParser.parse(web3Functions.findBlockByNumber(
            blockNumber, true, BSC_NETWORK), BSC_NETWORK);
    ethBlockEntity = ethBlockDbService.save(ethBlockEntity);
    if (ethBlockEntity == null) {
      ethBlockEntity = ethBlockRepository.findById(blockNumber).orElseThrow();
    }
    List<ContractEventEntity> events = contractDetector.handleBlock(ethBlockEntity, BSC_NETWORK);
    assertFalse(events.isEmpty(), "Not found eligible blocks");
    events.forEach(e -> contractEventsDbService.save(e));
  }
}
