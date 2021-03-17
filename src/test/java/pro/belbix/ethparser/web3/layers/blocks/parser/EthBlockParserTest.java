package pro.belbix.ethparser.web3.layers.blocks.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.Builder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;
import pro.belbix.ethparser.repositories.a_layer.EthBlockRepository;
import pro.belbix.ethparser.web3.Web3Service;
import pro.belbix.ethparser.web3.layers.blocks.db.EthBlockDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class EthBlockParserTest {

    @Autowired
    private Web3Service web3Service;
    @Autowired
    private EthBlockParser ethBlockParser;
    @Autowired
    private EthBlockDbService ethBlockDbService;
    @Autowired
    private EthBlockRepository ethBlockRepository;

    @Test
    void testBlockParsing_12055816() {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
            "0xd9f8b787073cae9e09162a156e5b93731a2c46bd93a50a93a1feb684ddfd16ba",
            true
        ));
        assertBlock(ethBlockEntity, AssertData.builder()
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
    }

    @Test
    public void smokeTest()
        throws JsonProcessingException, ExecutionException, InterruptedException {
        EthBlockEntity ethBlockEntity = ethBlockParser.parse(web3Service.findBlockByHash(
            "0xaa20f7bde5be60603f11a45fc4923aab7552be775403fc00c2e6b805e6297dbe",
            true
        ));
        assertNotNull(ethBlockEntity);
        String entityStr = new ObjectMapper().writeValueAsString(ethBlockEntity);
        assertNotNull(entityStr);
//        System.out.println(entityStr);
        CompletableFuture<EthBlockEntity> result = ethBlockDbService.save(ethBlockEntity);
        if (result != null) {
            EthBlockEntity persistedEntity = result.join();
            if (persistedEntity != null) {
                String persisted = new ObjectMapper().writeValueAsString(persistedEntity);
                assertNotNull(persisted);
                System.out.println(persisted);
            }
        }
        System.out.println("load entity");
        EthBlockEntity saved = ethBlockRepository.findById(ethBlockEntity.getNumber())
            .orElseThrow();
        System.out.println("delete entity");
        ethBlockRepository.delete(saved);
    }

    private void assertBlock(EthBlockEntity block, AssertData data) {
        assertNotNull(block);
        assertAll(
            () -> assertEquals(data.blockNum, block.getNumber(), "block num"),
            () -> assertEquals(data.blockTimestamp, block.getTimestamp(), "block timestamp"),
            () -> assertEquals(data.txSize, block.getTransactions().size(), "tx size"),
            () -> assertTx(new ArrayList<>(block.getTransactions()).get(data.txNum), data)
        );
    }

    private void assertTx(EthTxEntity tx, AssertData data) {
        assertAll(
            () -> assertEquals(data.txIdx, tx.getTransactionIndex(), "tx index"),
            () -> assertEquals(data.txValue, tx.getValue(), "tx value"),
            () -> assertEquals(data.txInput, tx.getInput(), "tx input"),
            () -> assertEquals(data.txStatus, tx.getStatus(), "tx status"),
            () -> assertEquals(data.txHash, tx.getHash().getHash(), "tx hash"),
            () -> assertEquals(data.txFrom, tx.getFromAddress().getAddress(), "tx from adr"),
            () -> assertEquals(data.txTo, tx.getToAddress().getAddress(), "tx to adr"),
            () -> {
                if (tx.getContractAddress() != null) {
                    assertEquals(data.txContractAdr, tx.getContractAddress().getAddress(),
                        "tx contr adr");
                }
            },
            () -> assertEquals(data.logSize, tx.getLogs().size(), "tx log size"),
            () -> assertLogs(new ArrayList<>(tx.getLogs()).get(data.logNum), data)
        );
    }

    private void assertLogs(EthLogEntity ethLog, AssertData data) {
        assertAll(
            () -> assertEquals(data.logIdx, ethLog.getLogId(), "log id"),
            () -> assertEquals(data.logTxIdx, ethLog.getTransactionIndex(), "log tx idx"),
            () -> assertEquals(data.logData, ethLog.getData(), "log data"),
            () -> assertEquals(data.logType, ethLog.getType(), "log type"),
            () -> assertEquals(data.logAdr, ethLog.getAddress().getAddress(), "log adr"),
            () -> assertEquals(data.logTopic, ethLog.getFirstTopic().getHash(), "log topic"),
            () -> assertEquals(data.logTopics, ethLog.getTopics(), "log topics")
        );
    }

    @Builder
    private static class AssertData {

        long blockNum;
        long blockTimestamp;
        int txSize;
        int txNum;
        long txIdx;
        String txValue;
        String txInput;
        String txStatus;
        String txHash;
        String txFrom;
        String txTo;
        String txContractAdr;
        int logSize;
        int logNum;
        long logIdx;
        long logTxIdx;
        String logData;
        String logType;
        String logAdr;
        String logTopic;
        String logTopics;
    }
}
