package pro.belbix.ethparser.web3.harvest.vault;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;

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
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class VaulActionstParserBscTest {

  private static final int LOG_ID = 0;

  @Autowired
  private VaultActionsParser harvestVaultParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
  @Autowired
  private VaultActionsDBService vaultActionsDBService;

  @Test
  void test_VENUS_ETH() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0x2ce34b1bb247f242f1d2a33811e01138968efbff",
        7064815);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0x48368aa00419e27c7ef5b8a566f2278a2e9058edd0309430e650992c936310bc")
        .block(7064815L)
        .blockDate(1619935480L)
        .confirmed(1)
        .methodName("Withdraw")
        .owner("0x3115ba406ad02d875ca84c0dc861b05e06ca684d")
        .vault("VENUS_ETH")
        .amount(2.9939447597876865)
        .usdAmount(8768L)
        .lastTvl(321.1753820777222)
        .lastUsdTvl(938252.0)
        .ownerBalance(0.0)
        .ownerBalanceUsd(0.0)
        .sharePrice(1.002514753768422)
        .lpStat(null)
        .migrated(false)
        .underlyingPrice(null)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
    vaultActionsDBService.saveHarvestDTO(harvestDTO);
  }

  @Test
  void test_EPS_BNB() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0x0A7d74604b39229D444855eF294F287099774aC8",
        6617283);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0xae43076f19628b1c671aba6bb6044ca23a09741ebc6e162e7a2dd5a2aa26e026")
        .block(6617283L)
        .blockDate(1618580189L)
        .confirmed(1)
        .methodName("Deposit")
        .owner("0x72c2c890625f7e2ec82a49a5b0d6438c36fc1bb5")
        .vault("PC_EPS_BNB")
        .amount(28.97068833608871)
        .usdAmount(2421L)
        .lastTvl(644.3396792810088)
        .lastUsdTvl(52790.0)
        .ownerBalance(29.553600928619893)
        .ownerBalanceUsd(2421.3216560351125)
        .sharePrice(1.020120771235009)
        .lpStat(null)
        .migrated(false)
        .underlyingPrice(null)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
  }

  @Test
  void test_EPS_3POOL() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0x63671425ef4D25Ec2b12C7d05DE855C143f16e3B", 6612952);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0x6bcae70f8628ef5711500d155bc974a67853e9b8444ad8405be35d4f724c6efb")
        .block(6612952L)
        .blockDate(1618567196L)
        .confirmed(1)
        .methodName("Withdraw")
        .owner("0xedd828ed8bf8205cdbf4439349c539215d3fad1d")
        .vault("EPS_3POOL")
        .amount(40654.48461038426)
        .usdAmount(25441L)
        .lastTvl(501441.97904271755)
        .lastUsdTvl(313473.0)
        .ownerBalance(0.0)
        .ownerBalanceUsd(0.)
        .sharePrice(1.001042916331453)
        .lpStat(null)
        .migrated(false)
        .underlyingPrice(null)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
  }

  @Test
  void test_EGG_BNB() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0xe3f309F151746b3C0953e4C0E455bFf3dc2176AA", 6231367);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0xa37fb128db5433af348b7798468e885e9471c23ed7ed117684602a998b98cbac")
        .block(6231367L)
        .blockDate(1617409428L)
        .confirmed(1)
        .methodName("Deposit")
        .owner("0x7bcab1384ab339d5b0e92e93709c2a869785b4a3")
        .vault("EGG_BNB")
        .amount(2.1605310506235966)
        .usdAmount(345L)
        .lastTvl(22.42643322642496)
        .lastUsdTvl(3579.0)
        .ownerBalance(2.1605310506235966)
        .ownerBalanceUsd(344.7824953160484)
        .sharePrice(1.)
        .lpStat(null)
        .ownerCount(1)
        .migrated(false)
        .underlyingPrice(null)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
  }

  @Test
  void test_ONEINCH_RENBTC() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0xbF2989575dE9850F0A4b534740A88F5D2b460A4f", 6231363);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0xec16afa25cbbfa92ae54373a53c3d1513fc15c88fd417bb1a711150114d7389c")
        .block(6231363L)
        .blockDate(1617409416L)
        .confirmed(1)
        .methodName("Deposit")
        .owner("0x1eb1b1bea69839835bd71e428c3cae0116c0d8bf")
        .vault("ONEINCH_RENBTC")
        .amount(1535.5432734372898)
        .usdAmount(7240L)
        .lastTvl(1535.5432734372898)
        .lastUsdTvl(7240.0)
        .ownerBalance(1535.5432734372898)
        .ownerBalanceUsd(7239.288065221555)
        .sharePrice(1.)
        .lpStat(null)
        .ownerCount(1)
        .migrated(false)
        .underlyingPrice(null)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
  }

  @Test
  void test_ONEINCH_BNB() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0x9090BCcD472b9D11DE302572167DED6632e185AB", 6400546);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0x7f32e4faab8187908eebbae504c44374854a9d9f6f8303180f6fcab977bcc888")
        .block(6400546L)
        .blockDate(1617928698L)
        .confirmed(1)
        .methodName("Deposit")
        .owner("0xa12f7352ee48481f5c5896d47d74eff6a7a267e7")
        .vault("ONEINCH_BNB")
        .amount(142.55437636286547)
        .usdAmount(1990L)
        .lastTvl(76006.17496369126)
        .lastUsdTvl(1056488.0)
        .ownerBalance(143.18501961766782)
        .ownerBalanceUsd(1990.275158037569)
        .sharePrice(1.0044238785991184)
        .lpStat(null)
        .ownerCount(1)
        .migrated(false)
        .underlyingPrice(null)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
  }

  @Test
  void test_VENUS_XVS() throws Exception {
    HarvestDTO harvestDTO = loadHarvestDto(
        "0xCf5F83F8FE0AB0f9E9C1db07E6606dD598b2bbf5", 6343242);
    assertNotNull(harvestDTO);
    assertModel(HarvestDTO.builder()
        .hash("0x8a3ed912f608f2d8dfafc0406082d52df2b79fc3ae57febc0bdfc98b75d833c8")
        .block(6343242L)
        .blockDate(1617750554L)
        .confirmed(1)
        .methodName("Deposit")
        .owner("0x67e82669107250b8562e5bc7df4e347abf1fb66f")
        .amount(389.8465842113253)
        .vault("VENUS_XVS")
        .lastUsdTvl(90119.0)
        .sharePrice(1.0014240802660224)
        .usdAmount(22531L)
        .lpStat(null)
        .ownerBalance(390.4017570386769)
        .ownerBalanceUsd(22531.514889208454)
        .migrated(false)
        .underlyingPrice(null)
        .ownerCount(1)
        .lastTvl(1561.4865941715939)
        .profit(null)
        .profitUsd(null)
        .totalAmount(null)
        .build(), harvestDTO);
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
        .lastUsdTvl(10.)
        .sharePrice(1.)
        .usdAmount(10L)
        .lpStat(
            "{\"coin1\":\"WBNB\",\"coin2\":\"BUSD\",\"amount1\":0.01830295070448038,\"amount2\":4.999430824411079,\"price1\":273.1488985099692,\"price2\":1.0}")
        .ownerBalance(0.2662041892498321)
        .ownerBalanceUsd(9.99886164882216)
        .migrated(false)
        .underlyingPrice(null)
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
    HarvestDTO harvestDTO = harvestVaultParser.parse(
        (Log) logResults.get(LOG_ID).get(), BSC_NETWORK);
    harvestVaultParser.enrichDto(harvestDTO, BSC_NETWORK);
    harvestOwnerBalanceCalculator.fillBalance(harvestDTO, BSC_NETWORK);
    vaultActionsDBService.saveHarvestDTO(harvestDTO);
    return harvestDTO;
  }

}
