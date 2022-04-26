package pro.belbix.ethparser.web3.deployer;

import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;
import static pro.belbix.ethparser.web3.contracts.ContractConstantsV7.DEPLOYERS;
import static pro.belbix.ethparser.web3.contracts.ContractConstantsV7.FARM_TOKEN;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class DeployerTransactionsParserTest {

  @Autowired
  private Web3Functions web3Functions;
  @Autowired
  private DeployerTransactionsParser parser;

  @Test
  public void testParseDeployerTransaction_SetFeeTx() throws Exception {
    DeployerDTO dto = loadDto("0x648a866acbd927613f4fc3c1d510415074da8a4d796b43a34da3f981b89229c3");
    assertModel(
        DeployerDTO.builder()
            .id("0x648a866acbd927613f4fc3c1d510415074da8a4d796b43a34da3f981b89229c3")
            .idx(75)
            .block(11792110)
            .blockDate(1612472244)
            .network("eth")
            .toAddress("0x222412af183bceadefd72e4cb1b71f1889953b1c")
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(0.0)
            .gasLimit(31221)
            .gasUsed(31221)
            .gasPrice(333)
            .methodName(DeployerActivityEnum.SET_FEE_REWARD_FORWARDER.getMethodName())
            .type(DeployerActivityEnum.SET_FEE_REWARD_FORWARDER.name())
            .confirmed(1)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_SetRewardDistTx() throws Exception {
    DeployerDTO dto = loadDto("0xe082536c0360c4c4a3e119d0b021b242891403096cf67bbdb89555e33facc7cf");
    assertModel(
        DeployerDTO.builder()
            .id("0xe082536c0360c4c4a3e119d0b021b242891403096cf67bbdb89555e33facc7cf")
            .idx(114)
            .block(11792109)
            .blockDate(1612472192)
            .network("eth")
            .toAddress("0x8f5adc58b32d4e5ca02eac0e293d35855999436c")
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(0.0)
            .gasLimit(28496)
            .gasUsed(28496)
            .gasPrice(333)
            .methodName(DeployerActivityEnum.SET_REWARD_DISTRIBUTION.getMethodName())
            .type(DeployerActivityEnum.SET_REWARD_DISTRIBUTION.name())
            .confirmed(1)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_HardWorkTx() throws Exception {
    DeployerDTO dto = loadDto("0xef92345cbdd7d25e1d0d2f8050c9d672b9cf3f7aa8e42ecffac602245776e758");
    assertModel(
        DeployerDTO.builder()
            .id("0xef92345cbdd7d25e1d0d2f8050c9d672b9cf3f7aa8e42ecffac602245776e758")
            .idx(17)
            .block(11808046)
            .blockDate(1612684117)
            .network("eth")
            .toAddress("0x859222dd0b249d0ea960f5102dab79b294d6874a")
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(0.0)
            .gasLimit(900000)
            .gasUsed(432845)
            .gasPrice(138)
            .methodName(DeployerActivityEnum.DO_HARD_WORK.getMethodName())
            .type(DeployerActivityEnum.DO_HARD_WORK.name())
            .confirmed(1)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_SetPathTx() throws Exception {
    DeployerDTO dto = loadDto("0xa21735d140f7877a3e72ea62ef6a4725d5e6ae742a9c04003b7a9f7ebd874334");
    assertModel(
        DeployerDTO.builder()
            .id("0xa21735d140f7877a3e72ea62ef6a4725d5e6ae742a9c04003b7a9f7ebd874334")
            .idx(114)
            .block(11815913)
            .blockDate(1612788944)
            .network("eth")
            .toAddress("0x7882172921e99d590e097cd600554339fbdbc480")
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(0.)
            .gasLimit(111156)
            .gasUsed(111156)
            .gasPrice(245)
            .methodName(DeployerActivityEnum.SET_PATH.getMethodName())
            .type(DeployerActivityEnum.SET_PATH.name())
            .confirmed(1)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_FailedTx() throws Exception {
    DeployerDTO dto = loadDto("0xc2a5a2705451d33f2817a7472bdf963e8169c4bc796059b58bd46a3b4fd250e6");
    assertModel(
        DeployerDTO.builder()
            .id("0xc2a5a2705451d33f2817a7472bdf963e8169c4bc796059b58bd46a3b4fd250e6")
            .idx(8)
            .block(11808016)
            .blockDate(1612683659)
            .network("eth")
            .toAddress("0x8e53031462e930827a8d482e7d80603b1f86e32d")
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(0.)
            .gasLimit(472179)
            .gasUsed(456817)
            .gasPrice(138)
            .methodName(DeployerActivityEnum.DO_HARD_WORK.getMethodName())
            .type(DeployerActivityEnum.DO_HARD_WORK.name())
            .confirmed(0)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_UnknownTx() throws Exception {
    DeployerDTO dto = loadDto("0xabd90485e1c558a25b1f8a7f04f338bc5d32151aaa72a2468b739dcf5442d07e");
    assertModel(
        DeployerDTO.builder()
            .id("0xabd90485e1c558a25b1f8a7f04f338bc5d32151aaa72a2468b739dcf5442d07e")
            .idx(4)
            .block(10784945)
            .blockDate(1599091186)
            .network("eth")
            .toAddress("0xf1499aae3f8f9cf925b663568a964385898b53c2")
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(0.)
            .gasLimit(12000000)
            .gasUsed(1471726)
            .gasPrice(351)
            .methodName("0xe33a87c7")
            .type(DeployerActivityEnum.UNKNOWN.name())
            .confirmed(1)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_ContractCreationTx() throws Exception {
    DeployerDTO dto = loadDto("0xc4000221276b1e7a97cb2f64f66e9df5e68794915379611c549805b831365f8b");
    assertModel(
        DeployerDTO.builder()
            .id("0xc4000221276b1e7a97cb2f64f66e9df5e68794915379611c549805b831365f8b")
            .idx(93)
            .block(10770079)
            .blockDate(1598895285)
            .network("eth")
            .toAddress(FARM_TOKEN)
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(0.0)
            .gasLimit(4721975)
            .gasUsed(1238992)
            .gasPrice(310)
            .methodName(DeployerActivityEnum.CONTRACT_CREATION.getMethodName())
            .type(DeployerActivityEnum.CONTRACT_CREATION.name())
            .confirmed(1)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_NoInputDataTx() throws Exception {
    DeployerDTO dto = loadDto("0x5e654d894b1b37eb98a19b724182ee028d541d0b87033de0ca97bcb5bb6f597d");
    assertModel(
        DeployerDTO.builder()
            .id("0x5e654d894b1b37eb98a19b724182ee028d541d0b87033de0ca97bcb5bb6f597d")
            .idx(5)
            .block(10977107)
            .blockDate(1601648372)
            .network("eth")
            .toAddress("0xbed04c43e74150794f2ff5b62b4f73820edaf661")
            .fromAddress(DEPLOYERS.get(ETH_NETWORK))
            .value(17.0)
            .gasLimit(21000)
            .gasUsed(21000)
            .gasPrice(151)
            .methodName(DeployerActivityEnum.NO_INPUT_DATA.getMethodName())
            .type(DeployerActivityEnum.NO_INPUT_DATA.name())
            .confirmed(1)
            .name(null)
            .build(),
        dto);
  }

  @Test
  public void testParseDeployerTransaction_NoInputDataTx_CheckAllFields() throws Exception {
    DeployerDTO dto = loadDto("0xcbbf816c41ad31cd68dacc873ec2f1a025bb80864cab3d2a846398a77b294ddc");
    assertModel(
       DeployerDTO.builder()
        .id("0xcbbf816c41ad31cd68dacc873ec2f1a025bb80864cab3d2a846398a77b294ddc")
        .idx(121)
        .block(11804364)
        .blockDate(1612636157)
        .network("eth")
        .toAddress("0x7ba605bc00ea26512a639d5e0335eaeb3e81ad94")
        .fromAddress(DEPLOYERS.get(ETH_NETWORK))
        .value(3.5)
        .gasLimit(21000)
        .gasUsed(21000)
        .gasPrice(220)
        .methodName(DeployerActivityEnum.NO_INPUT_DATA.getMethodName())
        .type(DeployerActivityEnum.NO_INPUT_DATA.name())
        .confirmed(1)
        .name(null)
       .build(),
      dto);
  }

  private DeployerDTO loadDto(String hash) {
    Transaction tx = web3Functions.findTransaction(hash, ETH_NETWORK);
    Assertions.assertNotNull(tx);
    DeployerDTO dto = parser.parse(tx, ETH_NETWORK);
    Assertions.assertNotNull(dto);
    return dto;
  }
}
