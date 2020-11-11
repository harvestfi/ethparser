package pro.belbix.ethparser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public class TestAppStart {

    @Test
    public void startTest() {
        Application.main(new String[]{});
    }
}
