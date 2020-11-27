package pro.belbix.ethparser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.belbix.ethparser.properties.AppProperties;
import pro.belbix.ethparser.utils.UtilsStarter;

@SpringBootTest(classes = Application.class)
public class StartUtils {
    @Autowired
    private UtilsStarter utilsStarter;
    @Autowired
    private AppProperties appProperties;

    @Test
    public void startUtils() {
        if(appProperties.isDevMod()) {
            utilsStarter.startUtils();
        }
    }

}
