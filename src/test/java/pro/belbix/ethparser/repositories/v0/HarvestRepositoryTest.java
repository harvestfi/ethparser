package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static pro.belbix.ethparser.TestUtils.assertModel;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.HarvestDTO;
import pro.belbix.ethparser.repositories.v0.HarvestRepository.UserBalance;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestRepositoryTest {

  private final Pageable limitOne = PageRequest.of(0, 1);
  private final Pageable limitZeroToFive = PageRequest.of(0, 5);

  private int id = 0;

  private HarvestDTO first;
  private HarvestDTO two;
  private HarvestDTO three;
  private HarvestDTO four;
  private HarvestDTO five;
  private HarvestDTO six;
  private HarvestDTO seven;

  @Autowired
  private HarvestRepository harvestRepository;

  @AfterEach
  public void clearDBBAfterTest() {
    clearDB();
  }

  @Test
  public void myTest() {
    //given
    first = createDto();
    first.setOwnerBalanceUsd(50001.0);
    first.setOwner("0xe5350e927b904fdb4d2af55c566e269bb3df1941");
    two = createDto();
    two.setOwnerBalanceUsd(50001.0);
    two.setOwner("0xe5350e927b904fdb4d2af55c566e269bb3df1666");
    three = createDto();
    three.setOwnerBalanceUsd(50000.0);
    three.setOwner("0xe5350e927b904fdb4d2af55c566e269bb3df5555");
    four = createDto();
    four.setOwnerBalanceUsd(50001.0);
    four.setOwner("0xe5350e927b904fdb4d2af55c566e269bb3df2222");
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<UserBalance> allElem = harvestRepository
        .fetchOwnerBalances("TestNetwork");
    List<UserBalance> otherNetwork = harvestRepository
        .fetchOwnerBalances("OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 2,
            "allElem"),
        () -> assertEquals(allElem.get(0).getOwner(), two.getOwner(),
            "allElem.get(0).getOwner()"),
        () -> assertEquals(allElem.get(0).getBalance(), two.getOwnerBalanceUsd(), 0.001,
            "allElem.get(0).getBalance()"),
        () -> assertEquals(allElem.get(1).getOwner(), first.getOwner(),
            "allElem.get(1).getOwner()"),
        () -> assertEquals(allElem.get(1).getBalance(), first.getOwnerBalanceUsd(), 0.001,
            "allElem.get(1).getBalance()"),

        () -> assertEquals(otherNetwork.size(), 1,
            "otherNetwork"),
        () -> assertEquals(otherNetwork.get(0).getOwner(), four.getOwner(),
            "otherNetwork.get(0).getOwner()"),
        () -> assertEquals(otherNetwork.get(0).getBalance(), four.getOwnerBalanceUsd(), 0.001,
            "otherNetwork.get(0).getBalance()")
    );
  }

  @Test
  public void test_fetchPagesByVault() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    Page<HarvestDTO> allElem = harvestRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 0, limitZeroToFive);

    Page<HarvestDTO> limitOneAllElem = harvestRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 0, limitOne);

    Page<HarvestDTO> minAmount3 = harvestRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 3, limitZeroToFive);

    Page<HarvestDTO> minAmount6 = harvestRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 6, limitZeroToFive);

    Page<HarvestDTO> otherNetwork = harvestRepository
        .fetchPagesByVault("TestVault", "OtherNetwork", 0, limitZeroToFive);

    Page<HarvestDTO> otherVault = harvestRepository
        .fetchPagesByVault("OtherVault", "TestNetwork", 0, limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(allElem.getContent().size(), 3, "allElem"),
        () -> assertModel(first, allElem.getContent().get(0)),
        () -> assertModel(three, allElem.getContent().get(2)),

        () -> assertEquals(limitOneAllElem.getContent().size(), 1, "limitOneAllElem"),
        () -> assertModel(first, limitOneAllElem.getContent().get(0)),

        () -> assertEquals(minAmount3.getContent().size(), 1, "minAmount3"),
        () -> assertModel(three, minAmount3.getContent().get(0)),

        () -> assertEquals(minAmount6.getContent().size(), 0, "minAmount6"),

        () -> assertEquals(otherNetwork.getContent().size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.getContent().get(0)),

        () -> assertEquals(otherVault.getContent().size(), 1, "otherVault"),
        () -> assertModel(five, otherVault.getContent().get(0))
    );
  }

  @Test
  public void test_fetchPages() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    Page<HarvestDTO> allElem = harvestRepository
        .fetchPages(0, "TestNetwork", limitZeroToFive);

    Page<HarvestDTO> limitOneAllElem = harvestRepository
        .fetchPages(0, "TestNetwork", limitOne);

    Page<HarvestDTO> minAmount3 = harvestRepository
        .fetchPages(3, "TestNetwork", limitZeroToFive);

    Page<HarvestDTO> minAmount6 = harvestRepository
        .fetchPages(6, "TestNetwork", limitZeroToFive);

    Page<HarvestDTO> otherNetwork = harvestRepository
        .fetchPages(0, "OtherNetwork", limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(allElem.getContent().size(), 4, "allElem"),
        () -> assertModel(first, allElem.getContent().get(0)),
        () -> assertModel(five, allElem.getContent().get(3)),

        () -> assertEquals(limitOneAllElem.getContent().size(), 1, "limitOneAllElem"),
        () -> assertModel(first, limitOneAllElem.getContent().get(0)),

        () -> assertEquals(minAmount3.getContent().size(), 2, "minAmount3"),
        () -> assertModel(three, minAmount3.getContent().get(0)),
        () -> assertModel(five, minAmount3.getContent().get(1)),

        () -> assertEquals(minAmount6.getContent().size(), 0, "minAmount6"),

        () -> assertEquals(otherNetwork.getContent().size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.getContent().get(0))
    );
  }

  @Test
  public void test_fetchAllWithoutAddresses() {
    //given
    first = createDto();
    first.setVaultAddress(null);
    two = createDto();
    two.setLpStat("000coin1Address0000");
    three = createDto();
    three.setLpStat("TestLpStat");
    four = createDto();
    four.setVaultAddress("");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HarvestDTO> fetchedData = harvestRepository.fetchAllWithoutAddresses();

    //then
    assertAll(
        () -> assertEquals(fetchedData.size(), 3, "fetchedData"),
        () -> assertModel(first, fetchedData.get(0)),
        () -> assertModel(three, fetchedData.get(1)),
        () -> assertModel(four, fetchedData.get(2))
    );
  }

  @Test
  public void test_fetchLatestSinceLastWithdraw() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");
    six = createDto();
    six.setOwner("OtherOwner");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .fetchLatestSinceLastWithdraw("TestOwner", "TestVault", Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> block3 = harvestRepository
        .fetchLatestSinceLastWithdraw("TestOwner", "TestVault", 3, "TestNetwork");

    List<HarvestDTO> otherNetwork = harvestRepository
        .fetchLatestSinceLastWithdraw("TestOwner", "TestVault", Long.MAX_VALUE, "OtherNetwork");

    List<HarvestDTO> otherVault = harvestRepository
        .fetchLatestSinceLastWithdraw("TestOwner", "OtherVault", Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> otherOwner = harvestRepository
        .fetchLatestSinceLastWithdraw("OtherOwner", "TestVault", Long.MAX_VALUE, "TestNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 3, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(three, allElem.get(2)),

        () -> assertEquals(block3.size(), 2, "block3"),
        () -> assertModel(first, block3.get(0)),
        () -> assertModel(two, block3.get(1)),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0)),

        () -> assertEquals(otherVault.size(), 1, "otherVault"),
        () -> assertModel(five, otherVault.get(0)),

        () -> assertEquals(otherOwner.size(), 1, "otherOwner"),
        () -> assertModel(six, otherOwner.get(0))
    );
  }

  @Test
  public void test_findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setMethodName("OtherMethodName");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
            "TestMethodName",
            0,
            "TestNetwork");

    List<HarvestDTO> block3 = harvestRepository
        .findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
            "TestMethodName",
            2,
            "TestNetwork");

    List<HarvestDTO> block5 = harvestRepository
        .findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
            "TestMethodName",
            4,
            "TestNetwork");

    List<HarvestDTO> otherMethod = harvestRepository
        .findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
            "OtherMethodName",
            0,
            "TestNetwork");

    List<HarvestDTO> otherNetwork = harvestRepository
        .findAllByMethodNameAndBlockDateGreaterThanAndNetworkOrderByBlockDate(
            "TestMethodName",
            0,
            "OtherNetwork");

    assertAll(
        () -> assertEquals(allElem.size(), 3, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(three, allElem.get(2)),

        () -> assertEquals(block3.size(), 1, "block3"),
        () -> assertModel(three, block3.get(0)),

        () -> assertEquals(block5.size(), 0, "block5"),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0)),

        () -> assertEquals(otherMethod.size(), 1, "otherMethod"),
        () -> assertModel(five, otherMethod.get(0))
    );
  }

  @Test
  public void test_fetchAllByPeriod() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .fetchAllByPeriod(0, Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> block3to3 = harvestRepository
        .fetchAllByPeriod(2, 3, "TestNetwork");

    List<HarvestDTO> block5toMax = harvestRepository
        .fetchAllByPeriod(5, Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> otherNetwork = harvestRepository
        .fetchAllByPeriod(0, Long.MAX_VALUE, "OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 4, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(five, allElem.get(3)),

        () -> assertEquals(block3to3.size(), 1, "block3to3"),
        () -> assertModel(three, block3to3.get(0)),

        () -> assertEquals(block5toMax.size(), 0, "block5toMax"),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0))
    );
  }

  @Test
  public void test_fetchLatest() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository.fetchLatest("TestNetwork");
    List<HarvestDTO> otherNetwork = harvestRepository.fetchLatest("OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 2, "allElem"),
        () -> assertModel(five, allElem.get(0)),
        () -> assertModel(three, allElem.get(1)),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0))
    );
  }

  @Test
  public void test_findAllByVaultOrderByBlockDate() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .findAllByVaultOrderByBlockDate("TestVault", 0, Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> block2to2 = harvestRepository
        .findAllByVaultOrderByBlockDate("TestVault", 2, 2, "TestNetwork");

    List<HarvestDTO> block5toMax = harvestRepository
        .findAllByVaultOrderByBlockDate("TestVault", 5, Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> otherVault = harvestRepository
        .findAllByVaultOrderByBlockDate("OtherVault", 0, Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> otherNetwork = harvestRepository
        .findAllByVaultOrderByBlockDate("TestVault", 0, Long.MAX_VALUE, "OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 3, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(three, allElem.get(2)),

        () -> assertEquals(block2to2.size(), 1, "block2to2"),
        () -> assertModel(two, block2to2.get(0)),

        () -> assertEquals(block5toMax.size(), 0, "block5toMax"),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0)),

        () -> assertEquals(otherVault.size(), 1, "otherVault"),
        () -> assertModel(five, otherVault.get(0))
    );

    System.out.println();
  }

  @Test
  public void test_fetchAllFromBlockDate() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .fetchAllFromBlockDate(0, "TestNetwork");

    List<HarvestDTO> otherNetwork = harvestRepository
        .fetchAllFromBlockDate(0, "OtherNetwork");

    List<HarvestDTO> withoutRange = harvestRepository
        .fetchAllFromBlockDate(3, "TestNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 3, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(three, allElem.get(2)),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0)),

        () -> assertEquals(withoutRange.size(), 0, "withoutRange")
    );
  }

  @Test
  public void test_findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    HarvestDTO block0 = harvestRepository
        .findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
            "TestVault",
            0,
            "TestNetwork");

    HarvestDTO allElem = harvestRepository
        .findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
            "TestVault",
            Long.MAX_VALUE,
            "TestNetwork");

    HarvestDTO block3 = harvestRepository
        .findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
            "TestVault",
            3,
            "TestNetwork");

    HarvestDTO otherNetwork = harvestRepository
        .findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
            "TestVault",
            Long.MAX_VALUE,
            "OtherNetwork");

    HarvestDTO otherVault = harvestRepository
        .findFirstByVaultAddressAndBlockDateBeforeAndNetworkOrderByBlockDateDesc(
            "OtherVault",
            Long.MAX_VALUE,
            "TestNetwork");

    //then
    assertAll(
        () -> assertNull(block0, "block0"),
        () -> assertModel(three, allElem),
        () -> assertModel(two, block3),
        () -> assertModel(four, otherNetwork),
        () -> assertModel(five, otherVault)
    );
  }

  @Test
  public void test_fetchAllMigration() {
    assertNotNull(harvestRepository.fetchAllMigration());
  }

  @Test
  public void test_fetchAllWithoutOwnerBalance() {
    //given
    first = createDto();
    first.setOwnerBalance(null);
    two = createDto();
    three = createDto();
    three.setOwnerBalanceUsd(null);
    four = createDto();
    four.setNetwork("OtherNetwork");
    four.setOwnerBalanceUsd(null);
    five = createDto();
    five.setOwner("OtherOwner");

    //when
    fillDB();

    List<HarvestDTO> allTestNetwork = harvestRepository
        .fetchAllWithoutOwnerBalance("TestNetwork");

    List<HarvestDTO> allOtherNetwork = harvestRepository
        .fetchAllWithoutOwnerBalance("OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(allTestNetwork.size(), 2, "allTestNetwork"),
        () -> assertModel(first, allTestNetwork.get(0)),
        () -> assertModel(three, allTestNetwork.get(1)),

        () -> assertEquals(allOtherNetwork.size(), 1, "allOtherNetwork"),
        () -> assertModel(four, allOtherNetwork.get(0))
    );
  }

  @Test
  public void test_fetchAllByOwner() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setOwner("OtherOwner");

    //when
    fillDB();

    List<HarvestDTO> block0to0 = harvestRepository
        .fetchAllByOwner("TestOwner", 0, 0, "TestNetwork");

    List<HarvestDTO> allElem = harvestRepository
        .fetchAllByOwner("TestOwner", 0, Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> block2to2 = harvestRepository
        .fetchAllByOwner("TestOwner", 2, 2, "TestNetwork");

    List<HarvestDTO> block6toMax = harvestRepository
        .fetchAllByOwner("TestOwner", 6, Long.MAX_VALUE, "TestNetwork");

    List<HarvestDTO> otherNetwork = harvestRepository
        .fetchAllByOwner("TestOwner", 0, Long.MAX_VALUE, "OtherNetwork");

    List<HarvestDTO> otherOwner = harvestRepository
        .fetchAllByOwner("OtherOwner", 0, Long.MAX_VALUE, "TestNetwork");

    //then
    assertAll(
        () -> assertEquals(block0to0.size(), 0, "block0to0"),

        () -> assertEquals(allElem.size(), 3, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(three, allElem.get(2)),

        () -> assertEquals(block2to2.size(), 1, "block2to2"),
        () -> assertModel(two, block2to2.get(0)),

        () -> assertEquals(block6toMax.size(), 0, "block6toMax"),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0)),

        () -> assertEquals(otherOwner.size(), 1, "otherOwner"),
        () -> assertModel(five, otherOwner.get(0))
    );
  }

  @Test
  public void test_fetchAverageTvl() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<Double> block0to0 = harvestRepository
        .fetchAverageTvl("TestVault", 0, 0, "TestNetwork", limitZeroToFive);

    List<Double> allElem = harvestRepository
        .fetchAverageTvl("TestVault", 0, Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Double> block0to2 = harvestRepository
        .fetchAverageTvl("TestVault", 0, 2, "TestNetwork", limitZeroToFive);

    List<Double> block2to2 = harvestRepository
        .fetchAverageTvl("TestVault", 2, 2, "TestNetwork", limitZeroToFive);

    List<Double> otherNetwork = harvestRepository
        .fetchAverageTvl("TestVault", 0, Long.MAX_VALUE, "OtherNetwork", limitZeroToFive);

    List<Double> otherVault = harvestRepository
        .fetchAverageTvl("OtherVault", 0, Long.MAX_VALUE, "TestNetwork", limitZeroToFive);
//then
    assertAll(
        () -> assertNull(block0to0.get(0), "block0to0"),
        () -> assertEquals(2.0, allElem.get(0), 0.1, "allElem"),
        () -> assertEquals(1.5, block0to2.get(0), 0.1, "block0to2"),
        () -> assertEquals(2.0, block2to2.get(0), 0.1, "block2to2"),
        () -> assertEquals(4.0, otherNetwork.get(0), 0.1, "otherNetwork"),
        () -> assertEquals(5.0, otherVault.get(0), 0.1, "otherVault")
    );
  }

  @Test
  public void test_fetchPeriodOfWork() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");
    six = createDto();
    six.setVaultAddress("OtherVault");
    seven = createDto();
    seven.setNetwork("OtherNetwork");

    //when
    fillDB();

    List<Long> block0 = harvestRepository
        .fetchPeriodOfWork("TestVault", 0, "TestNetwork", limitZeroToFive);

    List<Long> blockMax = harvestRepository
        .fetchPeriodOfWork("TestVault", Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Long> block2 = harvestRepository
        .fetchPeriodOfWork("TestVault", 2, "TestNetwork", limitZeroToFive);

    List<Long> otherNetwork = harvestRepository
        .fetchPeriodOfWork("TestVault", Long.MAX_VALUE, "OtherNetwork", limitZeroToFive);

    List<Long> otherVault = harvestRepository
        .fetchPeriodOfWork("OtherVault", Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    assertAll(
        () -> assertNull(block0.get(0), "block0"),
        () -> assertEquals(2, blockMax.get(0), 0.1, "blockMax"),
        () -> assertEquals(1, block2.get(0), 0.1, "block2"),
        () -> assertEquals(3, otherNetwork.get(0), 0.1, "otherNetwork"),
        () -> assertEquals(1, otherVault.get(0), 0.1, "otherVault")
    );
  }

  @Test
  public void test_findFirstByNetworkOrderByBlockDesc() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    HarvestDTO findInTestNetwork = harvestRepository
        .findFirstByNetworkOrderByBlockDesc("TestNetwork");

    HarvestDTO findInOtherNetwork = harvestRepository
        .findFirstByNetworkOrderByBlockDesc("OtherNetwork");

    //then
    assertAll(
        () -> assertModel(first, findInTestNetwork),
        () -> assertModel(four, findInOtherNetwork)
    );
  }

  @Test
  public void test_fetchAllPoolsUsersQuantity() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");
    six = createDto();
    six.setOwner("OtherOwner");

    //when
    fillDB();

    Integer block0 = harvestRepository
        .fetchAllPoolsUsersQuantity(List.of("TestVault"), 0, "TestNetwork");

    Integer blockMax = harvestRepository
        .fetchAllPoolsUsersQuantity(List.of("TestVault"), Long.MAX_VALUE, "TestNetwork");

    Integer block5 = harvestRepository
        .fetchAllPoolsUsersQuantity(List.of("TestVault"), 5, "TestNetwork");

    Integer otherVault = harvestRepository
        .fetchAllPoolsUsersQuantity(List.of("OtherVault"), Long.MAX_VALUE, "TestNetwork");

    Integer otherNetwork = harvestRepository
        .fetchAllPoolsUsersQuantity(List.of("TestVault"), Long.MAX_VALUE, "OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(0, block0, 0.1, "block0"),
        () -> assertEquals(2, blockMax, 0.1, "blockMax"),
        () -> assertEquals(1, block5, 0.1, "block5"),
        () -> assertEquals(1, otherVault, 0.1, "otherVault"),
        () -> assertEquals(1, otherNetwork, 0.1, "otherNetwork")
    );
  }

  @Test
  public void test_fetchAllUsersQuantity() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    three.setOwner("OtherOwner");
    four = createDto();
    four.setNetwork("OtherNetwork");

    //when
    fillDB();

    Integer block0 = harvestRepository
        .fetchAllUsersQuantity(0, "TestNetwork");

    Integer blockMax = harvestRepository
        .fetchAllUsersQuantity(Long.MAX_VALUE, "TestNetwork");

    Integer block2 = harvestRepository
        .fetchAllUsersQuantity(2, "TestNetwork");

    Integer otherNetwork = harvestRepository
        .fetchAllUsersQuantity(Long.MAX_VALUE, "OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(0, block0, 0.1, "block0"),
        () -> assertEquals(2, blockMax, 0.1, "blockMax"),
        () -> assertEquals(1, block2, 0.1, "block2"),
        () -> assertEquals(1, otherNetwork, 0.1, "otherNetwork")
    );
  }

  @Test
  public void test_fetchActualOwnerQuantity() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");
    six = createDto();
    six.setOwner("OtherOwner");

    //when
    fillDB();

    Integer block0 = harvestRepository
        .fetchActualOwnerQuantity("TestVault", "TestNetwork", 0);

    Integer blockMax = harvestRepository
        .fetchActualOwnerQuantity("TestVault", "TestNetwork", Long.MAX_VALUE);

    Integer block5 = harvestRepository
        .fetchActualOwnerQuantity("TestVault", "TestNetwork", 5);

    Integer otherVault = harvestRepository
        .fetchActualOwnerQuantity("OtherVault", "TestNetwork", Long.MAX_VALUE);

    Integer otherNetwork = harvestRepository
        .fetchActualOwnerQuantity("TestVault", "OtherNetwork", Long.MAX_VALUE);

    assertAll(
        () -> assertEquals(0, block0, 0.1, "block0"),
        () -> assertEquals(2, blockMax, 0.1, "blockMax"),
        () -> assertEquals(1, block5, 0.1, "block5"),
        () -> assertEquals(1, otherVault, 0.1, "otherVault"),
        () -> assertEquals(1, otherNetwork, 0.1, "otherNetwork")
    );
  }

  @Test
  public void test_fetchLastByVaultAndDateNotZero() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    three.setLastUsdTvl(null);
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    HarvestDTO block0 = harvestRepository
        .fetchLastByVaultAndDateNotZero("TestVault", "TestNetwork", 0);

    HarvestDTO block5 = harvestRepository
        .fetchLastByVaultAndDateNotZero("TestVault", "TestNetwork", 5);

    HarvestDTO otherNetwork = harvestRepository
        .fetchLastByVaultAndDateNotZero("TestVault", "OtherNetwork", 5);

    HarvestDTO otherVault = harvestRepository
        .fetchLastByVaultAndDateNotZero("OtherVault", "TestNetwork", 5);

    //then
    assertAll(
        () -> assertNull(block0, "block0"),
        () -> assertModel(two, block5),
        () -> assertModel(four, otherNetwork),
        () -> assertModel(five, otherVault)
    );

  }

  @Test
  public void test_fetchLastByVaultAndDate() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    HarvestDTO block0 = harvestRepository
        .fetchLastByVaultAndDate("TestVault", "TestNetwork", 0);

    HarvestDTO block5 = harvestRepository
        .fetchLastByVaultAndDate("TestVault", "TestNetwork", 5);

    HarvestDTO otherNetwork = harvestRepository
        .fetchLastByVaultAndDate("TestVault", "OtherNetwork", 5);

    HarvestDTO otherVault = harvestRepository
        .fetchLastByVaultAndDate("OtherVault", "TestNetwork", 5);

    //then
    assertAll(
        () -> assertNull(block0, "block0"),
        () -> assertModel(three, block5),
        () -> assertModel(four, otherNetwork),
        () -> assertModel(five, otherVault)
    );
  }

  @Test
  public void test_fetchAllWithoutCounts() {
    //given
    first = createDto();
    two = createDto();
    two.setAllPoolsOwnersCount(2);
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .fetchAllWithoutCounts("TestNetwork");
    List<HarvestDTO> otherNetwork = harvestRepository
        .fetchAllWithoutCounts("OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 3, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(five, allElem.get(2)),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0))
    );

  }

  @Test
  public void test_findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(0, "TestNetwork");

    List<HarvestDTO> block2 = harvestRepository
        .findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(2, "TestNetwork");

    List<HarvestDTO> block5 = harvestRepository
        .findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(5, "TestNetwork");

    List<HarvestDTO> otherNetwork = harvestRepository
        .findAllByBlockDateGreaterThanAndNetworkOrderByBlockDate(0, "OtherNetwork");

