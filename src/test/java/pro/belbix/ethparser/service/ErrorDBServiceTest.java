package pro.belbix.ethparser.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.ErrorEntity;
import pro.belbix.ethparser.repositories.ErrorsRepository;
import pro.belbix.ethparser.web3.contracts.db.ErrorDbService;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class ErrorDBServiceTest {

  @Autowired
  private ErrorDbService errorDbService;
  @Autowired
  private ErrorService errorService;
  @Autowired
  private ErrorsRepository errorsRepository;

  @BeforeEach
  public void init() {
    clearDb();
    prepareDB();

  }

  @Test
  public void test_startFixErrorService() {
    List<ErrorEntity> allData = errorDbService.getAllErrors();
    assertAll(
        () -> assertEquals(allData.size(), 3, "allData.size()"),
        () -> assertEquals(allData.get(0).getErrorClass(), "VaultActionsParser",
            "get(0).getErrorClass()"),
        () -> assertEquals(allData.get(1).getErrorClass(), "UniswapLpLogParser",
            "get(1).getErrorClass()")
    );

    errorService.startFixErrorService();
    assertEquals(errorDbService.getAllErrors().size(), 0, "checkDbAfterSceduler");
  }

  private void prepareDB() {
    ErrorEntity vaultActionsParserError = new ErrorEntity();
    vaultActionsParserError.setErrorClass("VaultActionsParser");
    vaultActionsParserError.setNetwork("eth");
    vaultActionsParserError.setJson("{\n"
        + "  \"removed\" : false,\n"
        + "  \"logIndex\" : 105,\n"
        + "  \"transactionIndex\" : 83,\n"
        + "  \"transactionHash\" : \"0x04796a3888b08059e90bcbadf20e127170097919587693989f94ebe8bb97d22c\",\n"
        + "  \"blockHash\" : \"0xc70f7f2ba12097d88377692836b0b4e524ce0290935a4abf10ec8fbcfb075527\",\n"
        + "  \"blockNumber\" : 10969546,\n"
        + "  \"address\" : \"0xdac17f958d2ee523a2206206994597c13d831ec7\",\n"
        + "  \"data\" : \"0x00000000000000000000000000000000000000000000000000000000ec08ce00\",\n"
        + "  \"type\" : null,\n"
        + "  \"topics\" : [ \"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef\", \"0x0000000000000000000000002f1cf0b0bb60a8707e54f9e97d907fce4fc91b94\", \"0x000000000000000000000000ee7358a67669cc6ba95fcf1c366f53514e5f841b\" ],\n"
        + "  \"transactionIndexRaw\" : \"0x53\",\n"
        + "  \"blockNumberRaw\" : \"0xa761ca\",\n"
        + "  \"logIndexRaw\" : \"0x69\"\n"
        + "}");

    ErrorEntity uniswapLpLogParser = new ErrorEntity();
    uniswapLpLogParser.setErrorClass("UniswapLpLogParser");
    uniswapLpLogParser.setNetwork("eth");
    uniswapLpLogParser.setJson("{\n"
        + "  \"removed\" : false,\n"
        + "  \"logIndex\" : 6,\n"
        + "  \"transactionIndex\" : 2,\n"
        + "  \"transactionHash\" : \"0xe74931ed1310a2a98671884f6112714b83250045ebee0c48d8a947899d91b038\",\n"
        + "  \"blockHash\" : \"0xc70f7f2ba12097d88377692836b0b4e524ce0290935a4abf10ec8fbcfb075527\",\n"
        + "  \"blockNumber\" : 10969546,\n"
        + "  \"address\" : \"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2\",\n"
        + "  \"data\" : \"0x00000000000000000000000000000000000000000000000001edde625e7d4967\",\n"
        + "  \"type\" : null,\n"
        + "  \"topics\" : [ \"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef\", \"0x0000000000000000000000005a265315520696299fa1ece0701c3a1ba961b888\", \"0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d\" ],\n"
        + "  \"blockNumberRaw\" : \"0xa761ca\",\n"
        + "  \"logIndexRaw\" : \"0x6\",\n"
        + "  \"transactionIndexRaw\" : \"0x2\"\n"
        + "}");
    ErrorEntity withoutAddress = new ErrorEntity();
    withoutAddress.setErrorClass("UniswapLpLogParser");
    withoutAddress.setNetwork("eth");
    withoutAddress.setJson("{\n"
        + "  \"removed\" : false,\n"
        + "  \"logIndex\" : 6,\n"
        + "  \"transactionIndex\" : 2,\n"
        + "  \"transactionHash\" : \"0xe74931ed1310a2a98671884f6112714b83250045ebee0c48d8a947899d91b038\",\n"
        + "  \"blockHash\" : \"0xc70f7f2ba12097d88377692836b0b4e524ce0290935a4abf10ec8fbcfb075527\",\n"
        + "  \"blockNumber\" : 10969546,\n"
        + "  \"address\" : \"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2\",\n"
        + "  \"data\" : \"0x00000000000000000000000000000000000000000000000001edde625e7d4967\",\n"
        + "  \"type\" : null,\n"
        + "  \"topics\" : [ \"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef\", \"0x0000000000000000000000005a265315520696299fa1ece0701c3a1ba961b888\", \"0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d\" ],\n"
        + "  \"logIndexRaw\" : \"0x6\",\n"
        + "  \"transactionIndexRaw\" : \"0x2\",\n"
        + "  \"blockNumberRaw\" : \"0xa761ca\"\n"
        + "}");

    errorsRepository.save(vaultActionsParserError);
    errorsRepository.save(uniswapLpLogParser);
    errorsRepository.save(withoutAddress);
  }

  @Test
  public void test_parseJsonToLog() {
    Log log = errorService
        .parseJsonToLog("{\n"
            + "  \"removed\" : false,\n"
            + "  \"logIndex\" : 6,\n"
            + "  \"transactionIndex\" : 2,\n"
            + "  \"transactionHash\" : \"0xe74931ed1310a2a98671884f6112714b83250045ebee0c48d8a947899d91b038\",\n"
            + "  \"blockHash\" : \"0xc70f7f2ba12097d88377692836b0b4e524ce0290935a4abf10ec8fbcfb075527\",\n"
            + "  \"blockNumber\" : 10969546,\n"
            + "  \"address\" : \"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2\",\n"
            + "  \"data\" : \"0x00000000000000000000000000000000000000000000000001edde625e7d4967\",\n"
            + "  \"type\" : null,\n"
            + "  \"topics\" : [ \"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef\", \"0x0000000000000000000000005a265315520696299fa1ece0701c3a1ba961b888\", \"0x0000000000000000000000007a250d5630b4cf539739df2c5dacb4c659f2488d\" ],\n"
            + "  \"logIndexRaw\" : \"0x6\",\n"
            + "  \"transactionIndexRaw\" : \"0x2\",\n"
            + "  \"blockNumberRaw\" : \"0xa761ca\"\n"
            + "}");

    assertAll(
        () -> assertFalse(log.isRemoved(), "removed"),
        () -> assertEquals(log.getLogIndex(), new BigInteger("6"), "logIndex"),
        () -> assertEquals(log.getTransactionIndex(), new BigInteger("2"), "TransactionIndex"),
        () -> assertEquals(log.getTransactionHash(),
            "0xe74931ed1310a2a98671884f6112714b83250045ebee0c48d8a947899d91b038",
            "TransactionHash"),
        () -> assertEquals(log.getBlockHash(),
            "0xc70f7f2ba12097d88377692836b0b4e524ce0290935a4abf10ec8fbcfb075527", "BlockHash"),
        () -> assertEquals(log.getBlockNumber(), new BigInteger("10969546"), "BlockNumber"),
        () -> assertEquals(log.getAddress(), "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
            "Address"),
        () -> assertEquals(log.getData(),
            "0x00000000000000000000000000000000000000000000000001edde625e7d4967", "Data"),
        () -> assertEquals(log.getTopics().size(), 3, "TopicSize")
    );
  }

  @Test
  public void test_parseJsonToTransaction() {
    Transaction transaction = errorService
        .parseJsonToTransaction("{\n"
            + "  \"hash\" : \"0xbb0be1ae2376e32ee35d86a835299e163dece6a6a6c19f3770b9a8b4a43b85db\",\n"
            + "  \"nonce\" : 83,\n"
            + "  \"blockHash\" : \"0x105568a7580795e823eaf3ce9cdb1361fd9a99adb3c2e38d284084b589929a71\",\n"
            + "  \"blockNumber\" : 5800113,\n"
            + "  \"transactionIndex\" : 0,\n"
            + "  \"from\" : \"0x1ff037fb77b995e37f70c566e9052f63c51a08f1\",\n"
            + "  \"to\" : \"0x05ff2b0db69458a0750badebc4f9e13add608c7f\",\n"
            + "  \"value\" : 0,\n"
            + "  \"gasPrice\" : 151000000000,\n"
            + "  \"gas\" : 176214,\n"
            + "  \"input\" : \"0x38ed173900000000000000000000000000000000000000000000000ad78ebc5ac620000000000000000000000000000000000000000000000000000a1c16e69e7de55edc00000000000000000000000000000000000000000000000000000000000000a00000000000000000000000001ff037fb77b995e37f70c566e9052f63c51a08f1000000000000000000000000000000000000000000000000000000006053d7430000000000000000000000000000000000000000000000000000000000000003000000000000000000000000acb2d47827c9813ae26de80965845d80935afd0b000000000000000000000000bb4cdb9cbd36b01bd1cbaebf2de08d9173bc095c000000000000000000000000e9e7cea3dedca5984780bafc599bd69add087d56\",\n"
            + "  \"creates\" : null,\n"
            + "  \"publicKey\" : null,\n"
            + "  \"raw\" : null,\n"
            + "  \"r\" : \"0xdb3f02ddcd0d5e2f254569b142a2ad9e624c604ed0deec5cd937bd089db8711\",\n"
            + "  \"s\" : \"0x71dd0342960560e6c59c72047b06a05cbd10f96e502d7deee110525a34e4c832\",\n"
            + "  \"v\" : 147,\n"
            + "  \"transactionIndexRaw\" : \"0x0\",\n"
            + "  \"blockNumberRaw\" : \"0x5880b1\",\n"
            + "  \"gasRaw\" : \"0x2b056\",\n"
            + "  \"nonceRaw\" : \"0x53\",\n"
            + "  \"chainId\" : 56,\n"
            + "  \"gasPriceRaw\" : \"0x23284d2600\",\n"
            + "  \"valueRaw\" : \"0x0\"\n"
            + "}");

    assertAll(
        () -> assertEquals(transaction.getHash(),
            "0xbb0be1ae2376e32ee35d86a835299e163dece6a6a6c19f3770b9a8b4a43b85db", "Hash"),
        () -> assertEquals(transaction.getNonce(), new BigInteger("83"), "Nonce"),
        () -> assertEquals(transaction.getBlockHash(),
            "0x105568a7580795e823eaf3ce9cdb1361fd9a99adb3c2e38d284084b589929a71", "BlockHash"),
        () -> assertEquals(transaction.getBlockNumber(), new BigInteger("5800113"), "BlockNumber"),
        () -> assertEquals(transaction.getTransactionIndex(), new BigInteger("0"), "TransactionIndex"),
        () -> assertEquals(transaction.getFrom(), "0x1ff037fb77b995e37f70c566e9052f63c51a08f1",
            "From"),
        () -> assertEquals(transaction.getTo(), "0x05ff2b0db69458a0750badebc4f9e13add608c7f", "To"),
        () -> assertEquals(transaction.getGasPrice(), new BigInteger("151000000000"), "GasPrice"),
        () -> assertEquals(transaction.getGas(), new BigInteger("176214"), "Gas")
    );
  }


  private void clearDb() {
    errorsRepository.deleteAll();
  }

}
