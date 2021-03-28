package pro.belbix.ethparser.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Log4j2
public class EtherscanService {
  private final static int RETRY_COUNT = 10;
  private final static String ETHERSCAN_URI = "https://api.etherscan.io/api"
      + "?module={module}&action={action}&address={address}&apikey={apikey}";
  private final RestTemplate restTemplate = new RestTemplate();

  public EtherscanService() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    MappingJackson2HttpMessageConverter convertor = new MappingJackson2HttpMessageConverter();
    convertor.setObjectMapper(mapper);
    restTemplate.setMessageConverters(Collections.singletonList(convertor));
  }

  public SourceCodeResult contractSourceCode(String address, String apiKey) {
    Map<String, ?> uriVariables = Map.of(
        "module", "contract",
        "action", "getsourcecode",
        "address", address,
        "apikey", apiKey
    );
    ResponseEntity<ResponseSourceCode> response;
    try {
      response = sendRequest(uriVariables);
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
    ResponseSourceCode code = response.getBody();

    if (code.getResult() == null || code.getResult().isEmpty()) {
      log.error("Empty response for {}", address);
      return null;
    }
    SourceCodeResult result = code.getResult().get(0);
    if (result == null) {
      log.error("Empty code result for {}", address);
      return null;
    }

    if (result.getAbi() == null
        || "Contract source code not verified".equals(result.getAbi())) {
      log.warn("Not verified source code for {} {}", result.getContractName(), address);
      return null;
    }

    return result;
  }

  private ResponseEntity<ResponseSourceCode> sendRequest(Map<String, ?> vars) {
    int count = 0;
    while (true) {
      try {
        return restTemplate.getForEntity(
            ETHERSCAN_URI,
            ResponseSourceCode.class,
            vars
        );
      } catch (Exception e) {
        log.error("Error send request {}, retry {}", vars, count, e);
        count++;
        if (count == RETRY_COUNT) {
          throw e;
        }
        try {
          //noinspection BusyWait
          Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
      }
    }
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
