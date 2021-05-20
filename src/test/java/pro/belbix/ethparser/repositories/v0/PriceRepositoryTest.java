package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.belbix.ethparser.TestUtils.assertModel;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.v0.PriceDTO;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceRepositoryTest {

  @Autowired
  private PriceRepository priceRepository;

  private int id = 0;

  @Test
  public void test_fetchLastPrices() {
    // given
    id = 0;
    PriceDTO first = createDTO("sourceFirst", "tokenAddress");
    PriceDTO two = createDTO("sourceFirst", "tokenAddress");
    PriceDTO three = createDTO("sourceTwo", "tokenAddressDifferent");
    PriceDTO four = createDTO("sourceThree", "tokenAddress");
    four.setNetwork("TestNetworkDifferent");

    //when
    priceRepository.save(first);
    priceRepository.save(two);
    priceRepository.save(three);
    priceRepository.save(four);

    List<PriceDTO> differentNetwork = priceRepository.fetchLastPrices("TestNetworkDifferent");
    List<PriceDTO> listPrices = priceRepository.fetchLastPrices("TestNetwork");

    //then
    assertAll(
        () -> assertEquals(differentNetwork.size(), 1),
        () -> assertModel(four, differentNetwork.get(0)),

        () -> assertEquals(listPrices.size(), 2),
        () -> assertEquals(two, listPrices.get(0)),
        () -> assertModel(three, listPrices.get(1))
    );

    //after
    id = 0;
    priceRepository.delete(first);
    priceRepository.delete(two);
    priceRepository.delete(three);
    priceRepository.delete(four);
  }

  @Test
  public void test_fetchLastPriceByTokenAddress() {
    // given
    id = 0;

    Pageable limitZeroToFive = PageRequest.of(0, 5);
    Pageable limitZeroToTwo = PageRequest.of(0, 2);

    PriceDTO first = createDTO("sourceAddress", "tokenAddress");
    PriceDTO two = createDTO("sourceAddress", "tokenAddress");
    PriceDTO three = createDTO("sourceAddressDifferent", "tokenAddressDifferent");
    PriceDTO four = createDTO("testSourceAddress", "tokenAddress");

    //when
    priceRepository.save(first);
    priceRepository.save(two);
    priceRepository.save(three);
    priceRepository.save(four);

    List<PriceDTO> differentSourceAddress = priceRepository
        .fetchLastPriceByTokenAddress(
            "tokenAddressDifferent",
            Long.MAX_VALUE,
            "TestNetwork",
            limitZeroToFive);

    List<PriceDTO> zeroToFive = priceRepository
        .fetchLastPriceByTokenAddress(
            "tokenAddress",
            Long.MAX_VALUE,
            "TestNetwork",
            limitZeroToFive);

    List<PriceDTO> zeroToTwo = priceRepository
        .fetchLastPriceByTokenAddress(
            "tokenAddress",
            Long.MAX_VALUE,
            "TestNetwork",
            limitZeroToTwo);

    List<PriceDTO> findAtLeastBlock3L = priceRepository
        .fetchLastPriceByTokenAddress(
            "tokenAddress",
            3L,
            "TestNetwork",
            limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(differentSourceAddress.size(), 1),
        () -> assertModel(three, differentSourceAddress.get(0)),

        () -> assertEquals(zeroToFive.size(), 3),
        () -> assertModel(first, zeroToFive.get(2)),
        () -> assertModel(four, zeroToFive.get(0)),

        () -> assertEquals(zeroToTwo.size(), 2),
        () -> assertModel(four, zeroToTwo.get(0)),
        () -> assertModel(two, zeroToTwo.get(1)),

        () -> assertEquals(findAtLeastBlock3L.size(), 2),
        () -> assertModel(two, findAtLeastBlock3L.get(0)),
        () -> assertModel(first, findAtLeastBlock3L.get(1))
    );

    //after
    id = 0;

    priceRepository.delete(first);
    priceRepository.delete(two);
    priceRepository.delete(three);
    priceRepository.delete(four);
  }

  @Test
  public void test_fetchLastPriceBySourceAddress() {
    // given
    id = 0;
    Pageable limitZeroToFive = PageRequest.of(0, 5);
    Pageable limitZeroToTwo = PageRequest.of(0, 2);

    PriceDTO first = createDTO("testSource", "token");
    PriceDTO two = createDTO("testSource", "token");
    PriceDTO three = createDTO("testSourceDifferent", "token");
    PriceDTO four = createDTO("testSource", "token");

    //when
    priceRepository.save(first);
    priceRepository.save(two);
    priceRepository.save(three);
    priceRepository.save(four);

    List<PriceDTO> differentSourceAddress = priceRepository
        .fetchLastPriceBySourceAddress(
            "testSourceDifferent",
            Long.MAX_VALUE,
            "TestNetwork",
            limitZeroToFive);

    List<PriceDTO> zeroToFive = priceRepository
        .fetchLastPriceBySourceAddress(
            "testSource",
            Long.MAX_VALUE,
            "TestNetwork",
            limitZeroToFive);

    List<PriceDTO> zeroToTwo = priceRepository
        .fetchLastPriceBySourceAddress(
            "testSource",
            Long.MAX_VALUE,
            "TestNetwork",
            limitZeroToTwo);

    List<PriceDTO> findAtLeastBlock3L = priceRepository
        .fetchLastPriceBySourceAddress(
            "testSource",
            3L,
            "TestNetwork",
            limitZeroToFive);

    //then
    assertAll(
        () -> assertEquals(differentSourceAddress.size(), 1),
        () -> assertModel(three, differentSourceAddress.get(0)),

        () -> assertEquals(zeroToFive.size(), 3),
        () -> assertModel(first, zeroToFive.get(2)),
        () -> assertModel(four, zeroToFive.get(0)),

        () -> assertEquals(zeroToTwo.size(), 2),
        () -> assertModel(four, zeroToTwo.get(0)),
        () -> assertModel(two, zeroToTwo.get(1)),

        () -> assertEquals(findAtLeastBlock3L.size(), 2),
        () -> assertModel(two, findAtLeastBlock3L.get(0)),
        () -> assertModel(first, findAtLeastBlock3L.get(1))
    );

    //after
    id = 0;
    priceRepository.delete(first);
    priceRepository.delete(two);
    priceRepository.delete(three);
    priceRepository.delete(four);
  }

  private PriceDTO createDTO(String source, String token) {
    id++;
    PriceDTO dto = new PriceDTO();
    dto.setId(String.valueOf(id));
    dto.setBlock((long) id);
    dto.setNetwork("TestNetwork");
    dto.setSourceAddress(source);
    dto.setTokenAddress(token);
    return dto;
  }
}
