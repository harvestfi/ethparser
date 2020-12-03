package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Log;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HarvestVaultParserTest {

    private static final int LOG_ID = 0;

    @Autowired
    private HarvestVaultParserV2 harvestVaultParser;
    @Autowired
    private Web3Service web3Service;
    @Autowired
    private PriceProvider priceProvider;

    @Before
    public void setUp() throws Exception {
        priceProvider.setUpdateTimeout(0);
    }

    @Test
    public void parseVaultIDX_ETH_DPI() {
        harvestVaultParseTest(
            Vaults.IDX_ETH_DPI,
            11378768,
            LOG_ID,
            "0xe4b8c8e33b17ec8517403852d7bb272134a2271a",
            "Deposit",
            "IDX_ETH_DPI",
            "0x35843129de10eead25090075c443b8cb03b35b946f5b777918260973349fbb05_36",
            "6,08144172",
            "0",
            "0",
            743L,
            1266L,
            true
        );
    }

    @Test
    public void parseVaultSUSHI_ETH_USDT() {
        harvestVaultParseTest(
            Vaults.SUSHI_ETH_USDT,
            11279480,
            LOG_ID,
            "0x385153335428d17024b32614ba66259ed307c9ba",
            "Deposit",
            "SUSHI_ETH_USDT",
            "0xe83f8805e98f2e2cccca82fa2bd48785914a09d9887e750183fceb4543f92654_292",
            "0,00089347",
            "0",
            "0",
            48497L,
            15531830L,
            true
        );
    }

    @Test
    public void parseVaultSUSHI_ETH_USDC() {
        harvestVaultParseTest(
            Vaults.SUSHI_ETH_USDC,
            11279940,
            LOG_ID,
            "0xa20f9874dd1edccbec1beda894f98f45069e4205",
            "Deposit",
            "SUSHI_ETH_USDC",
            "0x265bd97b6dfc7df3799ad6ca077ef8ae36b8d62f1c7ba8368f788f4535f9bd16_56",
            "0,00017192",
            "0",
            "0",
            9443L,
            14126008L,
            true
        );
    }

    @Test
    public void parseVaultSUSHI_ETH_DAI() {
        harvestVaultParseTest(
            Vaults.SUSHI_ETH_DAI,
            11279440,
            LOG_ID,
            "0x7b5c7755dffc97bf44677a084b591c38bf894abf",
            "Deposit",
            "SUSHI_ETH_DAI",
            "0x157cbc1828110467acf387ffeeb4f9a53255394ea7c1edb2bd388e145e1b7cf5_333",
            "181,23103431",
            "0",
            "0",
            9328L,
            10132491L,
            true
        );
    }

    @Test
    public void parseVaultSUSHI_ETH_WBTC() {
        harvestVaultParseTest(
            Vaults.SUSHI_ETH_WBTC,
            11279665,
            LOG_ID,
            "0x9ebaff2192d2746fec76561bdf72fd249d7a73ab",
            "Deposit",
            "SUSHI_ETH_WBTC",
            "0x44c68835607ae3286d2ca09bfe206527bfbf8e3a328902d25933ef80f8acdf42_196",
            "0,00003057",
            "0",
            "0",
            423523L,
            31808001L,
            true
        );
    }

    @Test
    public void shouldNotParseVaultSUSHI_ETH_DAI() {
        shouldNotParse(Vaults.SUSHI_ETH_DAI, 11278329, LOG_ID);
    }

    @Test
    public void parseVaultPS_V0() {
        harvestVaultParseTest(
            Vaults.PS_V0,
            10798055,
            LOG_ID,
            "0x0c124a0b302f06072ddc1fe1ce991578ecb248d6",
            "Deposit",
            "PS_V0",
            "0xe51375ce951af9c2f9cc61a94bf3dc1676a8440d93e9955c4af040646da4b4f2_259",
            "1,96201716",
            "0",
            "0",
            445L,
            445L,
            true
        );
    }

    @Test
    public void parseVaultUSDC_V0_stake() {
        shouldNotParse(Vaults.USDC_V0, 11021481, LOG_ID);
    }

    @Test
    public void parseVaultPS2() {
        harvestVaultParseTest(
            Vaults.PS,
            11262684,
            LOG_ID,
            "0xad77e73a9fd5d002bd1d043e6a4c6a456c9524fb",
            "Deposit",
            "PS",
            "0x1174e819acca01e2611a6f5b8846115938f46c2b014d258ffa5ce3fbb3fbb51c_267",
            "1,75901662",
            "0",
            "0",
            196L,
            19750016L,
            true
        );
    }

    @Test
    public void parseVaultPS() {
        harvestVaultParseTest(
            Vaults.PS,
            10964982,
            LOG_ID,
            "0x640236965becf920a70a5dcd44b5c9c18f283095",
            "Withdraw",
            "PS",
            "0x62649ba91314e388be6f62088a4506e9629a9e5401805d02fe14530ac8107d8c_73",
            "300,05543538",
            "0",
            "0",
            26207L,
            844047L,
            true
        );
    }

    @Test
    public void parseVaultWETH_V0() {
        harvestVaultParseTest(
            Vaults.WETH_V0,
            11037623,
            LOG_ID,
            "0x602b2d2278465ea5823898000140cef95f2b8d56",
            "Deposit",
            "WETH_V0",
            "0xb6ce381d9f423e1ff5fcb6164f95db8c0b8db99abced9f0bc43806df232fd52e_148",
            "14,94268625",
            "0",
            "0",
            5602L,
            20766702L,
            true
        );
    }

    @Test
    public void parseVaultWETH_V0_migration() {
        harvestVaultParseTest(
            Vaults.WETH_V0,
            11207867,
            LOG_ID,
            "0x252e7e8b9863f81798b1fef8cfd9741a46de653c",
            "Withdraw",
            "WETH_V0",
            "0xd4d4ea3fd788fed4b14ca1836d90cedb171a42af3dc08c252a37549ef523ebd3_39",
            "4,98502606",
            "0",
            "0",
            0L,
            0L,
            true
        );
    }

    @Test
    public void parseVaultUSDC_V0() {
        harvestVaultParseTest(
            Vaults.USDC_V0,
            11092650,
            LOG_ID,
            "0x39075d2473005586389ef2bbc3cf85fc3e9d09cc",
            "Deposit",
            "USDC_V0",
            "0x96534eaeb0897a804884807bb8d14ea886b6eb8f48fc5021290e6f85e06f6492_284",
            "9025,23421600",
            "82,05226438",
            "1,00424760",
            9334L,
            98066088L,
            true
        );
    }

    @Test
    public void parseVaultWETH2() {
        harvestVaultParseTest(
            Vaults.WETH,
            11147169,
            LOG_ID,
            "0xc22bc5f7e5517d7a5df9273d66e254d4b549523c",
            "Withdraw",
            "WETH",
            "0x8ca5430e2311a0ba200982f90fb03fae00d1a1bf7cf0c9f6ab4b5519a7cd3613_272",
            "82,00902140",
            "82,05226438",
            "1,00424760",
            31655L,
            7680756L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_WBTC() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_WBTC,
            11184877,
            LOG_ID,
            "0xd86d40ccbc02ea258c442185bf16f16d6336fc1b",
            "Deposit",
            "UNI_ETH_WBTC",
            "0x9a75018baa86301c732527856c00d6430fa0f7188efc26600bed7998799240ab_30",
            "0,00084212",
            "0,00084570",
            "1,00424760",
            421722L,
            65700482L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11185593,
            LOG_ID,
            "0xefe45d46722dc95502e81f80e6398b16112b5fa7",
            "Deposit",
            "UNI_ETH_DAI",
            "0x36b487961e6df03bee3af329de347079b02d1342037bebd0e8034b8ab01cce0d_272",
            "0,68198003",
            "0,68734177",
            "1,00424760",
            32L,
            24905449L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI2() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11188946,
            LOG_ID,
            "0x49b68b3c022f3531cde7e42ef5ff974193fe2576",
            "Deposit",
            "UNI_ETH_DAI",
            "0x7b13ad50915c24bdc78ba87413027f184d36134436dc88e14f50d5d681e2fd15_42",
            "147,42969692",
            "148,58879257",
            "1,00424760",
            6896L,
            24205978L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI3() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11059006,
            LOG_ID,
            "0xa51030ce6ee1f832ea559499cd4ae57c26a5c614",
            "Deposit",
            "UNI_ETH_DAI",
            "0xee05ff562e847cd2b51f86280eaef1892c536cde8153b571415811faa95929e6_158",
            "0,33045221",
            "0,33045221",
            "1,00424760",
            15L,
            41841786L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI4() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_DAI,
            11059057,
            LOG_ID,
            "0x8a85ee300b04f9f1622f13941a58cbdabec14af4",
            "Withdraw",
            "UNI_ETH_DAI",
            "0x972267d1f6fe7eef882736a8e0c26b7e38f4bc803170e194f476b638173db215_92",
            "158000,22215390",
            "158000,22215390",
            "1,00424760",
            7262270L,
            34622599L,
            true
        );
    }

    @Test
    public void parseVaultUSDT() {
        harvestVaultParseTest(
            Vaults.USDT,
            11190589,
            LOG_ID,
            "0x0e2481cdc9ffd4da1ee17b3060d41c7a0a4906b7",
            "Deposit",
            "USDT",
            "0xa212d43adde6faeab6039288af6eeee5aefeea8c299ab6a44ce8db1aa93975d2_247",
            "0,00078300",
            "0,00066200",
            "0",
            0L,
            8979591L,
            true
        );
    }

    @Test
    public void parseVaultUSDT2() {
        harvestVaultParseTest(
            Vaults.USDT,
            11188165,
            LOG_ID,
            "0x5dd63936fa77c6b7888cb664227ccd5b27e4f128",
            "Withdraw",
            "USDT",
            "0x96c51a098e367cdc95e9084dd5dab6b1fba07d7abf1f7f859a505c7bc5d3910b_250",
            "2365639,42309700",
            "1998649,72839200",
            "0,00066200",
            1998648L,
            8949404L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDT() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDT,
            11189726,
            LOG_ID,
            "0xff3083c7d442dbd4cfe9cfe2043e40df1ce2a75d",
            "Deposit",
            "UNI_ETH_USDT",
            "0x45cb031d9b7bf0cfb8df23b5e3cf1d8309aeb6de905a16e4f5b3e009c556cb68_200",
            "0,00068016",
            "0,00068416",
            "1,00424760",
            33497L,
            32457454L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDT2() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDT,
            11188401,
            LOG_ID,
            "0xb5ccc38dee973ede2d78c071e4b9c2e49783101c",
            "Withdraw",
            "UNI_ETH_USDT",
            "0xd9d77ccdd3c80cb6d78c5061a73a66cbd64cd936b65f5ac92e5e5e96265e4fde_112",
            "0,01479451",
            "0,01488166",
            "1,00424760",
            735155L,
            32553250L,
            true
        );
    }

    @Test
    public void parseVaultWETH() {
        harvestVaultParseTest(
            Vaults.WETH,
            11190612,
            LOG_ID,
            "0x071fe6456f926722b2731087395a5335612269fd",
            "Deposit",
            "WETH",
            "0x1f367e708d58c31349dbe9ebe8c3fbcb800351839cb0a56b061be84b45f98b91_87",
            "9,99356586",
            "10,00000000",
            "1,00424760",
            3818L,
            8338012L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDC() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDC,
            11187006,
            LOG_ID,
            "0xefc8dcba0188825ad5a35206e8d372d75c488f65",
            "Withdraw",
            "UNI_ETH_USDC",
            "0x1619bc754cbc8b74de04b660260d8509262b9eb10952e2efa08bc6a4fd9b9fae_106",
            "0,00000152",
            "0,00000153",
            "0",
            75L,
            47475148L,
            true
        );
    }

    @Test
    public void parseVaultUSDC() {
        harvestVaultParseTest(
            Vaults.USDC,
            11190589,
            LOG_ID,
            "0x0e2481cdc9ffd4da1ee17b3060d41c7a0a4906b7",
            "Deposit",
            "USDC",
            "0xa212d43adde6faeab6039288af6eeee5aefeea8c299ab6a44ce8db1aa93975d2_241",
            "0,03189300",
            "0,02663700",
            "0",
            0L,
            10844841L,
            true
        );
    }

    @Test
    public void parseVaultUSDC2() {
        harvestVaultParseTest(
            Vaults.USDC,
            11188063,
            LOG_ID,
            "0xe5350e927b904fdb4d2af55c566e269bb3df1941",
            "Deposit",
            "USDC",
            "0xa00eb4720ef6b31d060c704117c28704a69c9b967c2eec10479b4687f8321d89_44",
            "1990429,30747800",
            "1662355,80963700",
            "0",
            1662354L,
            10913194L,
            true
        );
    }

    @Test
    public void parseVaultCRVRENWBTC() {
        harvestVaultParseTest(
            Vaults.CRVRENWBTC,
            11187206,
            LOG_ID,
            "0x13e252df0cafe34116cec052177b7540afc75f76",
            "Deposit",
            "CRVRENWBTC",
            "0xc0e321b9bf751ac25922b34346e63b6ed6cb76789f9151bb00b8e1a75b4bf644_240",
            "624,51801895",
            "624,89371055",
            "0",
            8777930L,
            130974716L,
            true
        );
    }

    @Test
    public void parseVaultDAI() {
        harvestVaultParseTest(
            Vaults.DAI,
            11189347,
            LOG_ID,
            "0xc49c90a526086b06279c205c24d0e378f4c74c3e",
            "Withdraw",
            "DAI",
            "0xb161b9f57f6d77e43bdca60644e41662db2d0e144882a649a78ecdfb63b462b2_190",
            "4549,24977049",
            "4414,54065701",
            "0",
            4414L,
            1297996L,
            true
        );
    }

    @Test
    public void parseVaultWBTC() {
        harvestVaultParseTest(
            Vaults.WBTC,
            11190733,
            LOG_ID,
            "0x194b379c59a82c0f903768e9fc7a0440a4794708",
            "Withdraw",
            "WBTC",
            "0xca58ffb9adf64b3d153d1a755d17f63c495478639f90c1b60a002c3e6879ea23_133",
            "0,02802258",
            "0,02805316",
            "0",
            387L,
            7471619L,
            true
        );
    }

    @Test
    public void parseVaultRENBTC() {
        harvestVaultParseTest(
            Vaults.RENBTC,
            11187223,
            LOG_ID,
            "0xb9670cebabc4d89ed58c8cfc6393777a5cceeabe",
            "Deposit",
            "RENBTC",
            "0xa1d8c9602b4dcecdd0e229488b421996b5bcd0b6eea5b6dc2addfb7bd8ea6ea3_194",
            "1,02540908",
            "1,02579537",
            "0",
            14409L,
            10818598L,
            true
        );
    }

    @Test
    public void parseVaultSUSHI_WBTC_TBTC() {
        harvestVaultParseTest(
            Vaults.SUSHI_WBTC_TBTC,
            11055609,
            LOG_ID,
            "0xe9b05bc1fa8684ee3e01460aac2e64c678b9da5d",
            "Deposit",
            "SUSHI_WBTC_TBTC",
            "0x3aa1b7030d4487c0e91e2709ebf3910189ed45454fbfafa2a418e2581ff8b11b_207",
            "0,00000981",
            "0,00000981",
            "0",
            22349L,
            2675138L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDC2() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDC,
            11061363,
            LOG_ID,
            "0x4c8133051b300ccd66b6b35c5a0af15b6a97012a",
            "Deposit",
            "UNI_ETH_USDC",
            "0xd55e956b30b6fd561700a7a87cc42c1a50b88645d38451d1b5ff706660a64d16_46",
            "0,27332899",
            "0,27336849",
            "0",
            13200978L,
            88650256L,
            true
        );
    }

    @Test
    public void parseVaultUNI_ETH_USDC3() {
        harvestVaultParseTest(
            Vaults.UNI_ETH_USDC,
            11105503,
            LOG_ID,
            "0xc50b3f8f200ae3f1e8dca71ca770c3c4ea94a083",
            "Deposit",
            "UNI_ETH_USDC",
            "0x0d66e450cf9cca6a5a838e3ecf11f5f558d3d9494a7ccd0a9ddda2697730385b_221",
            "0,07969178",
            "0,07984819",
            "0",
            4009528L,
            107605622L,
            true
        );
    }

    @Test
    public void parseVaultCRVRENWBTC2() {
        harvestVaultParseTest(
            Vaults.CRVRENWBTC,
            11105759,
            LOG_ID,
            "0x875abe6f1e2aba07bed4a3234d8555a0d7656d12",
            "Withdraw",
            "CRVRENWBTC",
            "0xda678594b1a8d10c34312bbcfce496cc5fd3ba6bae2065bfe98ba4c005574d1a_137",
            "282,00527470",
            "282,01465792",
            "0",
            3664775L,
            124144485L,
            true
        );
    }

    @Test
    public void parseVaultTUSD() {
        harvestVaultParseTest(
            Vaults.TUSD,
            11005889,
            LOG_ID,
            "0xb71cd2a879c8d887ea8d75155ff51116178641c0",
            "Withdraw",
            "TUSD",
            "0x1c3112e872cd47ee3dc3405b4159f1ec59cd4141dcd3a56a789e0abe336c1d6f_160",
            "1474561,71532449",
            "1473478,20140244",
            "0",
            1473502L,
            3124008L,
            true
        );
    }

    private void shouldNotParse(String fromVault, int onBlock, int logId) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(fromVault), onBlock, onBlock);
        assertTrue("Log smaller then necessary", logId < logResults.size());
        HarvestDTO dto = harvestVaultParser.parseVaultLog((Log) logResults.get(logId).get());
        assertNull(dto);
    }

    private void harvestVaultParseTest(
        String fromVault,
        int onBlock,
        int logId,
        String owner,
        String methodName,
        String vault,
        String id,
        String amount,
        String amountIn,
        String sharePrice,
        Long usdAmount,
        Long usdTvl,
        boolean confirmed
    ) {
        List<LogResult> logResults = web3Service.fetchContractLogs(singletonList(fromVault), onBlock, onBlock);
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
            usdTvl,
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
                           Long usdTvl,
                           boolean confirmed) {
        assertNotNull("Dto is null", dto);
        assertAll(() -> assertEquals("owner", owner, dto.getOwner()),
            () -> assertEquals("MethodName", methodName, dto.getMethodName()),
            () -> assertEquals("Vault", vault, dto.getVault()),
            () -> assertEquals("Id", id, dto.getId()),
            () -> assertEquals("Amount", amount, String.format("%.8f", dto.getAmount())),
//            () -> assertEquals("AmountIn", amountIn, String.format("%.8f", dto.getAmountIn())),
//            () -> assertEquals("SharePrice", sharePrice, String.format("%.8f", dto.getSharePrice())), //unstable without archive
            () -> assertEquals("UsdAmount", String.format("%.0f", usdAmount.doubleValue()),
                String.format("%.0f", dto.getUsdAmount().doubleValue())),
            () -> assertEquals("usdTvl", String.format("%.0f", usdTvl.doubleValue()),
                String.format("%.0f", dto.getLastUsdTvl())),
            () -> assertEquals("Confirmed", confirmed, dto.isConfirmed())
        );
    }
}
