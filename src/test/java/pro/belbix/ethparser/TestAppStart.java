package pro.belbix.ethparser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.belbix.ethparser.properties.AppProperties;

@SpringBootTest(classes = Application.class)
public class TestAppStart {
    @Autowired
    private AppProperties appProperties;

    @Test
    public void startTest() throws InterruptedException {
        Application.main(new String[]{});
        while (appProperties.isDevMod()) {
            Thread.sleep(1000);
        }
    }
}
