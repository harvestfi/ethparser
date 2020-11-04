package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
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
            422843L,
            1604415805L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11185593,
            1,
            "0xefe45d46722dc95502e81f80e6398b16112b5fa7",
            "Deposit",
            "UNI_ETH_DAI",
            "0x36b487961e6df03bee3af329de347079b02d1342037bebd0e8034b8ab01cce0d_274",
            "0,68198003",
            "0,68734177",
            "1,00424760",
            32L,
            1604425790L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI2() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11188946,
            1,
            "0x49b68b3c022f3531cde7e42ef5ff974193fe2576",
            "Deposit",
            "UNI_ETH_DAI",
            "0x7b13ad50915c24bdc78ba87413027f184d36134436dc88e14f50d5d681e2fd15_44",
            "147,42969692",
            "148,58879257",
            "1,00424760",
            6885L,
            1604469644L,
            true
        );
    }

    @Test
    public void parseVaultUSDT() {
        harvestVaultParseTest(
            Vaults.USDT,
            11190589,
            1,
            "0x0e2481cdc9ffd4da1ee17b3060d41c7a0a4906b7",
            "Deposit",
            "USDT",
            "0xa212d43adde6faeab6039288af6eeee5aefeea8c299ab6a44ce8db1aa93975d2_249",
            "0,03189300",
            "0,00066200",
            "0",
            0L,
            1604491973L,
            true
        );
    }

    @Test
    public void parseVaultUSDT2() {
        harvestVaultParseTest(
            Vaults.USDT,
            11188165,
            1,
            "0x5dd63936fa77c6b7888cb664227ccd5b27e4f128",
            "Withdraw",
            "USDT",
            "0x96c51a098e367cdc95e9084dd5dab6b1fba07d7abf1f7f859a505c7bc5d3910b_252",
            "2365639,42309700",
            "1998649,72839200",
            "0,00066200",
            1998648L,
            1604459302L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDT() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDT,
            11189726,
            1,
            "0xff3083c7d442dbd4cfe9cfe2043e40df1ce2a75d",
            "Deposit",
            "UNI_ETH_USDT",
            "0x45cb031d9b7bf0cfb8df23b5e3cf1d8309aeb6de905a16e4f5b3e009c556cb68_202",
            "0,00068016",
            "0,00068416",
            "1,00424760",
            33656L,
            1604480111L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDT2() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDT,
            11188401,
            1,
            "0xb5ccc38dee973ede2d78c071e4b9c2e49783101c",
            "Withdraw",
            "UNI_ETH_USDT",
            "0xd9d77ccdd3c80cb6d78c5061a73a66cbd64cd936b65f5ac92e5e5e96265e4fde_114",
            "0,01479451",
            "0,01488166",
            "1,00424760",
            730803L,
            1604462506L,
            true
        );
    }

    @Test
    public void parseVaultWETH() {
        harvestVaultParseTest(
            Vaults.WETH,
            11190612,
            1,
            "0x071fe6456f926722b2731087395a5335612269fd",
            "Deposit",
            "WETH",
            "0x1f367e708d58c31349dbe9ebe8c3fbcb800351839cb0a56b061be84b45f98b91_89",
            "9,99356586",
            "10,00000000",
            "1,00424760",
            3820L,
            1604492330L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDC() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDC,
            11187006,
            1,
            "0xefc8dcba0188825ad5a35206e8d372d75c488f65",
            "Withdraw",
            "UNI_ETH_USDC",
            "0x1619bc754cbc8b74de04b660260d8509262b9eb10952e2efa08bc6a4fd9b9fae_108",
            "0,00000152",
            "0,00000153",
            "0",
            75L,
            1604444037L,
            true
        );
    }

    @Test
    public void parseVaultUSDC() {
        harvestVaultParseTest(
            Vaults.USDC,
            11190589,
            1,
            "0x0e2481cdc9ffd4da1ee17b3060d41c7a0a4906b7",
            "Deposit",
            "USDC",
            "0xa212d43adde6faeab6039288af6eeee5aefeea8c299ab6a44ce8db1aa93975d2_243",
            "0,03189300",
            "0,02663700",
            "0",
            0L,
            1604491973L,
            true
        );
    }

    @Test
    public void parseVaultUSDC2() {
        harvestVaultParseTest(
            Vaults.USDC,
            11188063,
            1,
            "0xe5350e927b904fdb4d2af55c566e269bb3df1941",
            "Deposit",
            "USDC",
            "0xa00eb4720ef6b31d060c704117c28704a69c9b967c2eec10479b4687f8321d89_46",
            "1990429,30747800",
            "1662355,80963700",
            "0",
            1662354L,
            1604457921L,
            true
        );
    }

    @Test
    public void parseVaultCRVRENWBTC() {
        harvestVaultParseTest(
            Vaults.CRVRENWBTC,
            11187206,
            1,
            "0x13e252df0cafe34116cec052177b7540afc75f76",
            "Deposit",
            "CRVRENWBTC",
            "0xc0e321b9bf751ac25922b34346e63b6ed6cb76789f9151bb00b8e1a75b4bf644_242",
            "624,51801895",
            "624,89371055",
            "0",
            8544662L,
            1604446549L,
            true
        );
    }

    @Test
    public void parseVaultDAI() {
        harvestVaultParseTest(
            Vaults.DAI,
            11189347,
            1,
            "0xc49c90a526086b06279c205c24d0e378f4c74c3e",
            "Withdraw",
            "DAI",
            "0xb161b9f57f6d77e43bdca60644e41662db2d0e144882a649a78ecdfb63b462b2_192",
            "4549,24977049",
            "4414,54065701",
            "0",
            4414L,
            1604475193L,
            true
        );
    }

    @Test
    public void parseVaultWBTC() {
        harvestVaultParseTest(
            Vaults.WBTC,
            11190733,
            1,
            "0x194b379c59a82c0f903768e9fc7a0440a4794708",
            "Withdraw",
            "WBTC",
            "0xca58ffb9adf64b3d153d1a755d17f63c495478639f90c1b60a002c3e6879ea23_135",
            "0,02802258",
            "0,02805316",
            "0",
            386L,
            1604493911L,
            true
        );
    }

    @Test
    public void parseVaultRENBTC() {
        harvestVaultParseTest(
            Vaults.RENBTC,
            11187223,
            1,
            "0xb9670cebabc4d89ed58c8cfc6393777a5cceeabe",
            "Deposit",
            "RENBTC",
            "0xa1d8c9602b4dcecdd0e229488b421996b5bcd0b6eea5b6dc2addfb7bd8ea6ea3_197",
            "1,02540908",
            "1,02579537",
            "0",
            14025L,
            1604446751L,
            true
        );
    }

    @Test
    public void parseVaultSUSHI_WBTC_TBTC() {
        harvestVaultParseTest(
            Vaults.SUSHI_WBTC_TBTC,
            11055609,
            1,
            "0xe9b05bc1fa8684ee3e01460aac2e64c678b9da5d",
            "Deposit",
            "SUSHI_WBTC_TBTC",
            "0x3aa1b7030d4487c0e91e2709ebf3910189ed45454fbfafa2a418e2581ff8b11b_209",
            "0,00000981",
            "0,00000981",
            "0",
            27056L,
            1602701976L,
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
            () -> assertEquals("UsdAmount", usdAmount, dto.getUsdAmount(), dto.getUsdAmount() / 30.0),
            () -> assertEquals("BlockDate", blockDate, dto.getBlockDate()),
            () -> assertEquals("Confirmed", confirmed, dto.isConfirmed())
        );
    }
}
