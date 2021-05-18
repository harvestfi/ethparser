package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.belbix.ethparser.TestUtils.assertModel;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.v0.HarvestTvlEntity;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class HarvestTvlRepositoryTest {

  @Autowired
  private HarvestTvlRepository harvestTvlRepository;

  private int id = 0;

  @Test
  public void test_testExpectedJpaSystemException() {
    HarvestTvlEntity harvestTvlEntity = new HarvestTvlEntity();
    Assertions.assertThrows(JpaSystemException.class, () -> {
      harvestTvlRepository.save(harvestTvlEntity);
    });
  }

  @Test
  public void test_getHistoryOfAllTvl() {
    // given
    id = 0;
    HarvestTvlEntity first = createDto(id);
    HarvestTvlEntity two = createDto(id);
    HarvestTvlEntity three = createDto(id);
    HarvestTvlEntity four = createDto(id);
    HarvestTvlEntity five = createDto(id);

    //when
    harvestTvlRepository.save(first);
    harvestTvlRepository.save(two);
    harvestTvlRepository.save(three);
    harvestTvlRepository.save(four);
    harvestTvlRepository.save(five);

    List<HarvestTvlEntity> expectedAllElements = harvestTvlRepository
        .getHistoryOfAllTvl(0, Long.MAX_VALUE, "TestNetwork");

    List<HarvestTvlEntity> expectedThreeElements = harvestTvlRepository
        .getHistoryOfAllTvl(0, 3333333333L, "TestNetwork");

    List<HarvestTvlEntity> expectedTwoLastElements = harvestTvlRepository
        .getHistoryOfAllTvl(4444444444L, Long.MAX_VALUE, "TestNetwork");

    List<HarvestTvlEntity> expectedOnlyElementTwo = harvestTvlRepository
        .getHistoryOfAllTvl(2222222222L, 2222222222L, "TestNetwork");

    List<HarvestTvlEntity> expectedEmpty = harvestTvlRepository
        .getHistoryOfAllTvl(0, 1111111110L, "TestNetwork");

    //then
    assertAll(
        () -> assertEquals(expectedAllElements.size(), 5),
        () -> assertModel(first, expectedAllElements.get(0)),
        () -> assertModel(five, expectedAllElements.get(4)),

        () -> assertEquals(expectedThreeElements.size(), 3),
        () -> assertModel(first, expectedThreeElements.get(0)),
        () -> assertModel(three, expectedThreeElements.get(2)),

        () -> assertEquals(expectedTwoLastElements.size(), 2),
        () -> assertModel(four, expectedTwoLastElements.get(0)),
        () -> assertModel(five, expectedTwoLastElements.get(1)),

        () -> assertEquals(expectedOnlyElementTwo.size(), 1),
        () -> assertModel(two, expectedOnlyElementTwo.get(0)),

        () -> assertEquals(expectedEmpty.size(), 0)
    );

    //after
    id = 0;
    harvestTvlRepository.delete(first);
    harvestTvlRepository.delete(two);
    harvestTvlRepository.delete(three);
    harvestTvlRepository.delete(four);
    harvestTvlRepository.delete(five);

  }

  @Test
  public void test_findAllByOrderByCalculateTime() {
    // given
    id = 0;

    HarvestTvlEntity first = createDto(3);
    HarvestTvlEntity two = createDto(2);
    HarvestTvlEntity three = createDto(1);
    HarvestTvlEntity four = createDto(1);

    //when
    harvestTvlRepository.save(two);
    harvestTvlRepository.save(first);
    harvestTvlRepository.save(three);
    harvestTvlRepository.save(four);

    List<HarvestTvlEntity> result = harvestTvlRepository
        .findAllByNetworkOrderByCalculateTime("TestNetwork");

    //then
    assertAll(
        () -> assertEquals(result.size(), 4),
        () -> assertModel(first, result.get(3)),
        () -> assertModel(two, result.get(2)),
        () -> assertModel(three, result.get(0)),
        () -> assertModel(four, result.get(1))
    );

    //after
    id=0;
    harvestTvlRepository.delete(two);
    harvestTvlRepository.delete(first);
    harvestTvlRepository.delete(three);
    harvestTvlRepository.delete(four);
  }

  private HarvestTvlEntity createDto(int calculateTime) {
    id++;
    HarvestTvlEntity dto = new HarvestTvlEntity();
    dto.setCalculateHash(String.valueOf(id));
    dto.setNetwork("TestNetwork");
    dto.setCalculateTime(calculateTime * 1111111111L);
    dto.setLastTvl((double) id);
    dto.setLastOwnersCount(id);
    dto.setLastAllOwnersCount(id);
    dto.setLastPrice((double) id);
    return dto;
  }
}