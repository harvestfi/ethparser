package pro.belbix.ethparser.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Log4j2
public class EtherscanService {

  private final static String ETHERSCAN_URI = "https://api.etherscan.io/api"
      + "?module={module}&action={action}&address={address}&apikey={apikey}";
  private final RestTemplate restTemplate = new RestTemplate();

  public ResponseSourceCode contractSourceCode(String address, String apiKey) {
    Map<String, ?> uriVariables = Map.of(
        "module", "contract",
        "action", "getsourcecode",
        "address", address,
        "apikey", apiKey
    );
    ResponseEntity<ResponseSourceCode> response;
    try {
      response = restTemplate.getForEntity(
          ETHERSCAN_URI,
          ResponseSourceCode.class,
          uriVariables
      );
    } catch (Exception e) {
      log.error("Error fetch contract info for {}", address, e);
      return null;
    }
    if (response.getStatusCodeValue() != 200) {
      log.error("Error response {}", response.getStatusCode());
    }
    if (response.getBody() == null) {
      log.error("Empty body");
      return null;
    }
    if (!"1".equals(response.getBody().getStatus())) {
      log.error("Error status: {}", response.getBody().getMessage());
      return null;
    }
    return response.getBody();
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ResponseSourceCode {

    private String status;
    private String message;
    private List<SourceCodeResult> result;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SourceCodeResult {

    @JsonProperty("SourceCode")
    private String sourceCode;
    @JsonProperty("ABI")
    private String abi;
    @JsonProperty("ContractName")
    private String contractName;
    @JsonProperty("CompilerVersion")
    private String compilerVersion;
    @JsonProperty("OptimizationUsed")
    private String optimizationUsed;
    @JsonProperty("Runs")
    private String runs;
    @JsonProperty("ConstructorArguments")
    private String constructorArguments;
    @JsonProperty("EVMVersion")
    private String eVMVersion;
    @JsonProperty("Library")
    private String library;
    @JsonProperty("LicenseType")
    private String licenseType;
    @JsonProperty("Proxy")
    private String proxy;
    @JsonProperty("Implementation")
    private String implementation;
    @JsonProperty("SwarmSource")
    private String swarmSource;
  }


}
