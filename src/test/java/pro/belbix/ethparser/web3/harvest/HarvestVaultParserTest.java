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
import pro.belbix.ethparser.entity.HarvestTvlEntity;
import pro.belbix.ethparser.web3.PriceProvider;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.harvest.contracts.Vaults;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
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
    @Autowired
    private HarvestOwnerBalanceCalculator harvestOwnerBalanceCalculator;
    @Autowired
    private HarvestDBService harvestDBService;

    @Before
    public void setUp() throws Exception {
        priceProvider.setUpdateBlockDifference(1);
    }

    @Test
    public void parseVaultDAI_BSG2() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.DAI_BSG,
            11668721,
            LOG_ID,
            "0xfab1ed72a7236a6b34f47ee7ed103d6cd448c441",
            "Withdraw",
            "DAI_BSG",
            "0xf7edbf678eb039fdf8f47e6d2aed4ad98f443ce70e701ef14e405cc4ec1d1ced_155",
            "2,82874611",
            "",
            "",
            603L,
            6248L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultDAI_BSG() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.DAI_BSG,
            11664219,
            LOG_ID,
            "0xa35b52835dc644444881dd51563d13ad987c148c",
            "Deposit",
            "DAI_BSG",
            "0x88999d4cbeff7d76f1a70e9cf43e174aba2c803b34a3aa69807354859cc12ad1_161",
            "10,24413224",
            "",
            "",
            2288L,
            22244L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultDAI_BSG3() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.DAI_BSG,
            11679676,
            LOG_ID,
            "0xf2ff7c888cd0f1a38d06f89658aeb36cf6951841",
            "Deposit",
            "DAI_BSG",
            "0x0b2ddc041e983cea36954a3592c8ba208ea7b2cfdd212deb9a9b97d8b20dad42_159",
            "7,49854995",
            "",
            "",
            1090L,
            6746L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultDAI_BSGS() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.DAI_BSGS,
            11664918,
            LOG_ID,
            "0x2e3a18c95c67158342426eab5a9fd1856e869214",
            "Deposit",
            "DAI_BSGS",
            "0x80164da94c69060dc7e4c2bc19efe7cf224a8d3ba0c25bfdd2af4a4af241cabd_252",
            "57,12271524",
            "",
            "",
            945L,
            123924L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultBAC() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.BAC,
            11664832,
            LOG_ID,
            "0x7cff918c6d506557d06fb307667fca863f07f262",
            "Deposit",
            "BAC",
            "0x63f332bacc17ba1216ae283fb83e08b6ee6ca174a8f15d6e8eb2cc64aac695a0_95",
            "301110,31093957",
            "",
            "",
            249019L,
            471579L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultESD() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.ESD,
            11666935,
            LOG_ID,
            "0x19912c1c3d299b8385504d42132bc9e78552c99e",
            "Deposit",
            "ESD",
            "0xd53954a87d5ef5d9813c826a381947928eb97fa05fb42cb6bed52d3d2e7f8d5f_320",
            "3041863,70114524",
            "",
            "",
            1720555L,
            5746405L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultDSD() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.DSD,
            11671555,
            LOG_ID,
            "0x7707c947b45cab9ce0fbf4505abf637bc4027a5b",
            "Deposit",
            "DSD",
            "0x8098c9c62198557f7265c5138c730fbdc9cc1a6e554e346707b7300178a3cd4c_249",
            "7431,58481815",
            "",
            "",
            4994L,
            18797282L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultONEINCH_ETH_DAI() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.ONEINCH_ETH_DAI,
            11656737,
            LOG_ID,
            "0x856b0303e51b9cb68737a2e9d5a5260d7bb515c4",
            "Deposit",
            "ONEINCH_ETH_DAI",
            "0x7e489e15a4f060692645ab78131b421a5e00c057cec3f7c8360ddb012ce04574_101",
            "1313,48394098",
            "",
            "",
            2605L,
            196916L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultONEINCH_ETH_USDC() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.ONEINCH_ETH_USDC,
            11652370,
            LOG_ID,
            "0xdb22c56f26940803095704634d0e305901cf83af",
            "Deposit",
            "ONEINCH_ETH_USDC",
            "0x5f5ee66cc4872778dac722c39af219d44fe395978adf7c1aab6529661c960276_173",
            "7,70258211",
            "",
            "",
            18749L,
            160570L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultONEINCH_ETH_USDT() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.ONEINCH_ETH_USDT,
            11657337,
            LOG_ID,
            "0x3c8319dd83fa18ec1a0df2acf65277a731514d67",
            "Deposit",
            "ONEINCH_ETH_USDT",
            "0x7373221e30209d5d254f9b6f3a8684d6f54a677d9f7b5d2ae483c933b7a93200_271",
            "14,32730677",
            "",
            "",
            34950L,
            47028L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultONEINCH_ETH_WBTC() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.ONEINCH_ETH_WBTC,
            11660459,
            LOG_ID,
            "0xb3bd674309ba3fd345134b28326326a0f0b3c6ed",
            "Deposit",
            "ONEINCH_ETH_WBTC",
            "0x2f244375de17c78430758b82bbeb786a3fe213fd3756732fbb5f274be67a385f_128",
            "11749,43295860",
            "",
            "",
            27519431L,
            27528776L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultCRV_OBTC() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.CRV_OBTC,
            11653428,
            LOG_ID,
            "0xde50bd8fd4e7b8e9fc048938d450120c51fd6da5",
            "Withdraw",
            "CRV_OBTC",
            "0x63150089d9db9da63740fb582f0fa46356fe3f4fddd98766225050f6b138217f_187",
            "0,74794491",
            "",
            "",
            28746L,
            1480484L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultWETH_V02() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.WETH_V0,
            11289211,
            LOG_ID,
            "0xe4b73f379705eac71bf2dc17e33e069a90a4041d",
            "Withdraw",
            "WETH_V0",
            "0x4ccb569444c5d3eb1cd4fa39d8ac9e78a616c2e96c4badc3468b8b53a34e264e_250",
            "4,54399269",
            "",
            "",
            0L,
            0L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultSUSHI_MIS_USDT() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.SUSHI_MIS_USDT,
            11616169,
            LOG_ID,
            "0xff21190b583e185235f5da7537f939d64ec1f6b5",
            "Deposit",
            "SUSHI_MIS_USDT",
            "0xe34a6dfb4dd357c684c45cfb59af3efa9b3770b5d1a37feb5bf8c83df3d891be_285",
            "0,00000784",
            "",
            "",
            405L,
            279662L,
            true
        );
        assertNotNull(dto);
    }

    @Test
    public void parseVaultSUSHI_MIC_USDT() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.SUSHI_MIC_USDT,
            11615991,
            LOG_ID,
            "0x8dc06d76a63e79c0b77633f60bc7b9e14ed94d18",
            "Deposit",
            "SUSHI_MIC_USDT",
            "0x23f459577376439163cd5cf60e0545e7c7920302092e3e77b1f24dd30cd6989f_59",
            "0,01899012",
            "",
            "",
            41679L,
            2399350L,
            true
        );
        assertNotNull(dto);
    }

    @Test
    public void parseVaultUNI_DAI_BAS() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.UNI_DAI_BAS,
            11619379,
            LOG_ID,
            "0x068a01a3443df8230d423af83110d278d8dc9018",
            "Deposit",
            "UNI_DAI_BAS",
            "0x0a6ff77be622ad6dca78f4ecdc115043380d6ef403cb2333f45a3443d33307a7_278",
            "135,29092535",
            "",
            "",
            4441L,
            667770L,
            true
        );
        assertNotNull(dto);
    }

    @Test
    public void parseVaultUNI_BAC_DAI() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.UNI_BAC_DAI,
            11615995,
            LOG_ID,
            "0xe2bb94210b41ce4c01b9b97f3ac62e728e472f9c",
            "Deposit",
            "UNI_BAC_DAI",
            "0xa78e9b989f0997ebf74afba986509a3233414155438e351b37987cfe93ff938b_95",
            "179375,42468206",
            "",
            "",
            385046L,
            415514L,
            true
        );
        assertNotNull(dto);
    }

    @Test
    public void parseVaultUNI_ETH_WBTC2() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.UNI_ETH_WBTC,
            11082567,
            LOG_ID,
            "0x15e7e224dcfa4dba59f342f01c70007b8a8f4aa0",
            "Deposit",
            "UNI_ETH_WBTC",
            "0xf280ca6140b715d268c206196313d03ee157c02595699c1221724fe85a72f7b8_11",
            "0,00175897",
            "",
            "",
            793188L,
            61345192L,
            true
        );
        assertNotNull(dto);
    }

    @Test
    public void parseVaultUNI_ETH_DAI_V0_migration() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.UNI_ETH_DAI_V0,
            11050173,
            LOG_ID,
            "0xc2b27903e0281740994895c32ee40c31dac3197d",
            "Withdraw",
            "UNI_ETH_DAI_V0",
            "0x44ed856bd4de7e4065cb4939b58f4ebc8ec30b564930d4c25b33b81cc03cead6_310",
            "12448,99201236",
            "",
            "",
            0L,
            0L,
            true
        );

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", "0,00000000", String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", "0,00000000", String.format("%.8f", dto.getOwnerBalanceUsd()))
        );

        HarvestDTO migration = dto.getMigration();
        assertNotNull(migration);
        assertDto(migration,
            "0xc2b27903e0281740994895c32ee40c31dac3197d",
            "Deposit",
            "UNI_ETH_DAI",
            "0x44ed856bd4de7e4065cb4939b58f4ebc8ec30b564930d4c25b33b81cc03cead6_313",
            "12657,60435602",
            "",
            "",
            583939L,
            15608163L,
            true
        );
        harvestOwnerBalanceCalculator.fillBalance(migration);
        assertAll(
            () -> assertEquals("owner balance", "12657,60435602", String.format("%.8f", migration.getOwnerBalance())),
            () -> assertEquals("owner balance usd", "583939,02122719", String.format("%.8f", migration.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI_V0() {
        HarvestDTO dto = harvestVaultParseTest(
            Vaults.UNI_ETH_DAI_V0,
            10884882,
            LOG_ID,
            "0xa35b52835dc644444881dd51563d13ad987c148c",
            "Deposit",
            "UNI_ETH_DAI_V0",
            "0xdf607d47e8fa5f48891d61d6acbeef0cfa674c6c814433294ef60bdfe5bf5dda_286",
            "241,08913795",
            "",
            "",
            11103L,
            40861L,
            true
        );

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", "241,08913795", String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", "11102,67103786", String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVaultIDX_ETH_DPI() {
        HarvestDTO dto = harvestVaultParseTest(
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
            3353L,
            5712L,
            true
        );

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", "6,08144172", String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", "3352,40976906", String.format("%.8f", dto.getOwnerBalanceUsd()))
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
            423610L,
            31814577L,
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
            421424L,
            65653968L,
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
            8764980L,
            130781486L,
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
            7473924L,
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
            14366L,
            10786356L,
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
            22232L,
            2661180L,
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
            3668456L,
            124269201L,
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

    private HarvestDTO harvestVaultParseTest(
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
        return dto;
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
