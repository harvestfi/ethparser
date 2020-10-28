package pro.belbix.ethparser.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "web3")
public class Web3Properties {

    private String apiKey = "apiKeyStub";
    private String startBlock = "";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(String startBlock) {
        this.startBlock = startBlock;
    }
}
