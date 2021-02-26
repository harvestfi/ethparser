package pro.belbix.ethparser.web3.deployer;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.DeployerDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.contracts.ContractConstants;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DeployerTransactionsParserTest {
  @Autowired private Web3Service web3Service;
  @Autowired private DeployerTransactionsParser parser;

  @Test
  public void testParseDeployerTransaction_SetFeeTx() {
    String hash = "0x648a866acbd927613f4fc3c1d510415074da8a4d796b43a34da3f981b89229c3";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, tx.getHash());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(
        DeployerActivityEnum.SET_FEE_REWARD_FORWARDER.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.SET_FEE_REWARD_FORWARDER.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_SetRewardDistTx() {
    String hash = "0xe082536c0360c4c4a3e119d0b021b242891403096cf67bbdb89555e33facc7cf";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, tx.getHash());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(DeployerActivityEnum.SET_REWARD_DISTRIBUTION.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.SET_REWARD_DISTRIBUTION.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_HardWorkTx() {
    String hash = "0xef92345cbdd7d25e1d0d2f8050c9d672b9cf3f7aa8e42ecffac602245776e758";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, tx.getHash());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(DeployerActivityEnum.DO_HARD_WORK.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.DO_HARD_WORK.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_SetPathTx() {
    String hash = "0xa21735d140f7877a3e72ea62ef6a4725d5e6ae742a9c04003b7a9f7ebd874334";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, tx.getHash());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(DeployerActivityEnum.SET_PATH.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.SET_PATH.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_FailedTx() {
    String hash = "0xc2a5a2705451d33f2817a7472bdf963e8169c4bc796059b58bd46a3b4fd250e6";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, tx.getHash());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(DeployerActivityEnum.DO_HARD_WORK.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.DO_HARD_WORK.name(), dto.getType());
    assertEquals(0, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_UnknownTx() {
    String hash = "0xabd90485e1c558a25b1f8a7f04f338bc5d32151aaa72a2468b739dcf5442d07e";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, dto.getId());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals("0xe33a87c7", dto.getMethodName());
    assertEquals(DeployerActivityEnum.UNKNOWN.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_ContractCreationTx() {
    String hash = "0xc4000221276b1e7a97cb2f64f66e9df5e68794915379611c549805b831365f8b";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, dto.getId());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(DeployerActivityEnum.CONTRACT_CREATION.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.CONTRACT_CREATION.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_NoInputDataTx() {
    String hash = "0x5e654d894b1b37eb98a19b724182ee028d541d0b87033de0ca97bcb5bb6f597d";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, dto.getId());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(DeployerActivityEnum.NO_INPUT_DATA.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.NO_INPUT_DATA.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }

  @Test
  public void testParseDeployerTransaction_NoInputDataTx_CheckAllFields() {
    String hash = "0xcbbf816c41ad31cd68dacc873ec2f1a025bb80864cab3d2a846398a77b294ddc";
    Transaction tx = web3Service.findTransaction(hash);
    DeployerDTO dto = parser.parseDeployerTransaction(tx);
    assertEquals(hash, dto.getId());
    assertEquals(121, dto.getIdx());
    assertEquals(11804364, dto.getBlock());
    assertEquals(1612636157, dto.getBlockDate());
    assertEquals("0x7ba605bc00ea26512a639d5e0335eaeb3e81ad94", dto.getToAddress());
    assertEquals(BigDecimal.valueOf(3.5), dto.getValue());
    assertEquals(BigInteger.valueOf(21000), dto.getGasLimit());
    assertEquals(BigInteger.valueOf(21000), dto.getGasUsed());
    assertEquals(BigInteger.valueOf(220), dto.getGasPrice());
    assertEquals(ContractConstants.DEPLOYER, dto.getFromAddress());
    assertEquals(DeployerActivityEnum.NO_INPUT_DATA.getMethodName(), dto.getMethodName());
    assertEquals(DeployerActivityEnum.NO_INPUT_DATA.name(), dto.getType());
    assertEquals(1, dto.getConfirmed());
  }
}
