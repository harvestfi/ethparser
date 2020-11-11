package pro.belbix.ethparser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.belbix.ethparser.utils.UtilsStarter;

@SpringBootTest(classes = Application.class)
public class StartUtils {
    @Autowired
    private UtilsStarter utilsStarter;

    @Test
    public void startUtils() {
        utilsStarter.startUtils();
    }

}
