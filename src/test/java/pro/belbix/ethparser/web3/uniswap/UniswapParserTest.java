package pro.belbix.ethparser.web3.uniswap;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.numberFormat;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractUtils.isFullParsableLpAddressAndDate;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.dto.v0.UniswapDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.harvest.parser.UniToHarvestConverter;
import pro.belbix.ethparser.web3.prices.PriceProvider;
import pro.belbix.ethparser.web3.uniswap.parser.UniswapLpLogParser;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class UniswapParserTest {

  private static final int LOG_ID = 0;

  @Autowired
  private UniswapLpLogParser uniswapLpLogParser;
  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private PriceProvider priceProvider;
  @Autowired
  private UniToHarvestConverter uniToHarvestConverter;

  @Test
  public void test_isFullParsableLpAddressAndDate_right_address_same_date() {
    boolean sameDate = isFullParsableLpAddressAndDate(
        "0x56feAccb7f750B997B36A68625C7C596F0B41A58",
        11407437,
        ETH_NETWORK);

    assertFalse(sameDate);
  }

  @Test
  public void test_isFullParsableLpAddressAndDate_right_address_less_date() {
    boolean lessDate =
        isFullParsableLpAddressAndDate(
            "0x56feAccb7f750B997B36A68625C7C596F0B41A58",
            11407436,
            ETH_NETWORK);

    assertFalse(lessDate);
  }

  @Test
  public void test_isFullParsableLpAddressAndDate_wrong_address() {
    boolean wrongAddress = isFullParsableLpAddressAndDate(
        "0x0",
        10777067,
        ETH_NETWORK);
    assertFalse(wrongAddress);
  }

  @Test
  public void test_isFullParsableLpAddressAndDate_right_address_right_date() {
    boolean allFine =
        isFullParsableLpAddressAndDate(
            "0x56feAccb7f750B997B36A68625C7C596F0B41A58",
            11407438, ETH_NETWORK);
    assertTrue(allFine);
  }

  @Test
  public void test_fetchContractLogs_wrong_contract() {
    List<LogResult> logResult = web3Functions
        .fetchContractLogs(singletonList("0x56feAccb7f750B997B36A68625C7C596F0B41A58"),
            10777652,
            10777652, ETH_NETWORK);

    UniswapDTO dto = uniswapLpLogParser.parse((Log) logResult.get(0).get(), ETH_NETWORK);

    assertNull(dto);
  }

  @Test
  public void parseUNI_LP_USDC_FARM_ADD2() {
    UniswapDTO dto = uniswapParseTest("0x514906fc121c7878424a5c928cad1852cc545892",
        10777202,
        2,
        "0x2b9e215b27dea420509c81a218119a77bc91931d017cc6eb9f00a090d5ba1eb6_27",
        "0xe2a6b5d2c244b0f9ba4c4abe8ed3d3ef0a89bfeb",
        "4,08147853",
        "ADD",
        "USDC",
        "945,85450400",
        "231,74310394"
    );
    HarvestDTO harvestDTO = uniToHarvestConverter.parse(dto, ETH_NETWORK);
    assertNotNull(harvestDTO);
    assertAll(
        () -> assertEquals("Amount", numberFormat("0,00000000"),
            String.format("%.8f", harvestDTO.getAmount())),
        () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
        () -> assertEquals("Vault", "UNI_LP_FARM_USDC", harvestDTO.getVault()),
        () -> assertEquals("UsdAmount", "1892",
            String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
        () -> assertEquals("LastUsdTvl", "0",
            String.format("%.0f", harvestDTO.getLastUsdTvl())),
        () -> assertEquals("LpStat",
            "{\"coin1\":\"FARM\",\"coin1Address\":\"0xa0246c9032bc3a600820415ae600c6388619a14d\",\"coin2\":\"USDC\",\"coin2Address\":\"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48\",\"amount1\":0.0,\"amount2\":0.0,\"price1\":231.7431039407448,\"price2\":1.0}",
            harvestDTO.getLpStat()),
        () -> assertEquals("LastTvl", numberFormat("0,00000000"),
            String.format("%.8f", harvestDTO.getLastTvl()))
    );
  }

  @Test
  public void parseUNI_LP_GRAIN_FARM_BUY() {
    uniswapParseTest("0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49",
        11420092,
        1,
        "0x1248c50025f40dcb1df7af6d1d96cbf744a59ef56f210f2415498fa04854458d_79",
        "0x2b01755949aec1c8479b4d6dd1ea1e725091b21e",
        "1784,61622059",
        "BUY",
        "FARM",
        "2,02599871",
        "0.11389017"
    );
  }

  @Test
  public void parseUNI_LP_USDC_FARM_BUY() {
    uniswapParseTest("0x514906fc121c7878424a5c928cad1852cc545892",
        11379014,
        1,
        "0xd57e7afaf8da1be10aebecc7f509990e180234b42e39cc56422b40bb913ab48c_126",
        "0xbd9fe62f10df42f6d58d88a280c798de865fac86",
        "10,00000000",
        "BUY",
        "USDC",
        "891,77695300",
        "89.17769530"
    );
  }

  @Test
  public void parseUNI_LP_USDC_FARM_ADD() {
    UniswapDTO dto = uniswapParseTest("0x514906fc121c7878424a5c928cad1852cc545892",
        10826472,
        3,
        "0x00088d2d18d024d462e3fc9e513063eff1545c10b43678e6bf605d08bddce0d2_243",
        "0x0d089508d5fcdc92363fe84c84a44738863d9201",
        "4,02461788",
        "ADD",
        "USDC",
        "941,51653400",
        "233,93936051"
    );

    HarvestDTO harvestDTO = uniToHarvestConverter.parse(dto, ETH_NETWORK);
    assertNotNull(harvestDTO);
    assertAll(
        () -> assertEquals("Amount", numberFormat("0,00005882"),
            String.format("%.8f", harvestDTO.getAmount())),
        () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
        () -> assertEquals("Vault", "UNI_LP_FARM_USDC", harvestDTO.getVault()),
        () -> assertEquals("UsdAmount", "1883",
            String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
        () -> assertEquals("LastUsdTvl", "9657721",
            String.format("%.0f", harvestDTO.getLastUsdTvl())),
        () -> assertEquals("LpStat",
            "{\"coin1\":\"FARM\",\"coin1Address\":\"0xa0246c9032bc3a600820415ae600c6388619a14d\",\"coin2\":\"USDC\",\"coin2Address\":\"0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48\",\"amount1\":20641.50424133,\"amount2\":4828860.303375671,\"price1\":233.93936056786777,\"price2\":1.0}",
            harvestDTO.getLpStat()),
        () -> assertEquals("LastTvl", numberFormat("0,30169333"),
            String.format("%.8f", harvestDTO.getLastTvl()))
    );
  }

  @Test
  public void parseUNI_LP_WETH_FARM_ADD() {
    UniswapDTO dto = uniswapParseTest("0x56feaccb7f750b997b36a68625c7c596f0b41a58",
        12439260,
        4,
        "0xb20ff0e35a14ea2d1ff0e420a3a01bc6a74c7bb538a180bb5a28b53af217e7e0_115",
        "0x92d044230b5d92222fb8ac72913ac3bafee484e5",
        "28.74148437",
        "REM",
        "WETH",
        "1.21872105",
        "165.97611085"
    );
    HarvestDTO harvestDTO = uniToHarvestConverter.parse(dto, ETH_NETWORK);
    assertNotNull(harvestDTO);
    assertAll(
        () -> assertEquals("Amount", numberFormat("4.68063834"),
            String.format("%.8f", harvestDTO.getAmount())),
        () -> assertEquals("Method", "Withdraw", harvestDTO.getMethodName()),
        () -> assertEquals("Vault", "UNI_LP_FARM_WETH", harvestDTO.getVault()),
        () -> assertEquals("UsdAmount", "9479",
            String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
        () -> assertEquals("LastUsdTvl", "2588176",
            String.format("%.0f", harvestDTO.getLastUsdTvl())),
        () -> assertEquals("LpStat",
            "{\"coin1\":\"FARM\",\"coin1Address\":\"0xa0246c9032bc3a600820415ae600c6388619a14d\",\"coin2\":\"WETH\",\"coin2Address\":\"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2\",\"amount1\":7822.103286930344,\"amount2\":331.6795263465357,\"price1\":164.90370984764417,\"price2\":3914.267173818034}",
            harvestDTO.getLpStat()),
        () -> assertEquals("LastTvl", numberFormat("1277.99542165"),
            String.format("%.8f", harvestDTO.getLastTvl()))
    );
  }

  @Test
  public void parseUNI_LP_GRAIN_FARM_ADD() {
    UniswapDTO dto = uniswapParseTest("0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49",
        11417637,
        2,
        "0x072a25cbdc61c4302483593044c536055f7ddd9dcca48fc25c66af1d4f56edfb_104",
        "0xbb830af11a5bf085e63ee04bf4f3da50955e8eb5",
        "127744,24825310",
        "ADD",
        "FARM",
        "140,02624240",
        "0,10707515"
    );
    HarvestDTO harvestDTO = uniToHarvestConverter.parse(dto, ETH_NETWORK);
    assertNotNull(harvestDTO);
    assertAll(
        () -> assertEquals("Amount", numberFormat("4170,06518775"),
            String.format("%.8f", harvestDTO.getAmount())),
        () -> assertEquals("Method", "Deposit", harvestDTO.getMethodName()),
        () -> assertEquals("Vault", "UNI_LP_GRAIN_FARM", harvestDTO.getVault()),
        () -> assertEquals("UsdAmount", "27356",
            String.format("%.0f", harvestDTO.getUsdAmount().doubleValue())),
        () -> assertEquals("LastUsdTvl", "219248",
            String.format("%.0f", harvestDTO.getLastUsdTvl())),
        () -> assertEquals("LpStat",
            "{\"coin1\":\"GRAIN\",\"coin1Address\":\"0x6589fe1271a0f29346796c6baf0cdf619e25e58e\",\"coin2\":\"FARM\",\"coin2Address\":\"0xa0246c9032bc3a600820415ae600c6388619a14d\",\"amount1\":1023805.547072685,\"amount2\":1122.2395189074418,\"price1\":0.10707514707395074,\"price2\":97.68336231347396}",
            harvestDTO.getLpStat()),
        () -> assertEquals("LastTvl", numberFormat("33420,96359919"),
            String.format("%.8f", harvestDTO.getLastTvl()))
    );
  }

  private void shouldNotParse(String contract, int onBlock, int logId) {
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(contract), onBlock, onBlock, ETH_NETWORK);
    assertTrue("Log smaller then necessary " + logResults.size(), logId < logResults.size());
    UniswapDTO dto = uniswapLpLogParser.parse((Log) logResults.get(logId).get(), ETH_NETWORK);
    assertNull(dto);
  }

  private UniswapDTO uniswapParseTest(
      String contract,
      int onBlock,
      int logId,
      String id,
      String owner,
      String amount,
      String type,
      String otherCoin,
      String otherAmount,
      String lastPrice
  ) {
    amount = numberFormat(amount);
    otherAmount = numberFormat(otherAmount);
    lastPrice = numberFormat(lastPrice);
    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(contract), onBlock, onBlock, ETH_NETWORK);
    assertTrue("Log smaller then necessary " + logResults.size(), logId < logResults.size());
    UniswapDTO dto = uniswapLpLogParser.parse((Log) logResults.get(logId).get(), ETH_NETWORK);
    assertDto(dto,
        id,
        owner,
        amount,
        type,
        otherCoin,
        otherAmount,
        lastPrice
    );
    return dto;
  }

  private void assertDto(UniswapDTO dto,
      String id,
      String owner,
      String amount,
      String type,
      String otherCoin,
      String otherAmount,
      String lastPrice) {
    assertNotNull(dto, "Dto is null");
    assertAll(() -> assertEquals("owner", owner, dto.getOwner()),
        () -> assertEquals("Id", id, dto.getId()),
        () -> assertEquals("Amount", amount, String.format("%.8f", dto.getAmount())),
        () -> assertEquals("Type", type, dto.getType()),
        () -> assertEquals("OtherCoin", otherCoin, dto.getOtherCoin()),
        () -> assertEquals("otherAmount", otherAmount, String.format("%.8f", dto.getOtherAmount())),
        () -> assertEquals("lastPrice", lastPrice, String.format("%.8f", dto.getLastPrice()))
    );
  }
}
