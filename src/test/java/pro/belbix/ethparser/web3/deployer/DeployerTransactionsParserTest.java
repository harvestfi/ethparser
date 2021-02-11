package pro.belbix.ethparser.web3.deployer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.DeployerDTO;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.deployer.decoder.DeployerActivityEnum;
import pro.belbix.ethparser.web3.deployer.parser.DeployerTransactionsParser;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DeployerTransactionsParserTest
{
    @Autowired
    private Web3Service web3Service;

    @Autowired
    private DeployerTransactionsParser parser;

    @Test
    public void testParseDeployerTransaction_SetFeeTx()
    {
        Transaction tx = web3Service
                .findTransaction("0x648a866acbd927613f4fc3c1d510415074da8a4d796b43a34da3f981b89229c3");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.SET_FEE_REWARD_FORWARDER.name(), dto.getType());
    }

    @Test
    public void testParseDeployerTransaction_SetRewardDistTx()
    {
        Transaction tx = web3Service
                .findTransaction("0xe082536c0360c4c4a3e119d0b021b242891403096cf67bbdb89555e33facc7cf");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.SET_REWARD_DISTRIBUTION.name(), dto.getType());
    }

    @Test
    public void testParseDeployerTransaction_HardWorkTx()
    {
        Transaction tx = web3Service
                .findTransaction("0xef92345cbdd7d25e1d0d2f8050c9d672b9cf3f7aa8e42ecffac602245776e758");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.DO_HARD_WORK.name(), dto.getType());
    }

    @Test
    public void testParseDeployerTransaction_SetPathTx()
    {
        Transaction tx = web3Service
                .findTransaction("0xa21735d140f7877a3e72ea62ef6a4725d5e6ae742a9c04003b7a9f7ebd874334");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.SET_PATH.name(), dto.getType());
    }

    @Test
    public void testParseDeployerTransaction_FailedTx()
    {
        Transaction tx = web3Service
                .findTransaction("0xc2a5a2705451d33f2817a7472bdf963e8169c4bc796059b58bd46a3b4fd250e6");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertFalse(dto.isConfirmed());
    }

    @Test
    public void testParseDeployerTransaction_UnknownTx()
    {
        Transaction tx = web3Service
                .findTransaction("0xabd90485e1c558a25b1f8a7f04f338bc5d32151aaa72a2468b739dcf5442d07e");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.UNKNOWN.name(), dto.getType());
    }

    @Test
    public void testParseDeployerTransaction_ContractCreationTx()
    {
        Transaction tx = web3Service
                .findTransaction("0xc4000221276b1e7a97cb2f64f66e9df5e68794915379611c549805b831365f8b");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.CONTRACT_CREATION.name(), dto.getType());
    }

    @Test
    public void testParseDeployerTransaction_NoInputDataTx()
    {
        Transaction tx = web3Service
                .findTransaction("0x5e654d894b1b37eb98a19b724182ee028d541d0b87033de0ca97bcb5bb6f597d");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.NO_INPUT_DATA.name(), dto.getType());
    }

    @Test
    public void testParseDeployerTransaction_NoInputDataTx2()
    {
        Transaction tx = web3Service
                .findTransaction("0xcbbf816c41ad31cd68dacc873ec2f1a025bb80864cab3d2a846398a77b294ddc");
        DeployerDTO dto = parser.parseDeployerTransaction(tx);
        assertTrue(dto.isConfirmed());
        assertEquals(DeployerActivityEnum.NO_INPUT_DATA.name(), dto.getType());
    }
}
