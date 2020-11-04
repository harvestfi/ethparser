package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.HarvestDTO;
import pro.belbix.ethparser.web3.Web3Service;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HarvestVaultParserTest {

    @Autowired
    private HarvestVaultParser harvestVaultParser;
    @Autowired
    private Web3Service web3Service;

    @Test
    public void parseVaultUNI_ETH_WBTC() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_WBTC,
            11184877,
            1,
            "0xd86d40ccbc02ea258c442185bf16f16d6336fc1b",
            "Deposit",
            "UNI_ETH_WBTC",
            "0x9a75018baa86301c732527856c00d6430fa0f7188efc26600bed7998799240ab_32",
            "0,00084212",
            "0,00084570",
            "1,00424760",
            11776851L,
            1604415805L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11185593 ,
            1,
            "0xefe45d46722dc95502e81f80e6398b16112b5fa7",
            "Deposit",
            "UNI_ETH_DAI",
            "0x36b487961e6df03bee3af329de347079b02d1342037bebd0e8034b8ab01cce0d_274",
            "0,68198003",
            "0,68734177",
            "1,00424760",
            11776851L,
            1604425790L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI2() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11188946 ,
            1,
            "0x49b68b3c022f3531cde7e42ef5ff974193fe2576",
            "Deposit",
            "UNI_ETH_DAI",
            "0x7b13ad50915c24bdc78ba87413027f184d36134436dc88e14f50d5d681e2fd15_44",
            "147,42969692",
            "148,58879257",
            "1,00424760",
            11776851L,
            1604469644L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDC() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDC,
            11187006 ,
            1,
            "0xefc8dcba0188825ad5a35206e8d372d75c488f65",
            "Withdraw",
            "UNI_ETH_USDC",
            "0x1619bc754cbc8b74de04b660260d8509262b9eb10952e2efa08bc6a4fd9b9fae_108",
            "0,00000152",
            "0,00000153",
            "0",
            0L,
            1604444037L,
            true
        );
    }

    @Test
    public void parseVaultCRVRENWBTC() {
        harvestVaultParseTest(
            Vaults.CRVRENWBTC,
            11187206 ,
            1,
            "0x13e252df0cafe34116cec052177b7540afc75f76",
            "Deposit",
            "CRVRENWBTC",
            "0xc0e321b9bf751ac25922b34346e63b6ed6cb76789f9151bb00b8e1a75b4bf644_242",
            "624,51801895",
            "624,89371055",
            "0",
            0L,
            1604446549L,
            true
        );
    }

    private void harvestVaultParseTest(
        String fromVault,
        long onBlock,
        int logId,
        String owner,
        String methodName,
        String vault,
        String id,
        String amount,
        String amountIn,
        String sharePrice,
        Long usdAmount,
        Long blockDate,
        boolean confirmed
    ) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(fromVault),
            new DefaultBlockParameterNumber(onBlock), new DefaultBlockParameterNumber(onBlock));
        assertTrue("Log smaller then necessary", logId < logResults.size());
        HarvestDTO dto = harvestVaultParser.parseVaultLog((Log) logResults.get(logId).get());
        assertDto(dto,
            owner,
            methodName,
            vault,
            id,
            amount,
            amountIn,
            sharePrice,
            usdAmount,
            blockDate,
            confirmed
        );
    }

    private void assertDto(HarvestDTO dto,
                           String owner,
                           String methodName,
                           String vault,
                           String id,
                           String amount,
                           String amountIn,
                           String sharePrice,
                           Long usdAmount,
                           Long blockDate,
                           boolean confirmed) {
        assertNotNull("Dto is null", dto);
        assertAll(() -> assertEquals("owner", owner, dto.getOwner()),
            () -> assertEquals("MethodName", methodName, dto.getMethodName()),
            () -> assertEquals("Vault", vault, dto.getVault()),
            () -> assertEquals("Id", id, dto.getId()),
            () -> assertEquals("Amount", amount, String.format("%.8f", dto.getAmount())),
            () -> assertEquals("AmountIn", amountIn, String.format("%.8f", dto.getAmountIn())),
//            () -> assertEquals("SharePrice", sharePrice, String.format("%.8f", dto.getSharePrice())), //unstable without archive
//            () -> assertEquals("UsdAmount", usdAmount, dto.getUsdAmount()), //unstable without archive
            () -> assertEquals("BlockDate", blockDate, dto.getBlockDate()),
            () -> assertEquals("Confirmed", confirmed, dto.isConfirmed())
        );
    }
}
