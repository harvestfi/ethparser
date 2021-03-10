package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.numberFormat;

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
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestOwnerBalanceCalculatorTest {

  @Autowired
  private HarvestVaultParserV2 harvestVaultParser;
  @Autowired
  private Web3Service web3Service;
  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  @Autowired
  private ContractLoader contractLoader;

   @BeforeEach
  public void setUp() throws Exception {
    contractLoader.load();
  }

  @Test
  public void shouldCalculateForUSDT_V0() {
    assertHarvest(
        "0xc7EE21406BB581e741FBb8B21f213188433D9f2F",
        10780101,
        1,
        "4990,85837900",
        "4990,85837900"
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
      List<LogResult> logResults = web3Service
          .fetchContractLogs(singletonList(fromVault), onBlock, onBlock);
      assertTrue("Log smaller then necessary", logId < logResults.size());
      HarvestDTO dto = harvestVaultParser.parseVaultLog((Log) logResults.get(logId).get());
      assertNotNull(dto, "Dto is null");
      boolean result = harvestOwnerBalanceCalculator.fillBalance(dto);
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
