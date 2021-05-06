package pro.belbix.ethparser.web3.harvest.vault;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.numberFormat;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

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
import pro.belbix.ethparser.web3.harvest.parser.VaultActionsParser;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestOwnerBalanceCalculatorTest {

  @Autowired
  private VaultActionsParser harvestVaultParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;

  @Test
  public void shouldCalculateForUSDT_V0() {
    assertHarvest(
        "0xc7EE21406BB581e741FBb8B21f213188433D9f2F",
        10780101,
        1,
        "4990,85837900",
        "4943.34928575"
    );
    }

    @Test
    public void shouldCalculateForDAI_V0() {
        assertHarvest(
            "0xe85C8581e60D7Cd32Bbfd86303d2A4FA6a951Dac",
            10780382,
            0,
            "0,00000000",
            "0,00000000"
        );
    }

    private HarvestDTO assertHarvest(String fromVault,
        int onBlock,
        int logId,
        String _ownerBalance,
        String _ownerBalanceUsd
    ) {
      String ownerBalance = numberFormat(_ownerBalance);
      String ownerBalanceUsd = numberFormat(_ownerBalanceUsd);
      List<LogResult> logResults = web3Functions
          .fetchContractLogs(singletonList(fromVault), onBlock, onBlock, ETH_NETWORK);
      assertTrue("Log smaller then necessary", logId < logResults.size());
      HarvestDTO dto = harvestVaultParser.parse((Log) logResults.get(logId).get(), ETH_NETWORK);
      assertNotNull(dto, "Dto is null");
      boolean result = harvestOwnerBalanceCalculator.fillBalance(dto, ETH_NETWORK);
      assertTrue(result);
      assertAll(
          () -> assertEquals("owner balance", ownerBalance,
              String.format("%.8f", dto.getOwnerBalance())),
          () -> assertEquals("owner balance usd", ownerBalanceUsd,
              String.format("%.8f", dto.getOwnerBalanceUsd()))
      );
        return dto;
    }
}
