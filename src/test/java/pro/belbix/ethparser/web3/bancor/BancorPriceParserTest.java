package pro.belbix.ethparser.web3.bancor;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.abi.FunctionsNames.RATE_BY_PATH;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BANCOR_CONVERSION_ADDRESS;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.BANCOR_USDC_CONVERT_PATH;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.D6;
import static pro.belbix.ethparser.web3.contracts.ContractConstants.L18;
import static pro.belbix.ethparser.web3.contracts.ContractUtils.isParsableBancorTransaction;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.BancorDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.abi.FunctionsUtils;


@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class BancorPriceParserTest {

  @Autowired
  private Web3Functions web3Functions;

  @Autowired
  private FunctionsUtils functionsUtils;

  @Autowired
  private BancorPriceParser bancorPriceParser;

  @Test
  public void isParsableBancorAddressAndDate_right_address_same_date() {
    boolean sameDate = isParsableBancorTransaction(
        BANCOR_CONVERSION_ADDRESS,
        10285676,
        ETH_NETWORK);

    assertFalse(sameDate);
  }

  @Test
  public void isParsableBancorAddressAndDate_right_address_less_date() {
    boolean lessDate = isParsableBancorTransaction(
        BANCOR_CONVERSION_ADDRESS,
        10285675,
        ETH_NETWORK);

    assertFalse(lessDate);
  }

  @Test
  public void isParsableBancorAddressAndDate_wrong_address() {
    boolean wrongAddress = isParsableBancorTransaction(
        "0x0",
        10285677,
        ETH_NETWORK);
    assertFalse(wrongAddress);
  }

  @Test
  public void isFullParsableLpAddressAndDate_right_address_right_date() {
    boolean allFine = isParsableBancorTransaction(
        BANCOR_CONVERSION_ADDRESS,
        10285677, ETH_NETWORK);
    assertTrue(allFine);
  }

  @Test
  public void checkBNTPriceAtBlockNumber() {

    BigInteger amountIn = BigInteger.valueOf(L18);;
    long price1 = functionsUtils.callIntByNameWithAddressesArrayAndBigIntegerArg(
        RATE_BY_PATH, BANCOR_USDC_CONVERT_PATH, amountIn,
        BANCOR_CONVERSION_ADDRESS, 13580560L,
        ETH_NETWORK).orElse(BigInteger.ZERO).longValue();
    Assertions.assertNotEquals(5.777, price1/D6);
    Assertions.assertEquals(4.688082, price1/D6, 0.000001);


    long price2 = functionsUtils.callIntByNameWithAddressesArrayAndBigIntegerArg(
        RATE_BY_PATH, BANCOR_USDC_CONVERT_PATH, amountIn,
        BANCOR_CONVERSION_ADDRESS, 13442586L,
        ETH_NETWORK).orElse(BigInteger.ZERO).longValue();
    Assertions.assertEquals(4.000914, price2/D6, 0.000001);
  }

  @Test
  public void parseFarmPrice() {

    List<LogResult> logResults = web3Functions
        .fetchContractLogs(singletonList(BANCOR_CONVERSION_ADDRESS),
            13586929,
            13586929, ETH_NETWORK);

    BancorDTO dto = bancorPriceParser.parse((Log) logResults.get(1).get(), ETH_NETWORK);
    assertNotNull(dto);
    Assertions.assertEquals(167.5412415, dto.getPriceFarm(), 0.000001);
  }

}
