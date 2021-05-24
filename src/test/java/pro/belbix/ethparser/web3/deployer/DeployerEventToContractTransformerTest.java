package pro.belbix.ethparser.web3.deployer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.db.ContractDbService;
import pro.belbix.ethparser.web3.contracts.models.LpContract;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;
import pro.belbix.ethparser.web3.contracts.models.TokenContract;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;
import pro.belbix.ethparser.web3.deployer.transform.DeployerEventToContractTransformer;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class DeployerEventToContractTransformerTest {

  @Autowired
  private DeployerEventToContractTransformer deployerEventToContractTransformer;

  @Autowired
  private Web3Functions web3Functions;

  @Autowired
  private DeployerTransactionsParser deployerTransactionsParser;

  @MockBean
  private ContractDbService contractDbService;

  @BeforeEach
  void setUp() {
    when(contractDbService.findLpByAddress(any(), any())).thenReturn(Optional.empty());
    when(contractDbService.getContractByAddress(any(), any())).thenReturn(Optional.empty());
  }

  @Test
  public void testCreateStrategy_CRV_GUSD() {
    String address = "0xa505917c1326670451eff9ea75fe0d49a3853acf".toLowerCase();
    long block = 11745403;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0xd9ac3dd9636a4ccd8570f8eef872e4bd5e97b2ae916b96b873d2918fd087311e", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(9, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("S_CRV_gusd3CRV", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.STRATEGY, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreatePool_FARM_WETH() {
    String address = "0x6555c79a8829b793f332f1535b0efb1fe4c11958".toLowerCase();
    long block = 11407437;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x420529d8a5cb26a83992073bfb43cbb728a249fc6c28287801fb255faeab023b", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(6, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    LpContract lpUnderlying = (LpContract) contracts.get(3);
    TokenContract token = (TokenContract) contracts.get(2);
    assertAll(
        () -> assertEquals("P_UNI_FARM_WETH", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.POOL, vault.getContractType(), "contract type"),

        () -> assertEquals("UNI_LP_FARM_USDC", lpUnderlying.getName(), "lp name"),
        () -> assertEquals("0x514906fc121c7878424a5c928cad1852cc545892", lpUnderlying.getAddress(),
            "lp address"),
        () -> assertEquals(block, lpUnderlying.getCreatedOnBlock(), "lp created"),
        () -> assertEquals(network, lpUnderlying.getNetwork(), "lp network"),
        () -> assertEquals(ContractType.UNI_PAIR, lpUnderlying.getContractType(),
            "lp contract type"),

        () -> assertEquals("FARM", token.getName(), "token name"),
        () -> assertEquals("0xa0246c9032bc3a600820415ae600c6388619a14d",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created"),
        () -> assertEquals(network, token.getNetwork(), "token network"),
        () -> assertEquals(Integer.valueOf((int) block),
            token.getLps().get(lpUnderlying.getAddress()), "token lp"),
        () -> assertEquals(ContractType.TOKEN, token.getContractType(), "token contract type")
    );
  }

  @Test
  public void testCreateVault_FARM_GRAIN() {
    String address = "0xe58f0d2956628921cded2ea6b195fc821c3a2b16".toLowerCase();
    long block = 11407202;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x686c0d0293a1b0084cd9dcb46256a4557db65e829107ff5562727017b2b035b0", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(5, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    LpContract lpUnderlying = (LpContract) contracts.get(1);
    TokenContract token = (TokenContract) contracts.get(2);
    assertAll(
        () -> assertEquals("P_UNI_GRAIN_FARM", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.POOL, vault.getContractType(), "contract type"),

        () -> assertEquals("UNI_LP_GRAIN_FARM", lpUnderlying.getName(), "lp name"),
        () -> assertEquals("0xb9fa44b0911f6d777faab2fa9d8ef103f25ddf49", lpUnderlying.getAddress(),
            "lp address"),
        () -> assertEquals(block, lpUnderlying.getCreatedOnBlock(), "lp created"),
        () -> assertEquals(network, lpUnderlying.getNetwork(), "lp network"),
        () -> assertEquals(ContractType.UNI_PAIR, lpUnderlying.getContractType(),
            "lp contract type"),

        () -> assertEquals("GRAIN", token.getName(), "token name"),
        () -> assertEquals("0x6589fe1271a0f29346796c6baf0cdf619e25e58e",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created"),
        () -> assertEquals(network, token.getNetwork(), "token network"),
        () -> assertEquals(Integer.valueOf((int) block),
            token.getLps().get(lpUnderlying.getAddress()), "token lp"),
        () -> assertEquals(ContractType.TOKEN, token.getContractType(), "token contract type")
    );
  }

  @Test
  public void testCreateVault_DAI_BSG() {
    String address = "0x21e22315bcfcba1c02fc40903bf02b3bd78c6e13".toLowerCase();
    long block = 11655635;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0xc1b8b0e44fe0e6ae3b72eb272ce998f4e50556e11bacfedab2a1ae502d1840cd", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(5, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("V_UNI_DAI_BSG", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreateVault_CRV_GUSD() {
    String address = "0xB8671E33fcFC7FEA2F7a3Ea4a117F065ec4b009E".toLowerCase();
    long block = 11745396;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x30a47c665311609e5932fada7a69276dc62d1925c21546e0725e3b25cf6dea8f", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("V_CRV_gusd3CRV", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreateVault_UNI_UST_mTWTR() {
    String address = "0xb37c79f954e3e1a4accc14a5cca3e46f226038b7".toLowerCase();
    long block = 11954001;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0xd468ccf42efe49b8f07aab68efda8a166de4cff60a66100cffb89ae623b91489", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(5, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    LpContract lpUnderlying = (LpContract) contracts.get(1);
    TokenContract token = (TokenContract) contracts.get(4);
    assertAll(
        () -> assertEquals("V_UNI_UST_mTWTR", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type"),

        () -> assertEquals("UNI_LP_UST_mTWTR", lpUnderlying.getName(), "lp name"),
        () -> assertEquals("0x34856be886a2dba5f7c38c4df7fd86869ab08040", lpUnderlying.getAddress(),
            "lp address"),
        () -> assertEquals(block, lpUnderlying.getCreatedOnBlock(), "lp created"),
        () -> assertEquals(network, lpUnderlying.getNetwork(), "lp network"),
        () -> assertEquals(ContractType.UNI_PAIR, lpUnderlying.getContractType(),
            "lp contract type"),

        () -> assertEquals("mTWTR", token.getName(), "token name"),
        () -> assertEquals("0xedb0414627e6f1e3f082de65cd4f9c693d78cca9",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created"),
        () -> assertEquals(network, token.getNetwork(), "token network"),
        () -> assertEquals(Integer.valueOf((int) block),
            token.getLps().get(lpUnderlying.getAddress()), "token lp"),
        () -> assertEquals(ContractType.TOKEN, token.getContractType(), "token contract type")
    );
  }

  @Test
  public void testCreateVault_EPS_fusdt3EPS() {
    String address = "0xe64bfe13aa99335487f1f42a56cddbffaec83bbf".toLowerCase();
    long block = 6736265;
    String network = BSC_NETWORK;

    DeployerDTO dto = loadDto(
        "0xa38e66c19905467492b52ab759b1e08467ad35f1d9f9f8df5a7008869c936453", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("V_EPS_fusdt3EPS", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreateVault_Belt_bDAI_bUSDC_bUSDT_bBUSD() {
    String address = "0x2427da81376a0c0a0c654089a951887242d67c92".toLowerCase();
    long block = 6619994;
    String network = BSC_NETWORK;

    DeployerDTO dto = loadDto(
        "0x2fd827c9756e354e7ad9dd60144f4e35823873111e74467f1fd61773e19f20da", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("V_BELT_bDAI_bUSDC_bUSDT_bBUSD", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreateVault_PC_BUSD_BNB() {
    String address = "0xF7A3a95d0f7E8A5EEaE483Cdd7b76aF287283D34".toLowerCase();
    long block = 5992263;
    String network = BSC_NETWORK;

    DeployerDTO dto = loadDto(
        "0x13626d5ec4bc925782f2d9ac6b3965b25a978e5d9605de796afd513284a5f98a", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("V_PCS_WBNB_BUSD", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreateVPool_ST_UNI_WBTC_KBTC() {
    String address = "0xdD496A6Ba1B4Cf2b3ef42dEf132e2B2c570941FE".toLowerCase();
    long block = 11924852;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x34358cd715cb9efc0d98bff92124bcb27a5c5e3780bafcbf0ecc750a2b6ee75e", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(7, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("P_UNI_WBTC_KBTC", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.POOL, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreateVault_PS() {
    String address = "0x25550cccbd68533fa04bfd3e3ac4d09f9e00fc50";
    long block = 10957909;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x6611e4fd6174077ab3468d66c12588ba0ee3f588a0dfbab551f30bd385da3eb4", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("V_PS", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type")
    );
  }

  @Test
  public void testNotCreateVault_Migration() {
    DeployerDTO dto = loadDto(
        "0xa9ffa219fb8276b9828ad7b0852f9df739c5503b98625189724b91cd9b408e3d"
        , BSC_NETWORK);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(0, contracts.size());
  }

  @Test
  public void testCreateVault_ICE_notInit() {
    String address = "0x5da237ad194b8bbb008ac8916df99a92a8a7c8eb";
    long block = 6736315;
    String network = BSC_NETWORK;

    DeployerDTO dto = loadDto(
        "0x36b37c88e0951435300a1fcd66232d5f7994dd36a292b39e98f939270b2d47db", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(0, contracts.size());
  }

  @Test
  public void testCreateVault_1INCH_1INCH_renBTC() {
    String network = BSC_NETWORK;

    DeployerDTO dto = loadDto(
        "0xa6898e6141552ca526d7de7e43ab59158c58e0326f5b4eee6a3b0c352f216404", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(8, contracts.size());
  }

  @Test
  public void testCreateVault_SUSHI_MIC_USDT() {
    String address = "0x6f14165c6d529ea3bfe1814d0998449e9c8d157d";
    long block = 11608458;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0xb622208e5955f596b7be1aa8aee6e8512c5611185b70e82e3207e9bb04a18afb", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    LpContract lpUnderlying = (LpContract) contracts.get(1);
    assertAll(
        () -> assertEquals("V_SUSHI_MIC_USDT", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type"),

        () -> assertEquals("SUSHI_LP_MIC_USDT", lpUnderlying.getName(), "name"),
        () -> assertEquals("0xc9cb53b48a2f3a9e75982685644c1870f1405ccb", lpUnderlying.getAddress(), "address"),
        () -> assertEquals(block, lpUnderlying.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, lpUnderlying.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.UNI_PAIR, lpUnderlying.getContractType(), "contract type")
    );
  }

  @Test
  public void testCreateVault_USDC() {
    String address = "0xf0358e8c3cd5fa238a29301d0bea3d63a17bedbe";
    long block = 11086843;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x579021dd520b36f37d28d8351e905ecddd64ec5460579cf0bfca99ca4295ea0e", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(2, contracts.size());
    TokenContract token = (TokenContract) contracts.get(1);
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("V_USDC", vault.getName(), "vault name"),
        () -> assertEquals(address, vault.getAddress(), "vault address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "vault created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type"),

        () -> assertEquals("USDC", token.getName(), "token name"),
        () -> assertEquals("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created"),
        () -> assertEquals(network, token.getNetwork(), "token network"),
        () -> assertEquals(ContractType.TOKEN, token.getContractType(), "token contract type")
    );
  }

  @Test
  public void testCreateVault_CRV_TBTC() {
    String address = "0x640704d106e79e105fda424f05467f005418f1b5";
    long block = 11230946;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0xc53d4bc13f2cee03897f4f385b1cee0a64d346f608f879fd13b7f411538c2062", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(6, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token = (TokenContract) contracts.get(1);
    assertAll(
        () -> assertEquals("V_CRV_tbtc_sbtc", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),

        () -> assertEquals("TBTC", token.getName(), "token name"),
        () -> assertEquals("0x8daebade922df735c38c80c7ebd708af50815faa",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created")
    );
  }

  @Test
  public void testCreateVault_YCRV() {
    String address = "0x0fe4283e0216f94f5f9750a7a11ac54d3c9c38f3";
    long block = 11152259;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x9761c711b51fc089959dfb58be0dc425ece064c9124b8c4d1065ed59ca6ef749", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token = (TokenContract) contracts.get(3);
    assertAll(
        () -> assertEquals("V_CRV_yDAI_yUSDC_yUSDT_yTUSD", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),

        () -> assertEquals("yDAI+yUSDC+yUSDT+yTUSD", token.getName(), "token name"),
        () -> assertEquals("0xdf5e0e81dff6faf3a7e52ba697820c5e32d806a8",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created")
    );
  }

  @Test
  public void testCreateVault_1INCH_ETH_DAI() {
    String address = "0x8e53031462e930827a8d482e7d80603b1f86e32d";
    long block = 11647788;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x44167a64ad68ff1ec9a7441505b511d9f1c90274fcd31683bf28d4afcd55978b", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token = (TokenContract) contracts.get(2);
    LpContract lp = (LpContract) contracts.get(3);
    assertAll(
        () -> assertEquals("V_1INCH_ETH_DAI", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),

        () -> assertEquals("DAI", token.getName(), "token name"),
        () -> assertEquals("0x6b175474e89094c44da98b954eedeac495271d0f",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created"),
        () -> assertEquals(block, (long) token.getLps().get(lp.getAddress()), "token lp"),

        () -> assertEquals("UNI_LP_DAI_USDC", lp.getName(), "lp name"),
        () -> assertEquals("0xae461ca67b15dc8dc81ce7615e0320da1a9ab8d5",
            lp.getAddress(), "lp address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "lp created")
    );
  }

  @Test
  public void testCreatePool_ST_MASK20_ETH() {
    String address = "0xc5fc56779b5925218d2cdac093d0bfc6de7cc2d1";
    long block = 12044260;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0xc898948df88e70d8af818157770fdf89fcdefc9ecab9887512136e0b6fd01b63", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(8, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token0 = (TokenContract) contracts.get(2);
    LpContract lp0 = (LpContract) contracts.get(3);
    TokenContract token1 = (TokenContract) contracts.get(5);
    assertAll(
        () -> assertEquals("P_UNI_WETH_MASK20", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),

        () -> assertEquals("WETH", token0.getName(), "token0 name"),
        () -> assertEquals("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
            token0.getAddress(), "token0 address"),
        () -> assertEquals(block, token0.getCreatedOnBlock(), "token0 created"),
        () -> assertEquals(block, (long) token0.getLps().get(lp0.getAddress()), "token0 lp"),

        () -> assertEquals("SUSHI_LP_USDC_WETH", lp0.getName(), "lp0 name"),
        () -> assertEquals("0x397ff1542f962076d0bfe58ea045ffa2d347aca0",
            lp0.getAddress(), "lp address"),
        () -> assertEquals(block, token0.getCreatedOnBlock(), "lp0 created"),

        () -> assertEquals("MASK20", token1.getName(), "token1 name"),
        () -> assertEquals("0xc2bde1a2fa26890c8e6acb10c91cc6d9c11f4a73",
            token1.getAddress(), "token1 address"),
        () -> assertEquals(block, token1.getCreatedOnBlock(), "token1 created")
    );
  }

  @Test
  public void testCreatePool_PS_V0() {
    String address = "0xae024f29c26d6f71ec71658b1980189956b0546d";
    long block = 10770203;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x25e9331254c2b262e5fc8dd14686d02d9c7ff9550503a53aeb1bde15a02a4ca1", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("P_PS", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreatePool_BPT() {
    String address = "0x158edb94d0bfc093952fb3009deeed613042907c";
    long block = 10816127;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0xab4019b4730cc7af860daa2077b8326a744a522acdbed8c4a4eaf42ee3df213e", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(6, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("P_BPT_YFV_FARM", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreatePool_P_UNI_DAI_fDAI() {
    String address = "0xb492faeda6c9ffb9b9854a58f28d5333ff7a11bc";
    long block = 10817095;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x83d7ba92ea38827c38405cd13f490de8e04fc3962334640d8ca0e2c6147f103d", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(7, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("P_UNI_DAI_fDAI", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  private DeployerDTO loadDto(String hash, String network) {
    Transaction tx = web3Functions.findTransaction(hash, network);
    Assertions.assertNotNull(tx);
    DeployerDTO dto = deployerTransactionsParser.parse(tx, network);
    Assertions.assertNotNull(dto);
    return dto;
  }

}
