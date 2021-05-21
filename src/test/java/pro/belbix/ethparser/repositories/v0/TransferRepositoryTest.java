package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.web3.erc20.TransferType.LP_SELL;
import static pro.belbix.ethparser.web3.erc20.TransferType.PS_EXIT;
import static pro.belbix.ethparser.web3.erc20.TransferType.REWARD;

import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.TransferDTO;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class TransferRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;

    private int id = 0;

    private TransferDTO first;
    private TransferDTO two;
    private TransferDTO three;
    private TransferDTO four;
    private TransferDTO five;
    private TransferDTO six;
    private TransferDTO seven;

    @AfterEach
    public void clearDBBAfterTest() {
        clearDB();
    }

    @Test
    public void test_fetchAllWithoutAddresses() {
        assertNotNull(transferRepository.fetchAllWithoutAddresses());
    }

    @Test
    public void test_fetchAllWithoutProfits() {
        //given
        first = createDto("firstAddress", "twoAddress", 10.0);
        first.setType(PS_EXIT.toString());

        two = createDto("twoAddress", "firstAddress", 1.02);
        two.setPrice(0);
        two.setType(PS_EXIT.toString());

        three = createDto("otherAddress", "firstAddress", 2.004);
        three.setMethodName("TestMethodName");
        three.setPrice(33D);
        three.setProfit(1D);
        three.setType(REWARD.toString());

        four = createDto("otherAddress", "firstAddress", 2.004);
        four.setNetwork("OtherTestNetwork");
        four.setType(LP_SELL.toString());

        //when
        fillDB();

        List<TransferDTO> fetchTestNetwork = transferRepository
            .fetchAllWithoutProfits("TestNetwork");

        List<TransferDTO> fetchOtherTestNetwork = transferRepository
            .fetchAllWithoutProfits("OtherTestNetwork");

        //then
        assertAll(
            () -> Assertions.assertThrows(InvalidDataAccessResourceUsageException.class, () -> {
                transferRepository.fetchAllWithoutProfits(null);
            }),

            () -> assertEquals(fetchTestNetwork.size(), 2, "fetchTestNetwork"),
            () -> assertModel(first, fetchTestNetwork.get(0)),
            () -> assertModel(two, fetchTestNetwork.get(1)),

            () -> assertEquals(fetchOtherTestNetwork.size(), 1, "fetchOtherTestNetwork"),
            () -> assertModel(four, fetchOtherTestNetwork.get(0))
        );
    }

    @Test
    public void test_fetchAllWithoutPrice() {
        //given
        first = createDto("firstAddress", "twoAddress", 10.0);
        first.setType(PS_EXIT.toString());

        two = createDto("twoAddress", "firstAddress", 1.02);
        two.setPrice(0);

        three = createDto("otherAddress", "firstAddress", 2.004);
        three.setMethodName("TestMethodName");
        three.setPrice(33D);

        four = createDto("otherAddress", "firstAddress", 2.004);
        four.setNetwork("OtherTestNetwork");

        //when
        fillDB();

        List<TransferDTO> fetchTestNetwork = transferRepository
            .fetchAllWithoutPrice("TestNetwork");

        List<TransferDTO> fetchOtherTestNetwork = transferRepository
            .fetchAllWithoutPrice("OtherTestNetwork");

        //then
        assertAll(
            () -> Assertions.assertThrows(InvalidDataAccessResourceUsageException.class, () -> {
                transferRepository.fetchAllWithoutPrice(null);
            }),
            () -> assertEquals(fetchTestNetwork.size(), 2, "fetchTestNetwork"),
            () -> assertModel(first, fetchTestNetwork.get(0)),
            () -> assertModel(two, fetchTestNetwork.get(1)),

            () -> assertEquals(fetchOtherTestNetwork.size(), 1, "fetchOtherTestNetwork"),
            () -> assertModel(four, fetchOtherTestNetwork.get(0))
        );
    }

    @Test
    public void test_fetchAllWithoutMethods() {
        //given
        first = createDto("firstAddress", "twoAddress", 10.0);
        first.setMethodName(null);

        two = createDto("twoAddress", "firstAddress", 1.02);
        two.setMethodName("0xSMTH");

        three = createDto("otherAddress", "firstAddress", 2.004);
        three.setMethodName("TestMethodName");

        four = createDto("otherAddress", "firstAddress", 2.004);
        four.setNetwork("OtherTestNetwork");
        four.setMethodName(null);

        //when
        fillDB();

        List<TransferDTO> fetchTestNetwork = transferRepository.fetchAllWithoutMethods("TestNetwork");
        List<TransferDTO> fetchOtherTestNetwork = transferRepository
            .fetchAllWithoutMethods("OtherTestNetwork");

        //then
        assertAll(
            () -> Assertions.assertThrows(InvalidDataAccessResourceUsageException.class, () -> {
                transferRepository.fetchAllWithoutMethods(null);
            }),
            () -> assertEquals(fetchTestNetwork.size(), 2, "fetchTestNetwork"),
            () -> assertModel(first, fetchTestNetwork.get(0)),
            () -> assertModel(two, fetchTestNetwork.get(1)),

            () -> assertEquals(fetchOtherTestNetwork.size(), 1, "fetchOtherTestNetwork"),
            () -> assertModel(four, fetchOtherTestNetwork.get(0))
        );
    }

    @Test
    public void test_fetchAllFromBlockDate() {
        //given
        first = createDto("firstAddress", "twoAddress", 10.0);
        two = createDto("twoAddress", "firstAddress", 1.02);
        three = createDto("otherAddress", "firstAddress", 2.004);
        four = createDto("firstAddress", "twoAddress", 4.04);
        four.setNetwork("otherNetwork");
        five = createDto(null, "firstAddress", 4.8);

        //when
        fillDB();

        List<TransferDTO> fetchAllNetwork = transferRepository.fetchAllFromBlockDate(0, "TestNetwork");
        List<TransferDTO> fetchOtherNtw = transferRepository.fetchAllFromBlockDate(0, "otherNetwork");
        List<TransferDTO> fetchMore2LDate = transferRepository.fetchAllFromBlockDate(2, "TestNetwork");
        List<TransferDTO> fetchMore6LDate = transferRepository.fetchAllFromBlockDate(6, "TestNetwork");

        //then
        assertAll(
            () -> assertEquals(fetchAllNetwork.size(), 4, "fetchAllNetwork"),
            () -> assertModel(first, fetchAllNetwork.get(0)),
            () -> assertModel(five, fetchAllNetwork.get(3)),

            () -> assertEquals(fetchOtherNtw.size(), 1, "fetchOtherNtw"),
            () -> assertModel(four, fetchOtherNtw.get(0)),

            () -> assertEquals(fetchMore2LDate.size(), 2, "fetchMore2LDate"),
            () -> assertModel(three, fetchMore2LDate.get(0)),
            () -> assertModel(five, fetchMore2LDate.get(1)),

            () -> assertEquals(fetchMore6LDate.size(), 0, "fetchMore6LDate")
        );
    }

    @Test
    public void test_getBalanceForOwner() {
        //given
        first = createDto("firstAddress", "twoAddress", 10.0);
        two = createDto("twoAddress", "firstAddress", 1.02);
        three = createDto("otherAddress", "firstAddress", 2.004);
        four = createDto("firstAddress", "twoAddress", 4.4);
        four.setNetwork(null);
        five = createDto(null, "firstAddress", 4.8);
        six = createDto("firstAddress", null, 3.003);
        seven = createDto("firstAddress", "firstAddress", 15.01);

        //when
        fillDB();

        Double onlyFirstBLockFirstAddress = transferRepository
            .getBalanceForOwner(
                "firstAddress",
                1,
                "TestNetwork");

        Double onlyFirstBLockTwoAddress = transferRepository
            .getBalanceForOwner(
                "twoAddress",
                1,
                "TestNetwork");

        Double allBlocksTwoAddress = transferRepository
            .getBalanceForOwner(
                "twoAddress",
                Long.MAX_VALUE,
                "TestNetwork");

        Double allBlocksFirstAddress = transferRepository
            .getBalanceForOwner(
                "firstAddress",
                Long.MAX_VALUE,
                "TestNetwork");

        Double allBlocksOtherAddress = transferRepository
            .getBalanceForOwner(
                "otherAddress",
                Long.MAX_VALUE,
                "TestNetwork");

        Double before6LBlocksFirstAddress = transferRepository
            .getBalanceForOwner(
                "firstAddress",
                6L,
                "TestNetwork");

        Double before7LBlocksFirstAddress = transferRepository
            .getBalanceForOwner(
                "firstAddress",
                7L,
                "TestNetwork");

        //then
        assertAll(
            () -> Assertions.assertThrows(InvalidDataAccessResourceUsageException.class, () -> {
                transferRepository.getBalanceForOwner(null, 1, null);
            }),

            () -> assertEquals(-10.0, onlyFirstBLockFirstAddress, 0.00001),
            () -> assertEquals(10.0, onlyFirstBLockTwoAddress, 0.00001),
            () -> assertEquals(8.98, allBlocksTwoAddress, 0.00001),
            () -> assertEquals(-5.17899, allBlocksFirstAddress, 0.00001),
            () -> assertEquals(-2.004, allBlocksOtherAddress, 0.00001),
            () -> assertEquals(-5.179, before6LBlocksFirstAddress, 0.00001),
            () -> assertEquals(-5.17899, before7LBlocksFirstAddress, 0.00001)
        );
    }

    @Test
    public void test_fetchAllByOwnerAndRecipient() {
        //given
        first = createDto("FirstOwner", "FirstRecipient", 10.0);
        first.setName("FARM");
        two = createDto("FirstOwner", "FirstRecipient", 1.02);
        two.setName("FARM");
        three = createDto("FirstOwner", "FirstRecipient", 2.004);
        three.setName("FARM");
        three.setNetwork(null);
        four = createDto(null, "TwoRecipient", 4.4);
        four.setName("FARM");
        five = createDto("TwoOwner", null, 4.8);
        five.setName("FARM");
        six = createDto("TwoOwner", "TwoRecipient", 3.003);
        six.setName("FARM");
        seven = createDto("FirstOwner", "TwoRecipient", 15.01);
        seven.setName("FARM");

        //when
        fillDB();

        List<TransferDTO> firstOwnerOrFirstRecipientRangeAll = transferRepository
            .fetchAllByOwnerAndRecipient(
                "FirstOwner",
                "FirstRecipient",
                0,
                Long.MAX_VALUE,
                "TestNetwork");

        List<TransferDTO> twoOwnerOrTwoRecipientRangeAll = transferRepository
            .fetchAllByOwnerAndRecipient(
                "TwoOwner",
                "TwoRecipient",
                0,
                Long.MAX_VALUE,
                "TestNetwork");

        List<TransferDTO> firstOwnerOrTwoRecipientRangeAll = transferRepository
            .fetchAllByOwnerAndRecipient(
                "FirstOwner",
                "TwoRecipient",
                0,
                Long.MAX_VALUE,
                "TestNetwork");

        List<TransferDTO> confusedFirstOwnerOrFirstRecipientRangeAll = transferRepository
            .fetchAllByOwnerAndRecipient(
                "FirstRecipient",
                "FirstOwner",
                0,
                Long.MAX_VALUE,
                "TestNetwork");

        List<TransferDTO> firstOwnerOrFirstRecipientRangeFrom1LTo5L = transferRepository
            .fetchAllByOwnerAndRecipient(
                "FirstOwner",
                "FirstRecipient",
                1L,
                5L,
                "TestNetwork");
        //then
        assertAll(
            () -> assertEquals(firstOwnerOrFirstRecipientRangeAll.size(), 3,
                "firstOwnerOrFirstRecipientRangeAll"),
            () -> assertModel(first, firstOwnerOrFirstRecipientRangeAll.get(0)),
            () -> assertModel(seven, firstOwnerOrFirstRecipientRangeAll.get(2)),

            () -> assertEquals(twoOwnerOrTwoRecipientRangeAll.size(), 4),
            () -> assertModel(four, twoOwnerOrTwoRecipientRangeAll.get(0)),
            () -> assertModel(seven, twoOwnerOrTwoRecipientRangeAll.get(3)),

            () -> assertEquals(firstOwnerOrTwoRecipientRangeAll.size(), 5),
            () -> assertModel(first, firstOwnerOrTwoRecipientRangeAll.get(0)),
            () -> assertModel(seven, firstOwnerOrTwoRecipientRangeAll.get(4)),

            () -> assertEquals(confusedFirstOwnerOrFirstRecipientRangeAll.size(), 0),
            () -> assertEquals(firstOwnerOrFirstRecipientRangeFrom1LTo5L.size(), 2),
            () -> assertModel(first, firstOwnerOrFirstRecipientRangeFrom1LTo5L.get(0)),
            () -> assertModel(two, firstOwnerOrFirstRecipientRangeFrom1LTo5L.get(1))
        );
    }

    private TransferDTO createDto(
        String owner,
        String recipient,
        Double value) {

        id++;
        TransferDTO dto = new TransferDTO();
        dto.setId(String.valueOf(id));
        dto.setNetwork("TestNetwork");
        dto.setBlockDate(id);
        dto.setOwner(owner);
        dto.setRecipient(recipient);
        dto.setTokenAddress("AddressTest");
        dto.setValue(value);
        return dto;
    }

    private void clearDB() {
        id = 0;
        deleteFromDB(first);
        deleteFromDB(two);
        deleteFromDB(three);
        deleteFromDB(four);
        deleteFromDB(five);
        deleteFromDB(six);
        deleteFromDB(seven);

        first = null;
        two = null;
        three = null;
        four = null;
        five = null;
        six = null;
        seven = null;
    }

    private void fillDB() {
        id = 0;
        saveToDB(first);
        saveToDB(two);
        saveToDB(three);
        saveToDB(four);
        saveToDB(five);
        saveToDB(six);
        saveToDB(seven);
    }

    private void deleteFromDB(TransferDTO dto) {
        if (dto != null) {
            transferRepository.delete(dto);
        }
    }

    private void saveToDB(TransferDTO dto) {
        if (dto != null) {
            transferRepository.save(dto);
        }
    }
}
