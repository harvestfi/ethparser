package pro.belbix.ethparser.web3.layers.blocks.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockAssertions.assertBlock;
import static pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockAssertions.assertContracts;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Functions;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class EthBlockParserBscTest {

    @Autowired
    private Web3Functions web3Functions;
    @Autowired
    private EthBlockParser ethBlockParser;
    @Autowired
    private EthBlockDbService ethBlockDbService;
    @Autowired
    private EthBlockRepository ethBlockRepository;

    @BeforeEach
    void setUp() {
        ethBlockRepository.deleteAll();
    }

    @Test
    void testBlockParsing_0() {
        EthBlockEntity block = ethBlockParser.parse(
            web3Functions.findBlockByNumber(0, true, BSC_NETWORK),
            BSC_NETWORK);
        assertNotNull(block, "EthBlockEntity is null");
        assertAll(
            () -> assertEquals(0, block.getNumber(), "block num"),
            () -> assertEquals(1587390414, block.getTimestamp(), "block timestamp")
        );
        assertNotNull(ethBlockDbService.save(block), "persist result");
    }

    @Test
    void testBlockParsing_6200000() throws IOException, URISyntaxException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(
            web3Functions.findBlockByNumber(6200000, true, BSC_NETWORK),
            BSC_NETWORK);
        assertContracts(ethBlockEntity, "data/6200000_bsc_contracts.txt");
        assertBlock(ethBlockEntity, EthBlockTestData.builder()
            .blockNum(6200000)
            .blockTimestamp(1617314202)
            .txSize(128)
            .txNum(1)
            .txIdx(1)
            .txValue("0")
            .txInput(
                "0x095ea7b300000000000000000000000011111112542d85b3ef69ae05771c2dccff4faa26ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
            .txStatus("0x1")
            .txHash("0x50590d3c0e35326ed7178e9619ec6591c5ed98d00110da1ecfca848e3a01c2f4")
            .txFrom("0x0cc849463813e31f9fd0de7a3561ed12d3285e32")
            .txTo("0xbf029ab41f556100f4342bde9644a62a23a6b55b")
            .txContractAdr("")
            .logSize(1)
            .logNum(0)
            .logIdx(4)
            .logTxIdx(1)
            .logData("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
            .logType(null)
            .logAdr("0xbf029ab41f556100f4342bde9644a62a23a6b55b")
            .logTopic("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925")
            .logTopics(
                "0x0000000000000000000000000cc849463813e31f9fd0de7a3561ed12d3285e32,0x00000000000000000000000011111112542d85b3ef69ae05771c2dccff4faa26")
            .build());
        EthBlockEntity persisted = ethBlockDbService.save(ethBlockEntity);
        assertNotNull(persisted);
        assertContracts(persisted, "data/6200000_bsc_contracts.txt");
        assertNull(ethBlockDbService.save(ethBlockEntity));
    }

    @Test
    void testBlockParsing_6101208() throws IOException, URISyntaxException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(
            web3Functions.findBlockByNumber(6101208, true, BSC_NETWORK),
            BSC_NETWORK);
        assertBlock(ethBlockEntity, EthBlockTestData.builder()
            .blockNum(6101208)
            .blockTimestamp(1617016126)
            .txSize(78)
            .txNum(35)
            .txIdx(35)
            .txValue("0")
            .txInput("0xb6b55f2500000000000000000000000000000000000000000000000003b1bf4cb9f50495")
            .txStatus("0x1")
            .txHash("0x2beda2c174968f2f7170d1a1722d29f0a15d29104cb1ff3ae8a63247a9656af2")
            .txFrom("0x7f4ac7a8b18d7dc76c5962aa1aacf968eac3ac67")
            .txTo("0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34")
            .txContractAdr("")
            .logSize(3)
            .logNum(0)
            .logIdx(137)
            .logTxIdx(35)
            .logData("0x00000000000000000000000000000000000000000000000003b1bf4cb9f50495")
            .logType(null)
            .logAdr("0xf7a3a95d0f7e8a5eeae483cdd7b76af287283d34")
            .logTopic("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")
            .logTopics(
                "0x0000000000000000000000000000000000000000000000000000000000000000,0x0000000000000000000000007f4ac7a8b18d7dc76c5962aa1aacf968eac3ac67")
            .build());
        EthBlockEntity persisted = ethBlockDbService.save(ethBlockEntity);
        assertNotNull(persisted);
        assertNull(ethBlockDbService.save(ethBlockEntity));
    }
}
