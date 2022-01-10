package pro.belbix.ethparser.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceCleanerServiceTest {

  @Autowired
  private PriceCleanerService priceCleanerService;

  @Test
  void startPriceCleaner_doesNotThrowJpaSystemException() {
    assertDoesNotThrow(() -> priceCleanerService.startPriceCleaner());
  }
}
