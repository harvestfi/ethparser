package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.belbix.ethparser.TestUtils.assertModel;

import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.RewardDTO;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RewardsRepositoryTest {

  @Autowired
  private RewardsRepository rewardsRepository;

  private int id = 0;
  RewardDTO first;
  RewardDTO two;
  RewardDTO three;
  RewardDTO four;
  RewardDTO five;
  RewardDTO six;

  @AfterEach
  public void clearDbAfterTest(){
    clearDB();
  }

  @Test
  public void test_fetchAllWithoutAddresses() {
    assertNotNull(rewardsRepository.fetchAllWithoutAddresses());
  }

  @Test
  public void test_getAllByVaultOrderByBlockDate() {
    // given
    first = createDto("firstVaultAddress");
    two = createDto("firstVaultAddress");
    three = createDto("twoVaultAddress");
    four = createDto("twoVaultAddress");
    four.setNetwork("NetworkDifferent");
    five = createDto("firstVaultAddress");
    six = createDto("firstVaultAddress");

    //when
    fillDB();

    List<RewardDTO> expectedTestFirstVaultAddress =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            "firstVaultAddress",
            0,
            Long.MAX_VALUE,
            "TestNetwork");

    List<RewardDTO> expectedTestTwoVaultAddress =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            "twoVaultAddress",
            0,
            Long.MAX_VALUE,
            "TestNetwork");

    List<RewardDTO> expectedFourElement =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            "twoVaultAddress",
            0,
            Long.MAX_VALUE,
            "NetworkDifferent");

    List<RewardDTO> expectedOneAndTwoElements =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            "firstVaultAddress",
            0,
            2L,
            "TestNetwork");

    List<RewardDTO> expectedAtLeastBlock5LElements =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            "firstVaultAddress",
            5L,
            Long.MAX_VALUE,
            "TestNetwork");

    List<RewardDTO> expectedOnlyFiveElements =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            "firstVaultAddress",
            5,
            5L,
            "TestNetwork");

    List<RewardDTO> expectedWithoutRange =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            "firstVaultAddress",
            7L,
            Long.MAX_VALUE,
            "TestNetwork");

    List<RewardDTO> expectedNullNetworkAndAddress =
        rewardsRepository.getAllByVaultOrderByBlockDate(
            null,
            0,
            Long.MAX_VALUE,
            null);

    //then
    assertAll(
        () -> assertEquals(expectedTestFirstVaultAddress.size(), 4,
            "expectedTestFirstVaultAddress"),
        () -> assertModel(first, expectedTestFirstVaultAddress.get(0)),
        () -> assertModel(six, expectedTestFirstVaultAddress.get(3)),

        () -> assertEquals(expectedTestTwoVaultAddress.size(), 1,
            "expectedTestTwoVaultAddress"),
        () -> assertModel(three, expectedTestTwoVaultAddress.get(0)),

        () -> assertEquals(expectedFourElement.size(), 1,
            "expectedFourElement"),
        () -> assertModel(four, expectedFourElement.get(0)),

        () -> assertEquals(expectedOneAndTwoElements.size(), 2,
            "expectedOneAndTwoElements"),
        () -> assertModel(first, expectedOneAndTwoElements.get(0)),
        () -> assertModel(two, expectedOneAndTwoElements.get(1)),

        () -> assertEquals(expectedAtLeastBlock5LElements.size(), 2,
            "expectedAtLeastBlock5LElements"),
        () -> assertModel(five, expectedAtLeastBlock5LElements.get(0)),
        () -> assertModel(six, expectedAtLeastBlock5LElements.get(1)),

        () -> assertEquals(expectedOnlyFiveElements.size(), 1,
            "expectedOnlyFiveElements"),
        () -> assertModel(five, expectedOnlyFiveElements.get(0)),

        () -> assertEquals(expectedNullNetworkAndAddress.size(), 0,
            "expectedNullNetworkAndAddress"),

        () -> assertEquals(expectedWithoutRange.size(), 0,
            "expectedWithoutRange")
    );
  }

  @Test
  public void test_fetchLastRewards() {
    // given
    first = createDto("firstVaultAddress");
    two = createDto("firstVaultAddress");
    three = createDto("twoVaultAddress");
    four = createDto("twoVaultAddress");
    four.setNetwork("NetworkDifferent");

    //when
    fillDB();

    List<RewardDTO> fetchLastRewards = rewardsRepository.fetchLastRewards("TestNetwork");
    List<RewardDTO> differentNetwork = rewardsRepository.fetchLastRewards("NetworkDifferent");

    //then
    assertAll(
        () -> assertEquals(fetchLastRewards.size(), 2, "fetchLastRewards"),
        () -> assertModel(two, fetchLastRewards.get(0)),
        () -> assertModel(three, fetchLastRewards.get(1)),

        () -> assertEquals(differentNetwork.size(), 1, "differentNetwork"),
        () -> assertModel(four, differentNetwork.get(0))
    );

    Assertions.assertThrows(InvalidDataAccessResourceUsageException.class, () -> {
      rewardsRepository.fetchLastRewards(null);
    });
  }

  @Test
  public void test_fetchRewardsByVaultAfterBlockDate() {
    // given
    first = createDto("firstVaultAddress");
    two = createDto("firstVaultAddress");
    three = createDto("twoVaultAddress");
    four = createDto("twoVaultAddress");
    four.setNetwork("NetworkDifferent");
    five = createDto("firstVaultAddress");

    //when
    fillDB();

    List<RewardDTO> expectedFirstVaultAddress =
        rewardsRepository.fetchRewardsByVaultAfterBlockDate(
            "firstVaultAddress",
            0,
            Long.MAX_VALUE,
            "TestNetwork");

    List<RewardDTO> expectedTwoVaultAddress =
        rewardsRepository.fetchRewardsByVaultAfterBlockDate(
            "twoVaultAddress",
            0,
            Long.MAX_VALUE,
            "TestNetwork");

    List<RewardDTO> expectedFourElement =
        rewardsRepository.fetchRewardsByVaultAfterBlockDate(
            "twoVaultAddress",
            0,
            Long.MAX_VALUE,
            "NetworkDifferent");

    List<RewardDTO> expectedOneAndTwoElements =
        rewardsRepository.fetchRewardsByVaultAfterBlockDate(
            "firstVaultAddress",
            0,
            2L,
            "TestNetwork");

    List<RewardDTO> expectedOnlyFiveElements =
        rewardsRepository.fetchRewardsByVaultAfterBlockDate(
            "firstVaultAddress",
            5,
            5L,
            "TestNetwork");

    List<RewardDTO> expectedNullNetworkAndAddress =
        rewardsRepository.fetchRewardsByVaultAfterBlockDate(
            null,
            0,
            Long.MAX_VALUE,
            null);

    //then
    assertAll(
        () -> assertEquals(expectedFirstVaultAddress.size(), 3, "expectedFirstVaultAddress"),
        () -> assertModel(first, expectedFirstVaultAddress.get(0)),
        () -> assertModel(five, expectedFirstVaultAddress.get(2)),

        () -> assertEquals(expectedTwoVaultAddress.size(), 1, "expectedTwoVaultAddress"),
        () -> assertModel(three, expectedTwoVaultAddress.get(0)),

        () -> assertEquals(expectedFourElement.size(), 1, "expectedTwoVaultAddress"),
        () -> assertModel(four, expectedFourElement.get(0)),

        () -> assertEquals(expectedOneAndTwoElements.size(), 2, "expectedOneAndTwoElements"),
        () -> assertModel(first, expectedOneAndTwoElements.get(0)),
        () -> assertModel(two, expectedOneAndTwoElements.get(1)),

        () -> assertEquals(expectedOnlyFiveElements.size(), 1, "expectedOnlyFiveElements"),
        () -> assertModel(five, expectedOnlyFiveElements.get(0)),

        () -> assertEquals(expectedNullNetworkAndAddress.size(), 0, "expectedNullNetworkAndAddress")
    );
  }

  @Test
  public void test_testExpectedJpaSystemException() {
    RewardDTO rewardDTO = new RewardDTO();
    Assertions.assertThrows(JpaSystemException.class, () -> {
      rewardsRepository.save(rewardDTO);
    });
  }

  @Test
  public void test_getFirstByVaultAddressAndNetworkOrderByBlockDateDesc() {
    // given
    first = createDto("firstVaultAddress");
    two = createDto("firstVaultAddress");
    three = createDto("twoVaultAddress");
    four = createDto("twoVaultAddress");
    four.setNetwork("NetworkDifferent");

    //when
    fillDB();

    RewardDTO expectedTwo = rewardsRepository
        .getFirstByVaultAddressAndNetworkOrderByBlockDateDesc(
            "firstVaultAddress",
            "TestNetwork");


    RewardDTO expectedFour = rewardsRepository
        .getFirstByVaultAddressAndNetworkOrderByBlockDateDesc(
            "twoVaultAddress",
            "NetworkDifferent");

    RewardDTO expectedNull = rewardsRepository
        .getFirstByVaultAddressAndNetworkOrderByBlockDateDesc(
            "firstVaultAddress",
            "NetworkDifferent");

    RewardDTO expectedNull2 = rewardsRepository
        .getFirstByVaultAddressAndNetworkOrderByBlockDateDesc(
            null,
            "NetworkDifferent");

    //then
    assertAll(
        () -> assertModel(two, expectedTwo),
        () -> assertModel(four, expectedFour),

        () -> assertNull(expectedNull),
        () -> assertNull(expectedNull2)
    );
  }

  @Test
  public void test_fetchAllByRange() {
    // given
    first = createDto("twoVaultAddress");
    two = createDto("twoVaultAddress");
    three = createDto("twoVaultAddress");
    four = createDto("twoVaultAddress");
    four.setNetwork("TestNetworkDifferent");
    five = createDto("twoVaultAddress");

    //when
    fillDB();

    List<RewardDTO> differentNetworkList = rewardsRepository
        .fetchAllByRange(0, Long.MAX_VALUE, "TestNetworkDifferent");

    List<RewardDTO> fetchAll = rewardsRepository
        .fetchAllByRange(0, Long.MAX_VALUE, "TestNetwork");

    List<RewardDTO> fetchFirstAndTwo = rewardsRepository
        .fetchAllByRange(0, 2L, "TestNetwork");

    List<RewardDTO> fetchAtLeast2L = rewardsRepository
        .fetchAllByRange(2L, Long.MAX_VALUE, "TestNetwork");

    List<RewardDTO> fetchOnlyFive = rewardsRepository
        .fetchAllByRange(5L, 5L, "TestNetwork");

    List<RewardDTO> fetchEmptyList = rewardsRepository
        .fetchAllByRange(6L, 7L, "TestNetwork");

    List<RewardDTO> fetchWrongSequence = rewardsRepository
        .fetchAllByRange(5L, 2L, "TestNetwork");

    //then
    assertAll(
        () -> assertEquals(differentNetworkList.size(), 1, "differentNetworkList"),
        () -> assertModel(four, differentNetworkList.get(0)),

        () -> assertEquals(fetchEmptyList.size(), 0, "fetchEmptyList"),
        () -> assertEquals(fetchWrongSequence.size(), 0, "fetchWrongSequence"),

        () -> assertEquals(fetchAll.size(), 4, "fetchAll"),
        () -> assertModel(first, fetchAll.get(0)),
        () -> assertModel(five, fetchAll.get(3)),

        () -> assertEquals(fetchFirstAndTwo.size(), 2, "fetchFirstAndTwo"),
        () -> assertModel(first, fetchFirstAndTwo.get(0)),
        () -> assertModel(two, fetchFirstAndTwo.get(1)),

        () -> assertEquals(fetchAtLeast2L.size(), 3, "fetchAtLeast2L"),
        () -> assertModel(two, fetchAtLeast2L.get(0)),
        () -> assertModel(five, fetchAtLeast2L.get(2)),

        () -> assertEquals(fetchOnlyFive.size(), 1, "fetchOnlyFive"),
        () -> assertModel(five, fetchOnlyFive.get(0))
    );
  }

  private RewardDTO createDto(String vaultAddress) {
    id++;
    RewardDTO dto = new RewardDTO();
    dto.setId(String.valueOf(id));
    dto.setNetwork("TestNetwork");
    dto.setVaultAddress(vaultAddress);
    dto.setBlockDate(id);
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

    first = null;
    two = null;
    three = null;
    four = null;
    five = null;
    six = null;
  }

  private void fillDB() {
    id = 0;
    saveToDB(first);
    saveToDB(two);
    saveToDB(three);
    saveToDB(four);
    saveToDB(five);
    saveToDB(six);
  }

  private void deleteFromDB(RewardDTO dto) {
    if (dto != null) {
      rewardsRepository.delete(dto);
    }
  }

  private void saveToDB(RewardDTO dto) {
    if (dto != null) {
      rewardsRepository.save(dto);
    }
  }
}
