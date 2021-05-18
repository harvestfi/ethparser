package pro.belbix.ethparser.repositories.v0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
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

    private final Pageable limitOne = PageRequest.of(0, 1);

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void test_fetchLastPrices() {
        // given
        PriceDTO first = new PriceDTO();
        first.setId("1");
        first.setBlock(1L);
        first.setNetwork("TestNetwork");
        first.setSourceAddress("testSourceAddressFirst");
        first.setTokenAddress("testTokenAddress");

        PriceDTO two = new PriceDTO();
        two.setId("2");
        two.setBlock(3L);
        two.setNetwork("TestNetwork");
        two.setSourceAddress("testSourceAddressFirst");
        two.setTokenAddress("testTokenAddress");

        PriceDTO three = new PriceDTO();
        three.setId("3");
        three.setBlock(4L);
        three.setNetwork("TestNetwork");
        three.setSourceAddress("testSourceAddressTwo");
        three.setTokenAddress("testTokenAddressDifferent");

        PriceDTO four = new PriceDTO();
        four.setId("4");
        four.setBlock(5L);
        four.setNetwork("TestNetworkDifferent");
        four.setSourceAddress("testSourceAddressThree");
        four.setTokenAddress("testTokenAddress");

        //when
        priceRepository.save(first);
        priceRepository.save(two);
        priceRepository.save(three);
        priceRepository.save(four);

        List<PriceDTO> differentNetwork = priceRepository.fetchLastPrices("TestNetworkDifferent");

        List<PriceDTO> listPrices = priceRepository.fetchLastPrices("TestNetwork");

        //then
        assertEquals(differentNetwork.size(), 1);
        assertTrue(EqualsBuilder.reflectionEquals(four, differentNetwork.get(0)));

        assertEquals(listPrices.size(), 2);
        assertTrue(EqualsBuilder.reflectionEquals(two, listPrices.get(0)));
        assertFalse(EqualsBuilder.reflectionEquals(two, listPrices.get(1)));
        assertTrue(EqualsBuilder.reflectionEquals(three, listPrices.get(1)));
    }

    @Test
    public void test_fetchLastPriceByTokenAddress() {
        // given
        Pageable limitZeroToFive = PageRequest.of(0, 5);
        Pageable limitZeroToTwo = PageRequest.of(0, 2);

        PriceDTO first = new PriceDTO();
        first.setId("1");
        first.setBlock(1L);
        first.setNetwork("TestNetwork");
        first.setSourceAddress("testSourceAddress");
        first.setTokenAddress("testTokenAddress");

        PriceDTO two = new PriceDTO();
        two.setId("2");
        two.setBlock(3L);
        two.setNetwork("TestNetwork");
        two.setSourceAddress("testSourceAddress");
        two.setTokenAddress("testTokenAddress");

        PriceDTO three = new PriceDTO();
        three.setId("3");
        three.setBlock(4L);
        three.setNetwork("TestNetwork");
        three.setSourceAddress("testSourceAddressDifferent");
        three.setTokenAddress("testTokenAddressDifferent");

        PriceDTO four = new PriceDTO();
        four.setId("4");
        four.setBlock(5L);
        four.setNetwork("TestNetwork");
        four.setSourceAddress("testSourceAddress");
        four.setTokenAddress("testTokenAddress");

        //when
        priceRepository.save(first);
        priceRepository.save(two);
        priceRepository.save(three);
        priceRepository.save(four);

        List<PriceDTO> differentSourceAddress = priceRepository
                .fetchLastPriceByTokenAddress("testTokenAddressDifferent",
                        Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

        List<PriceDTO> zeroToFive = priceRepository
                .fetchLastPriceByTokenAddress("testTokenAddress",
                        Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

        List<PriceDTO> zeroToTwo = priceRepository
                .fetchLastPriceByTokenAddress("testTokenAddress",
                        Long.MAX_VALUE, "TestNetwork", limitZeroToTwo);

        List<PriceDTO> findAtLeastBlock3L = priceRepository
                .fetchLastPriceByTokenAddress("testTokenAddress",
                        3L, "TestNetwork", limitZeroToFive);

        //then
        assertEquals(differentSourceAddress.size(), 1);
        assertTrue(EqualsBuilder.reflectionEquals(three, differentSourceAddress.get(0)));

        assertEquals(zeroToFive.size(), 3);
        assertTrue(EqualsBuilder.reflectionEquals(first, zeroToFive.get(2)));
        assertFalse(EqualsBuilder.reflectionEquals(first, zeroToFive.get(0)));
        assertTrue(EqualsBuilder.reflectionEquals(four, zeroToFive.get(0)));
        assertFalse(EqualsBuilder.reflectionEquals(four, zeroToFive.get(1)));

        assertEquals(zeroToTwo.size(), 2);
        assertTrue(EqualsBuilder.reflectionEquals(four, zeroToTwo.get(0)));
        assertFalse(EqualsBuilder.reflectionEquals(four, zeroToTwo.get(1)));
        assertTrue(EqualsBuilder.reflectionEquals(two, zeroToTwo.get(1)));

        assertEquals(findAtLeastBlock3L.size(), 2);
        assertTrue(EqualsBuilder.reflectionEquals(two, findAtLeastBlock3L.get(0)));
        assertFalse(EqualsBuilder.reflectionEquals(two, findAtLeastBlock3L.get(1)));
        assertTrue(EqualsBuilder.reflectionEquals(first, findAtLeastBlock3L.get(1)));

    }

    @Test
    public void test_fetchLastPriceBySourceAddress() {
        // given
        Pageable limitZeroToFive = PageRequest.of(0, 5);
        Pageable limitZeroToTwo = PageRequest.of(0, 2);

        PriceDTO first = new PriceDTO();
        first.setId("1");
        first.setBlock(1L);
        first.setNetwork("TestNetwork");
        first.setSourceAddress("testSourceAddress");

        PriceDTO two = new PriceDTO();
        two.setId("2");
        two.setBlock(3L);
        two.setNetwork("TestNetwork");
        two.setSourceAddress("testSourceAddress");

        PriceDTO three = new PriceDTO();
        three.setId("3");
        three.setBlock(4L);
        three.setNetwork("TestNetwork");
        three.setSourceAddress("testSourceAddressDifferent");

        PriceDTO four = new PriceDTO();
        four.setId("4");
        four.setBlock(5L);
        four.setNetwork("TestNetwork");
        four.setSourceAddress("testSourceAddress");

        //when
        priceRepository.save(first);
        priceRepository.save(two);
        priceRepository.save(three);
        priceRepository.save(four);

        List<PriceDTO> differentSourceAddress = priceRepository
                .fetchLastPriceBySourceAddress("testSourceAddressDifferent",
                        Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

        List<PriceDTO> zeroToFive = priceRepository
                .fetchLastPriceBySourceAddress("testSourceAddress",
                        Long.MAX_VALUE, "TestNetwork", limitZeroToFive);

        List<PriceDTO> zeroToTwo = priceRepository
                .fetchLastPriceBySourceAddress("testSourceAddress",
                        Long.MAX_VALUE, "TestNetwork", limitZeroToTwo);

        List<PriceDTO> findAtLeastBlock3L = priceRepository
                .fetchLastPriceBySourceAddress("testSourceAddress",
                        3L, "TestNetwork", limitZeroToFive);

        //then
        assertEquals(differentSourceAddress.size(), 1);
        assertTrue(EqualsBuilder.reflectionEquals(three, differentSourceAddress.get(0)));

        assertEquals(zeroToFive.size(), 3);
        assertTrue(EqualsBuilder.reflectionEquals(first, zeroToFive.get(2)));
        assertFalse(EqualsBuilder.reflectionEquals(first, zeroToFive.get(0)));
        assertTrue(EqualsBuilder.reflectionEquals(four, zeroToFive.get(0)));
        assertFalse(EqualsBuilder.reflectionEquals(four, zeroToFive.get(1)));

        assertEquals(zeroToTwo.size(), 2);
        assertTrue(EqualsBuilder.reflectionEquals(four, zeroToTwo.get(0)));
        assertFalse(EqualsBuilder.reflectionEquals(four, zeroToTwo.get(1)));
        assertTrue(EqualsBuilder.reflectionEquals(two, zeroToTwo.get(1)));

        assertEquals(findAtLeastBlock3L.size(), 2);
        assertTrue(EqualsBuilder.reflectionEquals(two, findAtLeastBlock3L.get(0)));
        assertFalse(EqualsBuilder.reflectionEquals(two, findAtLeastBlock3L.get(1)));
        assertTrue(EqualsBuilder.reflectionEquals(first, findAtLeastBlock3L.get(1)));
    }
}
