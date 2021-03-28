package pro.belbix.ethparser.repositories.c_layer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.views.UniPriceView;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class UniPriceViewRepositoryTest {

  @Autowired
  private UniPriceViewRepository uniPriceViewRepository;

  @Test
  void findByAddressesAndLogNames() {
    List<UniPriceView> uniPrices =
        uniPriceViewRepository.findByAddressesAndLogNames(
            List.of("0x0d4a11d5EEaaC28EC3F61d100daF4d40471f1852".toLowerCase()),
            List.of("Swap"),
            0,
            999999999,
            PageRequest.of(0, 1)
        );
    assertNotNull(uniPrices);
  }
}
