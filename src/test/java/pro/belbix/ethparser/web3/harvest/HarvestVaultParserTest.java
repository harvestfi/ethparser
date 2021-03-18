package pro.belbix.ethparser.web3.harvest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import pro.belbix.ethparser.entity.v0.HarvestTvlEntity;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.harvest.db.HarvestDBService;
import pro.belbix.ethparser.web3.harvest.parser.HarvestVaultParserV2;
import pro.belbix.ethparser.web3.prices.PriceProvider;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
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
    @Autowired
    private ContractLoader contractLoader;

    @BeforeEach
    public void setUp() throws Exception {
        contractLoader.load();
        priceProvider.setUpdateBlockDifference(1);
    }

    @Test
    public void parseVault_ETH_DAI_HODL() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x29EC64560ab14d3166222Bf07c3F29c4916E0027",
            12029883,
            LOG_ID,
            "0x74c4bdea0f077ad7a29f988a3521a5289b017b08",
            "Withdraw",
            "ETH_DAI_HODL",
            "0x8c7ecf6045f1d5490c0625a9c315a5af8bd75e29cf50ebbe694f3f710a7c34f3_312",
            "95.41784704",
            "",
            "",
            10143L,
            7551281L,
            true
        );
    }

    @Test
    public void parseVault_ONEINCH_ETH_ONEINCH() {
        HarvestDTO dto = harvestVaultParseTest(
            "0xfca949e34ecd9de519542cf02054de707cf361ce",
            11908406,
            LOG_ID,
            "0x7ba605bc00ea26512a639d5e0335eaeb3e81ad94",
            "Deposit",
            "ONEINCH_ETH_ONEINCH",
            "0x8570240c0e350df6663d720689c5f5061372cf66a019dd0fbfa2264df0b6362d_240",
            "0,07288828",
            "",
            "",
            0L,
            0L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", numberFormat("0,07288828"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("0.22055181"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVault_iPS() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x1571eD0bed4D987fe2b498DdBaE7DFA19519F651",
            11857842,
            LOG_ID,
            "0x0c124a0b302f06072ddc1fe1ce991578ecb248d6",
            "Deposit",
            "iPS",
            "0x560acf23f19347c0c6652ee4b35ba36ef067d943e014ef38c7366caaae9180e9_302",
            "0,10105548",
            "",
            "",
            35L,
            35L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", numberFormat("0,10105548"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("35.23048816"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVault_SUSHI_SUSHI_ETH() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x5aDe382F38A09A1F8759D06fFE2067992ab5c78e",
            11833261,
            LOG_ID,
            "0xb9bcd154b5a636bb3b049e7dea7da4cb47d6cc81",
            "Deposit",
            "SUSHI_SUSHI_ETH",
            "0xdc7b3578dcccc8cbefe8b38317ed55bc703499bdce37048348abf240b2560bd4_249",
            "358,92678598",
            "",
            "",
            130020L,
            338498L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", numberFormat("359,94131414"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("130019.75772826"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVault_CRV_AAVE() {
        HarvestDTO dto = harvestVaultParseTest(
            "0xc3EF8C4043D7cf1D15B6bb4cd307C844E0BA9d42",
            11836887,
            LOG_ID,
            "0x7ba605bc00ea26512a639d5e0335eaeb3e81ad94",
            "Deposit",
            "CRV_AAVE",
            "0x3e4399ec429da3e0d2ce6680ff4f20a52735940b36a8f60a81186b741d52dfed_170",
            "68,32630764",
            "",
            "",
            68L,
            68L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", numberFormat("68,32630764"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("68,32630764"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVault_USDC_migration() {
        HarvestDTO dto = harvestVaultParseTest(
            "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE",
            11094671,
            LOG_ID,
            "0xf00dd244228f51547f0563e60bca65a30fbf5f7f",
            "Deposit",
            "USDC",
            "0x64b552fd99d6abb1127c06cc866e328e9670f82f201f14dfc9895158a927386a_42",
            "99555832,75969400",
            "97541818",
            "97541818",
            97541818L,
            97541818L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", numberFormat("0,00000000"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("0,00000000"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVault_CRVRENWBTC_badger() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x9aA8F427A17d6B0d91B6262989EdC7D45d6aEdf8",
            11823682,
            LOG_ID,
            "0xda25ee226e534d868f0dd8a459536b03fee9079b",
            "Deposit",
            "CRVRENWBTC",
            "0xf78f241567e2472e78b345a4fc962adbf93e3953a4bbd738b1efe93e435d53cc_354",
            "899,91949809",
            "",
            "",
            42064610L,
            295051243L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", numberFormat("0,00000000"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("0,00000000"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVault_PS() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50",
            11800579,
            LOG_ID,
            "0x3fded1ff35d93fea43c9f8e5dea1a392d5d691c8",
            "Deposit",
            "PS",
            "0x54649df1f31ff62da62676d6d2a7cd820ee7ab75b68feaf921ab53248b59989f_221",
            "200,29795362",
            "",
            "",
            62971L,
            103007339L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVault_3CRV_from88mph() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x71B9eC42bB3CB40F017D8AD8011BE8e384a95fa5",
            11800597,
            LOG_ID,
            "0xf18124581a86d11436684b0124a46efa935a3510",
            "Deposit",
            "3CRV",
            "0xe8b198aff379d80be359ea70c9ea595d5754dbd30dcff8ec96174ef969af7582_335",
            "45027,59778072",
            "",
            "",
            46025L,
            28930799L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultCRV_GUSD() {
        HarvestDTO dto = harvestVaultParseTest(
            "0xB8671E33fcFC7FEA2F7a3Ea4a117F065ec4b009E",
            11768519,
            LOG_ID,
            "0x039223b49d56254cfe7be0242be5b4c5673feb6e",
            "Deposit",
            "CRV_GUSD",
            "0x43ecc869aa5352c1ff53f4cb423c1cfca123edc9abbbda2d4689c8285044f763_121",
            "142309,94517039",
            "",
            "",
            142309L,
            274242L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultCRV_EURS() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x6eb941BD065b8a5bd699C5405A928c1f561e2e5a",
            11686425,
            LOG_ID,
            "0x5ae053134b7866f329f1a0a474936d3d5b9d23cc",
            "Deposit",
            "CRV_EURS",
            "0x73f1a5811f1dd64d224eabf20f011c8f75d9236cf55c5a1519c5fa289920162e_175",
            "19201,10182170",
            "",
            "",
            23709L,
            188026L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultCRV_UST() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x84A1DfAdd698886A614fD70407936816183C0A02",
            11688348,
            LOG_ID,
            "0xe5350e927b904fdb4d2af55c566e269bb3df1941",
            "Deposit",
            "CRV_UST",
            "0x7d9467776c17178f7173c88cc98764e22248fb02e678099570083f1983f46ef8_247",
            "18805,20616001",
            "",
            "",
            18805L,
            44777L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultMAAPL_UST() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x11804D69AcaC6Ae9466798325fA7DE023f63Ab53",
            11688495,
            LOG_ID,
            "0x6aaaf98e8522491bc1006cd8cf95515220144dd3",
            "Deposit",
            "MAAPL_UST",
            "0xdc55d934106a85dfd101cb04d80757baffad3e735fbc940a57e9f92d8383557f_55",
            "87,57535992",
            "",
            "",
            1998L,
            1998L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultMAMZN_UST() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x8334A61012A779169725FcC43ADcff1F581350B7",
            11688255,
            LOG_ID,
            "0x123967b5e51eef1bea412394c51aa6702eddb675",
            "Deposit",
            "MAMZN_UST",
            "0xefaa1f00c817310d6ffea5f22aefe72f084178952940d0bb20de7b9a4d460e39_260",
            "0,53110351",
            "",
            "",
            60L,
            60L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultMGOOGL_UST() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x07DBe6aA35EF70DaD124f4e2b748fFA6C9E1963a",
            11688264,
            LOG_ID,
            "0x252e7e8b9863f81798b1fef8cfd9741a46de653c",
            "Deposit",
            "MGOOGL_UST",
            "0x2ed703289ac97ccc3639021b1900af3eb18bc8c240cf9cd660357c489ca4000b_13",
            "1,16986789",
            "",
            "",
            100L,
            100L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultMTSLA_UST() {
        HarvestDTO dto = harvestVaultParseTest(
            "0xC800982d906671637E23E031e907d2e3487291Bc",
            11688649,
            LOG_ID,
            "0x7ba605bc00ea26512a639d5e0335eaeb3e81ad94",
            "Deposit",
            "MTSLA_UST",
            "0xf8c923e71034431f965212f6d3096d076b87c616306f42dc90971a975c4b85a6_22",
            "0,01000000",
            "",
            "",
            1L,
            5170L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultCRV_STETH() {
        HarvestDTO dto = harvestVaultParseTest(
            "0xc27bfE32E0a934a12681C1b35acf0DBA0e7460Ba",
            11688707,
            LOG_ID,
            "0xfbfd491bc1e89782fb01219c5fd8462c039d0d7e",
            "Deposit",
            "CRV_STETH",
            "0x9b0ee4bf4057e601b9d18f53c5cfe7e633abe1fa3fb0cf5d90161c7a2d53da32_241",
            "9,99521148",
            "",
            "",
            13994L,
            609189L,
            true
        );
        assertNotNull(dto);
        HarvestTvlEntity tvl = harvestDBService.calculateHarvestTvl(dto, false);
        assertNotNull(tvl);
    }

    @Test
    public void parseVaultDAI_BSG2() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x639d4f3F41daA5f4B94d63C2A5f3e18139ba9E54",
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
            "0x639d4f3F41daA5f4B94d63C2A5f3e18139ba9E54",
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
            "0x639d4f3F41daA5f4B94d63C2A5f3e18139ba9E54",
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
            "0x633C4861A4E9522353EDa0bb652878B079fb75Fd",
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
            "0x371E78676cd8547ef969f89D2ee8fA689C50F86B",
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
            "0x45a9e027DdD8486faD6fca647Bb132AD03303EC2",
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
            "0x8Bf3c1c7B1961764Ecb19b4FC4491150ceB1ABB1",
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
            "0x8e53031462e930827a8d482e7d80603b1f86e32d",
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
            "0xd162395c21357b126c5afed6921bc8b13e48d690",
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
            "0x4bf633a09bd593f6fb047db3b4c25ef5b9c5b99e",
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
            "0x859222dd0b249d0ea960f5102dab79b294d6874a",
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
            "0x966A70A4d3719A6De6a94236532A0167d5246c72",
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
            "0x8e298734681adbfC41ee5d17FF8B0d6d803e7098",
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
            "0x145f39B3c6e6a885AA6A8fadE4ca69d64bab69c8",
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
            "0x6F14165c6D529eA3Bfe1814d0998449e9c8D157D",
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
            "0xf8b7235fcfd5a75cfdcc0d7bc813817f3dd17858",
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
            "0x6Bccd7E983E438a56Ba2844883A664Da87E4C43b",
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
            "0x01112a60f427205dcA6E229425306923c3Cc2073",
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
            "0x1a9F22b4C385f78650E7874d64e442839Dc32327",
            11050173,
            LOG_ID,
            "0xc2b27903e0281740994895c32ee40c31dac3197d",
            "Withdraw",
            "UNI_ETH_DAI_V0",
            "0x44ed856bd4de7e4065cb4939b58f4ebc8ec30b564930d4c25b33b81cc03cead6_310",
            "12448.99201236",
            "",
            "",
            0L,
            0L,
            true
        );

        harvestOwnerBalanceCalculator.fillBalance(dto);
        assertAll(
            () -> assertEquals("owner balance", numberFormat("0.00000000"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("0.00000000"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
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
            () -> assertEquals("owner balance", numberFormat("12657,60435602"),
                String.format("%.8f", migration.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("583939,02122719"),
                String.format("%.8f", migration.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVaultUNI_ETH_DAI_V0() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x1a9F22b4C385f78650E7874d64e442839Dc32327",
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
            () -> assertEquals("owner balance", numberFormat("241,08913795"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("11102,67103786"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVaultIDX_ETH_DPI() {
        HarvestDTO dto = harvestVaultParseTest(
            "0x2a32dcbb121d48c106f6d94cf2b4714c0b4dfe48",
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
            () -> assertEquals("owner balance", numberFormat("6,08144172"),
                String.format("%.8f", dto.getOwnerBalance())),
            () -> assertEquals("owner balance usd", numberFormat("3352,40976906"),
                String.format("%.8f", dto.getOwnerBalanceUsd()))
        );
    }

    @Test
    public void parseVaultSUSHI_ETH_USDT() {
        harvestVaultParseTest(
            "0x64035b583c8c694627A199243E863Bb33be60745",
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
            "0x01bd09A1124960d9bE04b638b142Df9DF942b04a",
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
            "0x203E97aa6eB65A1A02d9E80083414058303f241E",
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
            "0x5C0A3F55AAC52AA320Ff5F280E77517cbAF85524",
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
        shouldNotParse("0x203E97aa6eB65A1A02d9E80083414058303f241E", 11278329, LOG_ID);
    }

    @Test
    public void parseVaultPS_V0() {
        harvestVaultParseTest(
            "0x59258F4e15A5fC74A7284055A8094F58108dbD4f",
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
        shouldNotParse("0xc3F7ffb5d5869B3ade9448D094d81B0521e8326f", 11021481, LOG_ID);
    }

    @Test
    public void parseVaultPS2() {
        harvestVaultParseTest(
            "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50",
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
            "0x25550Cccbd68533Fa04bFD3e3AC4D09f9e00Fc50",
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
            "0x8e298734681adbfC41ee5d17FF8B0d6d803e7098",
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
            "0x8e298734681adbfC41ee5d17FF8B0d6d803e7098",
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
            "0xc3F7ffb5d5869B3ade9448D094d81B0521e8326f",
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
            "0xFE09e53A81Fe2808bc493ea64319109B5bAa573e",
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
            "0x01112a60f427205dcA6E229425306923c3Cc2073",
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
            "0x307E2752e8b8a9C29005001Be66B1c012CA9CDB7",
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
            "0x307E2752e8b8a9C29005001Be66B1c012CA9CDB7",
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
            "0x307E2752e8b8a9C29005001Be66B1c012CA9CDB7",
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
            "0x307E2752e8b8a9C29005001Be66B1c012CA9CDB7",
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
            "0x053c80eA73Dc6941F518a68E2FC52Ac45BDE7c9C",
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
            "0x053c80eA73Dc6941F518a68E2FC52Ac45BDE7c9C",
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
            "0x7DDc3ffF0612E75Ea5ddC0d6Bd4e268f70362Cff",
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
            "0x7DDc3ffF0612E75Ea5ddC0d6Bd4e268f70362Cff",
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
            "0xFE09e53A81Fe2808bc493ea64319109B5bAa573e",
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
            "0xA79a083FDD87F73c2f983c5551EC974685D6bb36",
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
            "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE",
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
            "0xf0358e8c3CD5Fa238a29301d0bEa3D63A17bEdBE",
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
            "0x9aA8F427A17d6B0d91B6262989EdC7D45d6aEdf8",
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
            "0xab7FA2B2985BCcfC13c6D86b1D5A17486ab1e04C",
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
            "0x5d9d25c7C457dD82fc8668FFC6B9746b674d4EcB",
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
            "0xC391d1b08c1403313B0c28D47202DFDA015633C4",
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
            "0xF553E1f826f42716cDFe02bde5ee76b2a52fc7EB",
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
            "0xA79a083FDD87F73c2f983c5551EC974685D6bb36",
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
            "0xA79a083FDD87F73c2f983c5551EC974685D6bb36",
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
            "0x9aA8F427A17d6B0d91B6262989EdC7D45d6aEdf8",
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
            "0x7674622c63Bee7F46E86a4A5A18976693D54441b",
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

    @Test
    public void parseVaultUNI_WBTC_KLON() {
        harvestVaultParseTest(
            "0xB4E3fC276532f27Bd0F738928Ce083A3b064ba61",
            11958867,
            LOG_ID,
            "0x750a31fa07184caf87b6cce251d2f0d7928badde",
            "Withdraw",
            "UNI_WBTC_KLON",
            "0x468b3c671758cab8b0e33c1063ab68671d7ca532304fe26556a35b38f0195267_122",
            "0,00016459",
            "0,000164586064304197",
            "0",
            166173L,
            165044L,
            true
        );
    }


    @Test
    public void parseVaultUNI_WBTC_KBTC() {
        harvestVaultParseTest(
            "0x5cd9db40639013a08d797a839c9becd6ec5dcd4d",
            11965381,
            LOG_ID,
            "0xf75fcd9849d3365d436d0cd889ed500150c9482a",
            "Deposit",
            "UNI_WBTC_KBTC",
            "0xe65697bd0a03cf45be5b9955bdd6f19d9590841d3f8edbaefb5ca1f9a49c3302_234",
            "0,00000857",
            "0,000008573748564383",
            "0",
            99233L,
            1208534L,
            true
        );
    }

    @Test
    public void parseVaultMNFLX_UST() {
        harvestVaultParseTest(
            "0x99c2564c9d4767c13e13f38ab073d4758af396ae",
            12000069,
            LOG_ID,
            "0xd98f36c2d99b6b8e610f70baa13df073928536c8",
            "Deposit",
            "MNFLX_UST",
            "0xe657363d9437bb5403eb3a59122e10ba2cf488fe3a59c875ff7fe14c10d7004c_342",
            "1108,86509634",
            "1108,86509634",
            "0",
            50537L,
            452090L,
            true
        );
    }

    @Test
    public void parseVaultMTWTR_UST() {
        harvestVaultParseTest(
            "0xb37c79f954E3e1A4ACCC14A5CCa3E46F226038b7",
            11999645,
            LOG_ID,
            "0xf6f75e3206b4bf2d3d6d12dbf633218894750c39",
            "Deposit",
            "MTWTR_UST",
            "0xe4a88a35ae1c93c8550ca8f89448ba4941e3b3e8eb5500c61a35220bebc5bf0e_76",
            "1587,47546529",
            "1587,47546529",
            "0",
            25965L,
            204142L,
            true
        );
    }

    @Test
    public void parseVaultSUSHI_ETH_UST() {
        harvestVaultParseTest(
            "0x4D4D85c6a1ffE6Bb7a1BEf51c9E2282893feE521",
            11982549,
            LOG_ID,
            "0x814055779f8d2f591277b76c724b7adc74fb82d9",
            "Deposit",
            "SUSHI_ETH_UST",
            "0x56bc16ee94269347678254f82de0a492b0ab960fda021e76bb291924b9e6db81_52",
            "4,95147346",
            "4,95147346",
            "0",
            403L,
            555L,
            true
        );
    }

    @Test
    public void parseVaultCRV_LINK() {
        harvestVaultParseTest(
            "0x24C562E24A4B5D905f16F2391E07213efCFd216E",
            12000189,
            LOG_ID,
            "0xa9c1c504652ab74e5fd22d6f36df53a2be4a4e0b",
            "Withdraw",
            "CRV_LINK",
            "0xf172535612225e2dde3b0918475da5a6569cd3017ecfdaea383c5161a500f285_281",
            "200,89198021",
            "",
            "",
            0L,
            0L,
            true
        );
    }

    @Test
    public void parseVaultMUSE_ETH() {
        harvestVaultParseTest(
            "0xc45d471c77ff31C39474d68a5080Fe1FfACDBC04",
            12057281,
            LOG_ID,
            "0x6bb8bc41e668b7c8ef3850486c9455b5c86830b3",
            "Deposit",
            "MUSE_ETH",
            "0xa87f7874ea7b98fd5b6a99752983a455de237ca2a630bc527a11114a8fcb189f_154",
            "47.22271827",
            "",
            "0",
            82732L,
            189494L,
            true
        );
    }

    @Test
    public void parseVaultDUDES20_ETH() {
        harvestVaultParseTest(
            "0x1E5f4e7127ea3981551D2Bf97dCc8f17a4ECEbEf",
            12056984,
            LOG_ID,
            "0xf28dcdf515e69da11ebd264163b09b1b30dc9dc8",
            "Deposit",
            "DUDES20_ETH",
            "0x7d69e86eadc8f791da68588af3ada72714450868f50d9c86182c74f038c5f775_235",
            "4.89730901",
            "",
            "",
            1776L,
            147638L,
            true
        );
    }

    @Test
    public void parseVaultMASK20_ETH() {
        harvestVaultParseTest(
            "0xF2a671645D0DF54d2f03E9ad7916c8F7368D1C29",
            12053942,
            LOG_ID,
            "0xf28dcdf515e69da11ebd264163b09b1b30dc9dc8",
            "Deposit",
            "MASK20_ETH",
            "0xccf883e70281b3f86286c94b10783da98e575e6b02287e891095efcf46b08454_357",
            "61.23927696",
            "",
            "",
            26914L,
            81484L,
            true
        );
    }

    @Test
    public void parseVaultROPE20_ETH() {
        harvestVaultParseTest(
            "0xAF9486E3DA0cE8d125aF9b256b3ecd104a3031B9",
            12057371,
            LOG_ID,
            "0x42740f20aed483b69701a55ab295a2edc886b1fe",
            "Deposit",
            "ROPE20_ETH",
            "0xecee528512da7abdd884c3c0e7abb096a1838c19c74708e7ba5c782f38584dcf_244",
            "29.04792201",
            "",
            "",
            2289L,
            155348L,
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
        String _amount,
        String _amountIn,
        String sharePrice,
        Long usdAmount,
        Long usdTvl,
        boolean confirmed
    ) {
        String amount = numberFormat(_amount);
        String amountIn = numberFormat(_amountIn);
        List<LogResult> logResults = web3Service
            .fetchContractLogs(singletonList(fromVault), onBlock, onBlock);
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
        String _amount,
        String amountIn,
        String sharePrice,
        Long usdAmount,
        Long usdTvl,
        boolean confirmed) {
        String amount = numberFormat(_amount);
        assertNotNull(dto, "Dto is null");
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
                String.format("%.0f", dto.getLastUsdTvl()))
        );
    }
}
