package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static pro.belbix.ethparser.TestUtils.assertModel;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

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
import pro.belbix.ethparser.dto.v0.HardWorkDTO;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HardWorkRepositoryTest {

  private final Pageable limitOne = PageRequest.of(0, 1);
  private final Pageable limitZeroToFive = PageRequest.of(0, 5);

  private int id = 0;

  private HardWorkDTO first;
  private HardWorkDTO two;
  private HardWorkDTO three;
  private HardWorkDTO four;
  private HardWorkDTO five;

  @AfterEach
  public void clearDBBAfterTest() {
    clearDB();
  }

  @Autowired
  private HardWorkRepository hardWorkRepository;

  @Test
  public void test_fetchAllWithoutAddresses() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    three.setVaultAddress("");
    four = createDto();
    four.setVaultAddress(null);
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<HardWorkDTO> fetchedData = hardWorkRepository.fetchAllWithoutAddresses();

    //then
    assertAll(
        () -> assertEquals(fetchedData.size(), 2, "fetchedData"),
        () -> assertModel(three, fetchedData.get(0)),
        () -> assertModel(four, fetchedData.get(1))
    );
  }

  @Test
  public void test_fetchPreviousBlockDateByVaultAndDate() {
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

    Long blockMax = hardWorkRepository
        .fetchPreviousBlockDateByVaultAndDate("TestVault", "TestNetwork", Long.MAX_VALUE);

    Long block3 = hardWorkRepository
        .fetchPreviousBlockDateByVaultAndDate("TestVault", "TestNetwork", 3);

    Long otherVault = hardWorkRepository
        .fetchPreviousBlockDateByVaultAndDate("OtherVault", "TestNetwork", Long.MAX_VALUE);

    Long otherNetwork = hardWorkRepository
        .fetchPreviousBlockDateByVaultAndDate("TestVault", "OtherNetwork", Long.MAX_VALUE);

    Long block0 = hardWorkRepository
        .fetchPreviousBlockDateByVaultAndDate("TestVault", "TestNetwork", 0);

    //then
    assertAll(
        () -> assertEquals(3, blockMax.longValue(), "blockMax"),
        () -> assertEquals(2, block3.longValue(), "block3"),
        () -> assertEquals(5, otherVault.longValue(), "otherVault"),
        () -> assertEquals(4, otherNetwork.longValue(), "otherNetwork"),
        () -> assertNull(block0, "block0")
    );
  }

  @Test
  public void test_fetchLastGasSaved() {
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

    Double fetchTestNetwork = hardWorkRepository.fetchLastGasSaved("TestNetwork");
    Double fetchOtherNetwork = hardWorkRepository.fetchLastGasSaved("OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(5.0, fetchTestNetwork, 0.1, "fetchTestNetwork"),
        () -> assertEquals(4.0, fetchOtherNetwork, 0.1, "fetchOtherNetwork")
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

    List<HardWorkDTO> fetchTestNetwork = hardWorkRepository.fetchLatest("TestNetwork");
    List<HardWorkDTO> fetchOtherNetwork = hardWorkRepository.fetchLatest("OtherNetwork");

    //then
    assertAll(
        () -> assertEquals(fetchTestNetwork.size(), 2, "fetchTestNetwork"),
        () -> assertModel(five, fetchTestNetwork.get(0)),
        () -> assertModel(three, fetchTestNetwork.get(1)),

        () -> assertEquals(fetchOtherNetwork.size(), 1, "fetchOtherNetwork"),
        () -> assertModel(four, fetchOtherNetwork.get(0))
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

    List<HardWorkDTO> allElements = hardWorkRepository
        .findAllByVaultOrderByBlockDate("TestVault", "TestNetwork", 0, Long.MAX_VALUE);

    List<HardWorkDTO> onlyTwo = hardWorkRepository
        .findAllByVaultOrderByBlockDate("TestVault", "TestNetwork", 2, 2);

    List<HardWorkDTO> otherNetwork = hardWorkRepository
        .findAllByVaultOrderByBlockDate("TestVault", "OtherNetwork", 0, Long.MAX_VALUE);

    List<HardWorkDTO> otherVault = hardWorkRepository
        .findAllByVaultOrderByBlockDate("OtherVault", "TestNetwork", 0, Long.MAX_VALUE);

    List<HardWorkDTO> withoutRange = hardWorkRepository
        .findAllByVaultOrderByBlockDate("TestVault", "TestNetwork", 5, Long.MAX_VALUE);

    //then
    assertAll(
        () -> assertEquals(allElements.size(), 3, "allElements"),
        () -> assertModel(first, allElements.get(0)),
        () -> assertModel(three, allElements.get(2)),

        () -> assertEquals(onlyTwo.size(), 1, "onlyTwo"),
        () -> assertModel(two, onlyTwo.get(0)),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0)),

        () -> assertEquals(otherVault.size(), 1, "otherVault"),
        () -> assertModel(five, otherVault.get(0)),

        () -> assertEquals(withoutRange.size(), 0, "withoutRange")
    );
  }

  @Test
  public void test_sumSavedGasFees() {
    //given
    first = createDto();
    first.setSavedGasFees(1.1);
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    Double allElem = hardWorkRepository
        .sumSavedGasFees("TestVault", "TestNetwork", Long.MAX_VALUE);
    Double block3 = hardWorkRepository
        .sumSavedGasFees("TestVault", "TestNetwork", 3);
    Double otherNetwork = hardWorkRepository
        .sumSavedGasFees("TestVault", "OtherNetwork", Long.MAX_VALUE);
    Double otherVault = hardWorkRepository
        .sumSavedGasFees("OtherVault", "TestNetwork", Long.MAX_VALUE);
    Double withoutRange = hardWorkRepository
        .sumSavedGasFees("TestVault", "TestNetwork", 0);

    //then
    assertAll(
        () -> assertEquals(6.1, allElem, 0.1, "allElem"),
        () -> assertEquals(3.1, block3, 0.1, "block3"),
        () -> assertEquals(4, otherNetwork, 0.1, "otherNetwork"),
        () -> assertEquals(5, otherVault, 0.1, "otherVault"),
        () -> assertNull(withoutRange, "withoutRange")
    );
  }

  @Test
  public void test_countAtBlockDate() {
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

    Integer allElem = hardWorkRepository
        .countAtBlockDate("TestVault", "TestNetwork", Long.MAX_VALUE);
    Integer block2 = hardWorkRepository
        .countAtBlockDate("TestVault", "TestNetwork", 2);
    Integer otherNetwork = hardWorkRepository
        .countAtBlockDate("TestVault", "OtherNetwork", Long.MAX_VALUE);
    Integer otherVault = hardWorkRepository
        .countAtBlockDate("OtherVault", "TestNetwork", Long.MAX_VALUE);
    Integer withoutRange = hardWorkRepository
        .countAtBlockDate("TestVault", "TestNetwork", 0);

    //then
    assertAll(
        () -> assertEquals(3, allElem, 0.1, "allElem"),
        () -> assertEquals(2, block2, 0.1, "block2"),
        () -> assertEquals(1, otherNetwork, 0.1, "otherNetwork"),
        () -> assertEquals(1, otherVault, 0.1, "otherVault"),
        () -> assertEquals(0, withoutRange, 0.1, "withoutRange")
    );
  }

  @Test
  public void test_fetchAllBuybacksAtDate() {
    //given
    first = createDto();
    first.setFullRewardUsd(1.1);
    first.setFarmBuyback(1.1);
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<Double> allElem = hardWorkRepository
        .fetchAllBuybacksAtDate(Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Double> block3 = hardWorkRepository
        .fetchAllBuybacksAtDate(3, "TestNetwork", limitZeroToFive);

    List<Double> otherNetwork = hardWorkRepository
        .fetchAllBuybacksAtDate(Long.MAX_VALUE, "OtherNetwork", limitZeroToFive);

    List<Double> withoutRange = hardWorkRepository
        .fetchAllBuybacksAtDate(0, "TestNetwork", limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(11.1, allElem.get(0), 0.1, "allElem"),
        () -> assertEquals(3.1, block3.get(0), 0.1, "block3"),
        () -> assertEquals(4.0, otherNetwork.get(0), 0.1, "otherNetwork"),
        () -> assertNull(withoutRange.get(0), "withoutRange")
    );
  }

  @Test
  public void test_fetchAllProfitForPeriod() {
    //given
    first = createDto();
    first.setFullRewardUsd(1.1);
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<Double> allElem = hardWorkRepository
        .fetchAllProfitForPeriod(0, Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Double> onlyTwo = hardWorkRepository
        .fetchAllProfitForPeriod(2, 2, "TestNetwork", limitZeroToFive);

    List<Double> otherNetwork = hardWorkRepository
        .fetchAllProfitForPeriod(0, Long.MAX_VALUE, "OtherNetwork", limitZeroToFive);

    List<Double> withoutRange = hardWorkRepository
        .fetchAllProfitForPeriod(6, Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(11.1, allElem.get(0), 0.1, "allElem"),
        () -> assertEquals(2, onlyTwo.get(0), 0.1, "onlyTwo"),
        () -> assertEquals(4.0, otherNetwork.get(0), 0.1, "otherNetwork"),
        () -> assertNull(withoutRange.get(0), "withoutRange")
    );
  }

  @Test
  public void test_fetchAllProfitAtDate() {
    //given
    first = createDto();
    first.setFullRewardUsd(1.1);
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<Double> allElem = hardWorkRepository
        .fetchAllProfitAtDate(Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Double> block2 = hardWorkRepository
        .fetchAllProfitAtDate(2, "TestNetwork", limitZeroToFive);

    List<Double> otherNetwork = hardWorkRepository
        .fetchAllProfitAtDate(Long.MAX_VALUE, "OtherNetwork", limitZeroToFive);

    List<Double> withoutRange = hardWorkRepository
        .fetchAllProfitAtDate(0, "TestNetwork", limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(11.1, allElem.get(0), 0.1, "allElem"),
        () -> assertEquals(3.1, block2.get(0), 0.1, "block2"),
        () -> assertEquals(4.0, otherNetwork.get(0), 0.1, "otherNetwork"),
        () -> assertNull(withoutRange.get(0), "withoutRange")
    );
  }

  @Test
  public void test_fetchProfitForPeriod() {
    //given
    first = createDto();
    first.setFullRewardUsd(1.1);
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    List<Double> allElem = hardWorkRepository
        .fetchProfitForPeriod("TestVault", 0, Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Double> onlyTwo = hardWorkRepository
        .fetchProfitForPeriod("TestVault", 2, 2, "TestNetwork", limitZeroToFive);

    List<Double> otherNetwork = hardWorkRepository
        .fetchProfitForPeriod("TestVault", 0, Long.MAX_VALUE, "OtherNetwork", limitZeroToFive);

    List<Double> otherVault = hardWorkRepository
        .fetchProfitForPeriod("OtherVault", 0, Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Double> withoutRange = hardWorkRepository
        .fetchProfitForPeriod("TestVault", 6, Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(6.0, allElem.get(0), 0.1, "allElem"),
        () -> assertEquals(2.0, onlyTwo.get(0), 0.1, "onlyTwo"),
        () -> assertEquals(4.0, otherNetwork.get(0), 0.1, "otherNetwork"),
        () -> assertEquals(5.0, otherVault.get(0), 0.1, "otherVault"),
        () -> assertNull(withoutRange.get(0), "withoutRange")
    );
  }

  @Test
  public void test_fetchPercentForPeriod() {
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

    List<Double> allElem = hardWorkRepository.
        fetchPercentForPeriod("TestVault", Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    List<Double> onlyFirst = hardWorkRepository.
        fetchPercentForPeriod("TestVault", 1, "TestNetwork", limitZeroToFive);

    List<Double> otherNetwork = hardWorkRepository.
        fetchPercentForPeriod("TestVault", Long.MAX_VALUE, "OtherNetwork", limitZeroToFive);

    List<Double> otherVault = hardWorkRepository.
        fetchPercentForPeriod("OtherVault", Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(6.0, allElem.get(0), 0.1, "allElem"),
        () -> assertEquals(1.0, onlyFirst.get(0), 0.1, "onlyFirst"),
        () -> assertEquals(4.0, otherNetwork.get(0), 0.1, "otherNetwork"),
        () -> assertEquals(5.0, otherVault.get(0), 0.1, "otherVault")
    );
  }

  @Test
  public void test_getSumForVault() {
    //given
    first = createDto();
    first.setFullRewardUsd(1.1);
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setVaultAddress("OtherVault");

    //when
    fillDB();

    Double allElem = hardWorkRepository
        .getSumForVault("TestVault", Long.MAX_VALUE, "TestNetwork");

    Double block2 = hardWorkRepository
        .getSumForVault("TestVault", 2, "TestNetwork");

    Double otherNetwork = hardWorkRepository
        .getSumForVault("TestVault", Long.MAX_VALUE, "OtherNetwork");

    Double otherVault = hardWorkRepository
        .getSumForVault("OtherVault", Long.MAX_VALUE, "TestNetwork");

    Double withoutRange = hardWorkRepository
        .getSumForVault("TestVault", 0, "TestNetwork");

    //then
    assertAll(
        () -> assertEquals(6.1, allElem, 0.1, "allElem"),
        () -> assertEquals(3.1, block2, 0.1, "block2"),
        () -> assertEquals(4.0, otherNetwork, 0.1, "otherNetwork"),
        () -> assertEquals(5.0, otherVault, 0.1, "otherVault"),
        () -> assertNull(withoutRange, "withoutRange")
    );
  }

  @Test
  public void test_fetchAllInRange() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();

    //when
    fillDB();

    List<HardWorkDTO> allElem = hardWorkRepository
        .fetchAllInRange(0, Long.MAX_VALUE, "TestNetwork");

    List<HardWorkDTO> otherNetwork = hardWorkRepository
        .fetchAllInRange(0, Long.MAX_VALUE, "OtherNetwork");

    List<HardWorkDTO> onlyTwoElem = hardWorkRepository
        .fetchAllInRange(2, 2, "TestNetwork");

    List<HardWorkDTO> withoutRange = hardWorkRepository
        .fetchAllInRange(6, Long.MAX_VALUE, "TestNetwork");

    //then
    assertAll(
        () -> assertEquals(allElem.size(), 4, "allElem"),
        () -> assertModel(first, allElem.get(0)),
        () -> assertModel(five, allElem.get(3)),

        () -> assertEquals(otherNetwork.size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.get(0)),

        () -> assertEquals(onlyTwoElem.size(), 1, "onlyTwoElem"),
        () -> assertModel(two, onlyTwoElem.get(0)),

        () -> assertEquals(withoutRange.size(), 0, "withoutRange")
    );
  }

  @Test
  public void test_findFirstByNetworkOrderByBlockDateDesc() {
    //given
    first = createDto();
    two = createDto();
    three = createDto();
    four = createDto();
    four.setNetwork("OtherNetwork");
    five = createDto();
    five.setNetwork(null);

    //when
    fillDB();

    HardWorkDTO threeElem = hardWorkRepository
        .findFirstByNetworkOrderByBlockDateDesc("TestNetwork");

    HardWorkDTO fourElem = hardWorkRepository
        .findFirstByNetworkOrderByBlockDateDesc("OtherNetwork");

    HardWorkDTO fiveElem = hardWorkRepository
        .findFirstByNetworkOrderByBlockDateDesc(null);

    //then
    assertAll(
        () -> assertModel(three, threeElem),
        () -> assertModel(four, fourElem),
        () -> assertModel(five, fiveElem)
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

    Page<HardWorkDTO> allElements = hardWorkRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 0, limitZeroToFive);

    Page<HardWorkDTO> otherNetwork = hardWorkRepository
        .fetchPagesByVault("TestVault", "OtherNetwork", 0, limitZeroToFive);

    Page<HardWorkDTO> minAmount2 = hardWorkRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 2, limitZeroToFive);

    Page<HardWorkDTO> minAmount5 = hardWorkRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 5, limitZeroToFive);

    Page<HardWorkDTO> onlyFirstElement = hardWorkRepository
        .fetchPagesByVault("TestVault", "TestNetwork", 0, limitOne);

    Page<HardWorkDTO> otherVault = hardWorkRepository
        .fetchPagesByVault("OtherVault", "TestNetwork", 0, limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(allElements.getContent().size(), 3, "allElements"),
        () -> assertModel(first, allElements.getContent().get(0)),
        () -> assertModel(three, allElements.getContent().get(2)),

        () -> assertEquals(otherNetwork.getContent().size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.getContent().get(0)),

        () -> assertEquals(minAmount2.getContent().size(), 2, "minAmount2"),
        () -> assertModel(two, minAmount2.getContent().get(0)),
        () -> assertModel(three, minAmount2.getContent().get(1)),

        () -> assertEquals(minAmount5.getContent().size(), 0, "minAmount5"),

        () -> assertEquals(onlyFirstElement.getContent().size(), 1, "onlyFirstElement"),
        () -> assertModel(first, onlyFirstElement.getContent().get(0)),

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

    //when
    fillDB();

    Page<HardWorkDTO> allElements = hardWorkRepository
        .fetchPages(0, "TestNetwork", limitZeroToFive);

    Page<HardWorkDTO> otherNetwork = hardWorkRepository
        .fetchPages(0, "OtherNetwork", limitZeroToFive);

    Page<HardWorkDTO> minAmount2 = hardWorkRepository
        .fetchPages(2, "TestNetwork", limitZeroToFive);

    Page<HardWorkDTO> minAmount5 = hardWorkRepository
        .fetchPages(5, "TestNetwork", limitZeroToFive);

    Page<HardWorkDTO> onlyFirstElement = hardWorkRepository
        .fetchPages(0, "TestNetwork", limitOne);

    //then
    assertAll(
        () -> assertEquals(allElements.getContent().size(), 3, "allElements"),
        () -> assertModel(first, allElements.getContent().get(0)),
        () -> assertModel(three, allElements.getContent().get(2)),

        () -> assertEquals(otherNetwork.getContent().size(), 1, "otherNetwork"),
        () -> assertModel(four, otherNetwork.getContent().get(0)),

        () -> assertEquals(minAmount2.getContent().size(), 2, "minAmount2"),
        () -> assertModel(two, minAmount2.getContent().get(0)),
        () -> assertModel(three, minAmount2.getContent().get(1)),

        () -> assertEquals(minAmount5.getContent().size(), 0, "minAmount5"),

        () -> assertEquals(onlyFirstElement.getContent().size(), 1, "onlyFirstElement"),
        () -> assertModel(first, onlyFirstElement.getContent().get(0))
    );
  }

  private HardWorkDTO createDto() {
    id++;
    HardWorkDTO dto = new HardWorkDTO();
    dto.setId(String.valueOf(id));
    dto.setNetwork("TestNetwork");
    dto.setBlockDate(id);
    dto.setFullRewardUsd(id);
    dto.setVaultAddress("TestVault");
    dto.setPerc(id);
    dto.setFarmBuyback(id);
    dto.setSavedGasFees(id);
    dto.setSavedGasFeesSum(id);
    return dto;
  }

  private void clearDB() {
    id = 0;
    deleteFromDB(first);
    deleteFromDB(two);
    deleteFromDB(three);
    deleteFromDB(four);
    deleteFromDB(five);

    first = null;
    two = null;
    three = null;
    four = null;
    five = null;
  }

  private void fillDB() {
    id = 0;
    saveToDB(first);
    saveToDB(two);
    saveToDB(three);
    saveToDB(four);
    saveToDB(five);
  }

  private void deleteFromDB(HardWorkDTO dto) {
    if (dto != null) {
      hardWorkRepository.delete(dto);
    }
  }

  private void saveToDB(HardWorkDTO dto) {
    if (dto != null) {
      hardWorkRepository.save(dto);
    }
  }
}
