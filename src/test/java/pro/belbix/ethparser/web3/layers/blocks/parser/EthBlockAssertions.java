package pro.belbix.ethparser.web3.layers.blocks.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static pro.belbix.ethparser.TestUtils.assertTwoArrays;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import pro.belbix.ethparser.entity.a_layer.EthBlockEntity;
import pro.belbix.ethparser.entity.a_layer.EthLogEntity;
import pro.belbix.ethparser.entity.a_layer.EthTxEntity;

public class EthBlockAssertions {

  public static void assertContracts(EthBlockEntity block, String contractsPath)
      throws IOException, URISyntaxException {
    Set<String> expected = new HashSet<>(
        Files.readAllLines(
            Paths.get(EthBlockAssertions.class.getClassLoader().getResource(contractsPath).toURI()),
            Charset.defaultCharset())
    );
    Set<String> contracts = new HashSet<>();
    block.getTransactions().forEach(tx -> {
      contracts.add(tx.getFromAddress().getAddress().toLowerCase());
      if (tx.getToAddress() != null) {
        contracts.add(tx.getToAddress().getAddress().toLowerCase());
      }
      if (tx.getContractAddress() != null) {
        contracts.add(tx.getContractAddress().getAddress().toLowerCase());
      }
      tx.getLogs().forEach(l -> contracts.add(l.getAddress().getAddress()));
    });
    assertTwoArrays(new ArrayList<>(contracts), new ArrayList<>(expected));
  }

  public static void assertBlock(EthBlockEntity block, EthBlockTestData data) {
    assertNotNull(block, "EthBlockEntity is null");
    assertAll(
        () -> assertEquals(data.blockNum, block.getNumber(), "block num"),
        () -> assertEquals(data.blockTimestamp, block.getTimestamp(), "block timestamp"),
        () -> assertEquals(data.txSize, block.getTransactions().size(), "tx size"),
        () -> assertTx(new ArrayList<>(block.getTransactions()).get(data.txNum), data)
    );
  }

  private static void assertTx(EthTxEntity tx, EthBlockTestData data) {
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

  private static void assertLogs(EthLogEntity ethLog, EthBlockTestData data) {
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

}
