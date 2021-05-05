package pro.belbix.ethparser.web3.deployer.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.models.LpContract;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;
import pro.belbix.ethparser.web3.contracts.models.TokenContract;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class DeployerEventToContractTransformerTest {

  @Autowired
  private DeployerEventToContractTransformer deployerEventToContractTransformer;

  @Test
  public void testCreateVault_SUSHI_MIC_USDT() {
    String address = "0x6f14165c6d529ea3bfe1814d0998449e9c8d157d";
    long block = 11608458;
    String network = ETH_NETWORK;

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(5, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(4);
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

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(3, contracts.size());
    TokenContract token = (TokenContract) contracts.get(0);
    LpContract lp = (LpContract) contracts.get(1);
    SimpleContract vault = (SimpleContract) contracts.get(2);
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
        () -> assertEquals(block, (long) token.getLps().get(lp.getAddress()), "token lp"),
        () -> assertEquals(network, token.getNetwork(), "token network"),
        () -> assertEquals(ContractType.TOKEN, token.getContractType(), "token contract type"),

        () -> assertEquals("UNI_LP_USDC_WETH", lp.getName(), "lp name"),
        () -> assertEquals("0xb4e16d0168e52d35cacd2c6185b44281ec28c9dc",
            lp.getAddress(), "lp address"),
        () -> assertEquals(block, lp.getCreatedOnBlock(), "lp created"),
        () -> assertEquals(network, lp.getNetwork(), "lp network"),
        () -> assertEquals(ContractType.UNI_PAIR, lp.getContractType(), "lp contract type")
    );
  }

  @Test
  public void testCreateVault_CRV_TBTC() {
    String address = "0x640704d106e79e105fda424f05467f005418f1b5";
    long block = 11230946;
    String network = ETH_NETWORK;

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(4, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(3);
    TokenContract token = (TokenContract) contracts.get(0);
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

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(3, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(2);
    TokenContract token = (TokenContract) contracts.get(0);
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

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(3, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(2);
    TokenContract token = (TokenContract) contracts.get(0);
    LpContract lp = (LpContract) contracts.get(1);
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
  public void testCreateVault_ST_MASK20_ETH() {
    String address = "0xc5fc56779b5925218d2cdac093d0bfc6de7cc2d1";
    long block = 12044260;
    String network = ETH_NETWORK;

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(5, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(4);
    TokenContract token0 = (TokenContract) contracts.get(0);
    LpContract lp0 = (LpContract) contracts.get(1);
    TokenContract token1 = (TokenContract) contracts.get(2);
    LpContract lp1 = (LpContract) contracts.get(3);
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
        () -> assertEquals(block, token1.getCreatedOnBlock(), "token1 created"),
        () -> assertEquals(block, (long) token1.getLps().get(lp1.getAddress()), "token1 lp1"),

        () -> assertEquals("UNI_LP_WETH_MASK20", lp1.getName(), "lp1 name"),
        () -> assertEquals("0xaa617c8726adfde9e7b08746457e6b90ddb21480",
            lp1.getAddress(), "lp1 address"),
        () -> assertEquals(block, token1.getCreatedOnBlock(), "lp1 created")
    );
  }

  @Test
  public void testCreateVault_PS_V0() {
    String address = "0xae024f29c26d6f71ec71658b1980189956b0546d";
    long block = 10770203;
    String network = ETH_NETWORK;

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(2, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(1);
    assertAll(
        () -> assertEquals("ST_FARM", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_BPT() {
    String address = "0x158edb94d0bfc093952fb3009deeed613042907c";
    long block = 10816127;
    String network = ETH_NETWORK;

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(5, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(4);
    assertAll(
        () -> assertEquals("ST_BPT_YFV_FARM", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_ST_UNI_DAI_fDAI() {
    String address = "0xb492faeda6c9ffb9b9854a58f28d5333ff7a11bc";
    long block = 10817095;
    String network = ETH_NETWORK;

    DeployerDTO dto = createDeployerDto(address, block, network);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(5, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(4);
    assertAll(
        () -> assertEquals("ST_UNI_DAI_fDAI", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  private DeployerDTO createDeployerDto(String toAddress, long block, String network) {
    return DeployerDTO.builder()
        .type(CONTRACT_CREATION.name())
        .toAddress(toAddress)
        .network(network)
        .block(block)
        .build();
  }

}