//then
    assertAll(
        () -> assertEquals(allElem.size(), 4, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(five, allElem.get(3)),

        () -> assertEquals(block2.size(), 2, "block2"),
        () -> assertModel(three, block2.get(0)),
        () -> assertModel(five, block2.get(1)),

        () -> assertEquals(block5.size(), 0, "block5"),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0))
    );
  }

  @Test
  public void test_findAllByNetworkOrderByBlockDate() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HarvestDTO> allElem = harvestRepository
        .findAllByNetworkOrderByBlockDate("TestNetwork");
    List<HarvestDTO> otherNetwork = harvestRepository
        .findAllByNetworkOrderByBlockDate("OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 4, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(five, allElem.get(3)),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0))
    );
  }

  private HarvestDTO createDto() {
    id++;
    HarvestDTO dto = new HarvestDTO();
    dto.setId(String.valueOf(id));
    dto.setNetwork("TestNetwork");
    dto.setBlockDate((long) id);
    dto.setVaultAddress("TestVault");
    dto.setLastUsdTvl((double) id);
    dto.setOwner("TestOwner");
    dto.setOwnerBalanceUsd((double) id * 10);
    dto.setOwnerBalance((double) id * 10);
    dto.setMethodName("TestMethodName");
    dto.setUsdAmount((long) id);
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

  private void deleteFromDB(HarvestDTO dto) {
    if (dto != null) {
      harvestRepository.delete(dto);
    }
  }

  private void saveToDB(HarvestDTO dto) {
    if (dto != null) {
      harvestRepository.save(dto);
    }
  }
}
