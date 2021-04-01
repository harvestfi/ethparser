package pro.belbix.ethparser.web3.layers.blocks.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockAssertions.assertBlock;
import static pro.belbix.ethparser.web3.layers.blocks.parser.EthBlockAssertions.assertContracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
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
public class EthBlockParserTest {

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
            web3Functions.findBlockByNumber(0, true));
        assertNotNull(block, "EthBlockEntity is null");
        assertAll(
            () -> assertEquals(0, block.getNumber(), "block num"),
            () -> assertEquals(0, block.getTimestamp(), "block timestamp")
        );
        assertNotNull(ethBlockDbService.save(block), "persist result");
    }

    @Test
    void testBlockParsing_12055816() throws IOException, URISyntaxException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(
            web3Functions.findBlockByNumber(12055816, true));
        assertContracts(ethBlockEntity, "data/12055816_contracts.txt");
        assertBlock(ethBlockEntity, EthBlockTestData.builder()
            .blockNum(12055816)
            .blockTimestamp(1615979134)
            .txSize(195)
            .txNum(1)
            .txIdx(1)
            .txValue("0")
            .txInput("0x578e4b2385a28d2e213e2f88cd1dfacecb84e1d2891fb45dce6cf024896e8f2d3a770821")
            .txStatus("0x1")
            .txHash("0x4c70a0c54c5c7469e346546718da1b71fcb13eaaa370ff7c98bed88c9ef76547")
            .txFrom("0xdd07249e403979bd79848c27aa5454c7e66bdee7")
            .txTo("0x000000000000084e91743124a982076c59f10084")
            .txContractAdr("")
            .logSize(4)
            .logNum(1)
            .logIdx(2)
            .logTxIdx(1)
            .logData("0x000000000000000000000000000000000000000000000000606ece035a5edada")
            .logType(null)
            .logAdr("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2")
            .logTopic("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")
            .logTopics(
                "0x000000000000000000000000570febdf89c07f256c75686caca215289bb11cfc,0x000000000000000000000000000000000000084e91743124a982076c59f10084")
            .build());
        EthBlockEntity persisted = ethBlockDbService.save(ethBlockEntity);
        assertNotNull(persisted);
        assertContracts(persisted, "data/12055816_contracts.txt");
        assertNull(ethBlockDbService.save(ethBlockEntity));
    }

    @Test
    void testBlockParsing_10800000() throws IOException, URISyntaxException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(
            web3Functions.findBlockByNumber(10800000, true));
        assertContracts(ethBlockEntity, "data/10800000_contracts.txt");
        assertBlock(ethBlockEntity, EthBlockTestData.builder()
            .blockNum(10800000)
            .blockTimestamp(1599290487)
            .txSize(167)
            .txNum(1)
            .txIdx(1)
            .txValue("0")
            .txInput(
                "0x095ea7b30000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488dffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
            .txStatus("0x1")
            .txHash("0xa0ef7c140dd4edf747b58d02ac5aa42513fc8fd67ff613483a2424ca56b6e3d4")
            .txFrom("0xd2d865c9d2960b53127c3bfca8a237bc7c803adc")
            .txTo("0xc4da39e646e7f5d233b89ca0f7b75344e7ddb2cc")
            .txContractAdr("")
            .logSize(1)
            .logNum(0)
            .logIdx(0)
            .logTxIdx(1)
            .logData("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
            .logType(null)
            .logAdr("0xc4da39e646e7f5d233b89ca0f7b75344e7ddb2cc")
            .logTopic("0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925")
            .logTopics(
                "0x000000000000000000000000d2d865c9d2960b53127c3bfca8a237bc7c803adc,0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d")
            .build());
        assertContracts(ethBlockEntity, "data/10800000_contracts.txt");
    }

    @Test
    void testBlockParsing__SUSHI_HODL_12030868() throws IOException, URISyntaxException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(
            web3Functions.findBlockByNumber(12030868, true));
        assertContracts(ethBlockEntity, "data/12030868_contracts.txt");
        assertBlock(ethBlockEntity, EthBlockTestData.builder()
            .blockNum(12030868)
            .blockTimestamp(1615647154)
            .txSize(160)
            .txNum(1)
            .txIdx(1)
            .txValue("0")
            .txInput(
                "0xa9059cbb000000000000000000000000f050227be1a7ce587aa83d5013f900dbc3be06110000000000000000000000000000000000000000000000000000000004c4b400")
            .txStatus("0x1")
            .txHash("0x6b28730ab60bbcff5fa51c866ca1217f7e08c77d3bf8f343046c5beddac1a311")
            .txFrom("0xacc865b581314595eb7d68afa109678797cbf0a1")
            .txTo("0xdac17f958d2ee523a2206206994597c13d831ec7")
            .txContractAdr("")
            .logSize(1)
            .logNum(0)
            .logIdx(0)
            .logTxIdx(1)
            .logData("0x0000000000000000000000000000000000000000000000000000000004c4b400")
            .logType(null)
            .logAdr("0xdac17f958d2ee523a2206206994597c13d831ec7")
            .logTopic("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef")
            .logTopics(
                "0x000000000000000000000000acc865b581314595eb7d68afa109678797cbf0a1,0x000000000000000000000000f050227be1a7ce587aa83d5013f900dbc3be0611")
            .build());
        assertContracts(ethBlockEntity, "data/12030868_contracts.txt");
    }

    @Test
    public void smokeTest()
        throws JsonProcessingException, ExecutionException, InterruptedException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Functions.findBlockByHash(
            "0xaa20f7bde5be60603f11a45fc4923aab7552be775403fc00c2e6b805e6297dbe",
            true
        ));
        assertNotNull(ethBlockEntity);
        String entityStr = new ObjectMapper().writeValueAsString(ethBlockEntity);
        assertNotNull(entityStr);
//        System.out.println(entityStr);
        EthBlockEntity persistedEntity = ethBlockDbService.save(ethBlockEntity);
        if (persistedEntity != null) {
            String persisted = new ObjectMapper().writeValueAsString(persistedEntity);
            assertNotNull(persisted);
            System.out.println(persisted);
        }

        System.out.println("load entity");
        EthBlockEntity saved = ethBlockRepository.findById(ethBlockEntity.getNumber())
            .orElseThrow();
        System.out.println("delete entity");
        ethBlockRepository.delete(saved);
    }


}
