package pro.belbix.ethparser.web3.deployer.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

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
  public void testCreateVPool_ST_UNI_WBTC_KBTC() {
    String address = "0xdD496A6Ba1B4Cf2b3ef42dEf132e2B2c570941FE".toLowerCase();
    long block = 11924852;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x34358cd715cb9efc0d98bff92124bcb27a5c5e3780bafcbf0ecc750a2b6ee75e", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(8, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("ST_UNI_WBTC_KBTC", vault.getName(), "name"),
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
        () -> assertEquals("PS", vault.getName(), "name"),
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
    assertEquals(1, contracts.size());
  }

  @Test
  public void testCreateVault_1INCH_1INCH_renBTC() {
    String network = BSC_NETWORK;

    DeployerDTO dto = loadDto(
        "0xa6898e6141552ca526d7de7e43ab59158c58e0326f5b4eee6a3b0c352f216404", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(9, contracts.size());
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
    assertEquals(9, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("SUSHI_MIC_USDT", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),
        () -> assertEquals(network, vault.getNetwork(), "vault network"),
        () -> assertEquals(ContractType.VAULT, vault.getContractType(), "contract type")
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
        () -> assertEquals("USDC", vault.getName(), "vault name"),
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
    assertEquals(9, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token = (TokenContract) contracts.get(1);
    assertAll(
        () -> assertEquals("CRV_TBTC_SBTC", vault.getName(), "name"),
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
    assertEquals(9, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token = (TokenContract) contracts.get(8);
    assertAll(
        () -> assertEquals("CRV_YDAI_YUSDC_YUSDT_YTUSD", vault.getName(), "name"),
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
    assertEquals(8, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token = (TokenContract) contracts.get(2);
    LpContract lp = (LpContract) contracts.get(3);
    assertAll(
        () -> assertEquals("1INCH_ETH_DAI", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),

    () -> assertEquals("DAI", token.getName(), "token name"),
        () -> assertEquals("0x6b175474e89094c44da98b954eedeac495271d0f",
            token.getAddress(), "token address"),
        () -> assertEquals(block, token.getCreatedOnBlock(), "token created"),
        () -> assertEquals(block, (long) token.getLps().get(lp.getAddress()), "token lp"),

        () -> assertEquals("UNI_LP_DAI_WETH", lp.getName(), "lp name"),
        () -> assertEquals("0xa478c2975ab1ea89e8196811f51a7b7ade33eb11",
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
    assertEquals(7, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    TokenContract token0 = (TokenContract) contracts.get(2);
    LpContract lp0 = (LpContract) contracts.get(3);
    TokenContract token1 = (TokenContract) contracts.get(6);
    assertAll(
        () -> assertEquals("ST_UNI_WETH_MASK20", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created"),

    () -> assertEquals("WETH", token0.getName(), "token0 name"),
        () -> assertEquals("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
            token0.getAddress(), "token0 address"),
        () -> assertEquals(block, token0.getCreatedOnBlock(), "token0 created"),
        () -> assertEquals(block, (long) token0.getLps().get(lp0.getAddress()), "token0 lp"),

        () -> assertEquals("SUSHI_LP_WBTC_WETH", lp0.getName(), "lp0 name"),
        () -> assertEquals("0xceff51756c56ceffca006cd410b03ffc46dd3a58",
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
    assertEquals(2, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("ST_PS", vault.getName(), "name"),
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
    assertEquals(9, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("ST_BPT_YFV_FARM", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreatePool_ST_UNI_DAI_fDAI() {
    String address = "0xb492faeda6c9ffb9b9854a58f28d5333ff7a11bc";
    long block = 10817095;
    String network = ETH_NETWORK;

    DeployerDTO dto = loadDto(
        "0x83d7ba92ea38827c38405cd13f490de8e04fc3962334640d8ca0e2c6147f103d", network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(8, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("ST_UNI_DAI_fDAI", vault.getName(), "name"),
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
