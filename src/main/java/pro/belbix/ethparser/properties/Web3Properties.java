package pro.belbix.ethparser.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "web3")
@Getter
@Setter
@ToString
public class Web3Properties {

    private String apiKey = "apiKeyStub";
}
