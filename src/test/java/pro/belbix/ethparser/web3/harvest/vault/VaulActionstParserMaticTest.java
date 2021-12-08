package pro.belbix.ethparser.web3.harvest.vault;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.MATIC_NETWORK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.harvest.HarvestOwnerBalanceCalculator;
import pro.belbix.ethparser.web3.harvest.db.VaultActionsDBService;
import pro.belbix.ethparser.web3.harvest.parser.VaultActionsParser;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class VaulActionstParserMaticTest {

  private static final int LOG_ID = 0;

  @Autowired
  private VaultActionsParser harvestVaultParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  @Autowired
  private VaultActionsDBService vaultActionsDBService;


  @Test
  void test_SUSHI_USDC_WETH() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0xf76a0c5083b895c76ecbf30121f036849137d545", 18540186);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0x02f122326491e95ff304e463ddbea1b4e967c636becd518869a0ce306f7d8c0d")
        .block(18540186L)
        .blockDate(1630294216L)
        .confirmed(1)
        .methodName("Deposit")
        .owner("0x5c70052919c4cb622e0dffd774ed6a31ad43a22b")
        .amount(0.000000149036230053)
        .vault("V_SUSHI_USDC_WETH_#V1")
        .lastUsdTvl(573391.)
        .sharePrice(1.0112412359971954)
        .usdAmount(19L)
        .lpStat(
            "{\"coin1\":\"USDC\",\"coin1Address\":\"0x2791bca1f2de4661ed88a30c99a7a9449aa84174\",\"coin2\":\"WETH\",\"coin2Address\":\"0x7ceb23fd6bc0add59e62ac25578270cff1b9f619\",\"amount1\":286679.05916009314,\"amount2\":90.1230610362711,\"price1\":1.0,\"price2\":3181.341124459234}")
        .ownerBalance(0.000000150711581487)
        .ownerBalanceUsd(18.875915326777225)
        .migrated(false)
        .underlyingPrice(null)
        .ownerCount(1)
        .lastTvl(0.004578146385675686)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
  }

  private HarvestDTO loadHarvestDto(String vaultAddress, int block) {
    @SuppressWarnings("rawtypes")
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(vaultAddress), block, block, MATIC_NETWORK);
    assertTrue(LOG_ID < logResults.size(),
        "Log smaller then necessary");
    HarvestDTO harvestDTO = harvestVaultParser.parse((Log) logResults.get(LOG_ID).get(), MATIC_NETWORK);
    harvestVaultParser.enrichDto(harvestDTO, MATIC_NETWORK);
    harvestOwnerBalanceCalculator.fillBalance(harvestDTO, MATIC_NETWORK);
    vaultActionsDBService.saveHarvestDTO(harvestDTO);
    return harvestDTO;
  }

}
