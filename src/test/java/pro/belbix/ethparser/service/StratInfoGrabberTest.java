package pro.belbix.ethparser.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.properties.EthAppProperties;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
class StratInfoGrabberTest {

  @Autowired
  private StratInfoGrabber stratInfoGrabber;
  @Autowired
  private EthAppProperties ethAppProperties;

  @Test
  void smokeTest() {
    ethAppProperties.setGrabStratInfo(true);
    stratInfoGrabber.grab();
  }
}
