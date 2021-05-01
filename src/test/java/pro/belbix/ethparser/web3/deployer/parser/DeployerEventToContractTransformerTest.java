package pro.belbix.ethparser.web3.deployer.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum.CONTRACT_CREATION;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.contracts.ContractType;
import pro.belbix.ethparser.web3.contracts.models.PureEthContractInfo;
import pro.belbix.ethparser.web3.contracts.models.SimpleContract;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class DeployerEventToContractTransformerTest {

  @Autowired
  private DeployerEventToContractTransformer deployerEventToContractTransformer;

  @Test
  public void testCreateVault_SUSHI_MIC_USDT() {
    String address = "0x6f14165c6d529ea3bfe1814d0998449e9c8d157d";
    long block = 11608458;

    DeployerDTO dto = createDeployerDto(address, block);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("SUSHI_MIC_USDT", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_USDC() {
    String address = "0xf0358e8c3cd5fa238a29301d0bea3d63a17bedbe";
    long block = 11086843;

    DeployerDTO dto = createDeployerDto(address, block);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("USDC", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_CRV_TBTC() {
    String address = "0x640704d106e79e105fda424f05467f005418f1b5";
    long block = 11230946;

    DeployerDTO dto = createDeployerDto(address, block);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("CRV_TBTC_SBTC", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_YCRV() {
    String address = "0x0fe4283e0216f94f5f9750a7a11ac54d3c9c38f3";
    long block = 11152259;

    DeployerDTO dto = createDeployerDto(address, block);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("CRV_YDAI_YUSDC_YUSDT_YTUSD", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_ONEINCH_ETH_USDC() {
    String address = "0x8e53031462e930827a8d482e7d80603b1f86e32d";
    long block = 11647788;

    DeployerDTO dto = createDeployerDto(address, block);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("1INCH_ETH_DAI", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_ST_MASK20_ETH() {
    String address = "0xc5fc56779b5925218d2cdac093d0bfc6de7cc2d1";
    long block = 12044260;

    DeployerDTO dto = createDeployerDto(address, block);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("ST_UNI_WETH_MASK20", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  @Test
  public void testCreateVault_PS_V0() {
    String address = "0xae024f29c26d6f71ec71658b1980189956b0546d";
    long block = 10770203;

    DeployerDTO dto = createDeployerDto(address, block);
    List<PureEthContractInfo> contracts =
        deployerEventToContractTransformer.transform(dto);
    assertEquals(1, contracts.size());
    SimpleContract vault = (SimpleContract) contracts.get(0);
    assertAll(
        () -> assertEquals("ST_FARM", vault.getName(), "name"),
        () -> assertEquals(address, vault.getAddress(), "address"),
        () -> assertEquals(block, vault.getCreatedOnBlock(), "created")
    );
  }

  private DeployerDTO createDeployerDto(String toAddress, long block) {
    return DeployerDTO.builder()
        .type(CONTRACT_CREATION.name())
        .toAddress(toAddress)
        .network(ETH_NETWORK)
        .block(block)
        .build();
  }

}
