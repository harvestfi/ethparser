package pro.belbix.ethparser.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ethparser")
@Getter
@Setter
public class AppProperties {

    private String[] networks = new String[]{"eth", "bsc"};

    private boolean onlyApi = false;
    private boolean onlyParse = false;
    private boolean testWs = false;
    private int testWsRate = 1000;
    private String startUtil = "";
    private String utilNetwork = "";
    private boolean overrideDuplicates = false;
    private boolean updateContracts = false;
    private boolean stopOnParseError = false;
    private boolean reconnectSubscriptions = true;
    private boolean skipSimilarPrices = true;
    private int handleLoopStep = 1000;
}
