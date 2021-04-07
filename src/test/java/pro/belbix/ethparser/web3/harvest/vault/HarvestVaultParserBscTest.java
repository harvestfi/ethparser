package pro.belbix.ethparser.web3.harvest.vault;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestVaultParserBscTest {

  private static final int LOG_ID = 0;

  @Autowired
  private HarvestVaultParserV2 harvestVaultParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  @Autowired
  private HarvestDBService harvestDBService;
  @Autowired
  private ContractLoader contractLoader;

  @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load(BSC_NETWORK);
  }

  @Test
  void test_PC_BUSD_BNB() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34", 6101208);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0x2beda2c174968f2f7170d1a1722d29f0a15d29104cb1ff3ae8a63247a9656af2")
        .block(6101208L)
        .blockDate(1617016126L)
        .confirmed(1)
        .methodName("Deposit")
        .owner("0x7f4ac7a8b18d7dc76c5962aa1aacf968eac3ac67")
        .amount(0.2662041892498321)
        .vault("PC_BUSD_BNB")
        .lastGas(10.)
        .lastUsdTvl(10.)
        .sharePrice(1.)
        .usdAmount(10L)
        .lpStat(
            "{\"coin1\":\"WBNB\",\"coin2\":\"BUSD\",\"amount1\":0.01830295070448038,\"amount2\":4.999430824411079,\"price1\":273.1488985099692,\"price2\":1.0}")
        .ownerBalance(0.2662041892498321)
        .ownerBalanceUsd(9.99886164882216)
        .allOwnersCount(6)
        .migrated(false)
        .underlyingPrice(null)
        .allPoolsOwnersCount(0)
        .ownerCount(0)
        .lastTvl(0.2662041892498321)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
  }

  private HarvestDTO loadHarvestDto(String vaultAddress, int block) {
    @SuppressWarnings("rawtypes")
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(vaultAddress), block, block, BSC_NETWORK);
    assertTrue(LOG_ID < logResults.size(),
        "Log smaller then necessary");
    HarvestDTO harvestDTO = harvestVaultParser.parseVaultLog(
        (Log) logResults.get(LOG_ID).get(), BSC_NETWORK);
    harvestVaultParser.enrichDto(harvestDTO, BSC_NETWORK);
    harvestOwnerBalanceCalculator.fillBalance(harvestDTO, BSC_NETWORK);
    harvestDBService.saveHarvestDTO(harvestDTO);
    return harvestDTO;
  }

}
