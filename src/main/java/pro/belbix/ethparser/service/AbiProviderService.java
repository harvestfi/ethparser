package pro.belbix.ethparser.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Log4j2
public class AbiProviderService {

  public final static int RATE_TIMEOUT = 1000;
  public final static String ETH_NETWORK = "eth";
  public final static String BSC_NETWORK = "bsc";
  public final static String MATIC_NETWORK = "matic";
  private final static int RETRY_COUNT = 10;
  private final static String ETHERSCAN_URL = "https://api.etherscan.io/api";
  private final static String BSC_URL = "https://api.bscscan.com/api";
  private final static String MATIC_URL = "https://api.polygonscan.com/api";
  private final static String PARAMS = "?module={module}&action={action}&address={address}&apikey={apikey}";
  private final RestTemplate restTemplate = new RestTemplate();
  private Instant lastRequest = Instant.now();

  public AbiProviderService() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    MappingJackson2HttpMessageConverter convertor = new MappingJackson2HttpMessageConverter();
    convertor.setObjectMapper(mapper);
    restTemplate.setMessageConverters(Collections.singletonList(convertor));
  }

  public SourceCodeResult contractSourceCode(String address, String apiKey, String network) {
    Map<String, ?> uriVariables = Map.of(
        "module", "contract",
        "action", "getsourcecode",
        "address", address,
        "apikey", apiKey
    );
    ResponseEntity<ResponseSourceCode> response;
    SourceCodeResult emptyResult = new SourceCodeResult();

    emptyResult.setContractName("UNKNOWN");
    emptyResult.setProxy("0");
    emptyResult.setImplementation("");

    try {
      response = sendRequest(uriVariables, network);
    } catch (Exception e) {
      log.error("Error fetch contract info for {}", address, e);
      return emptyResult;
    }
    if (response.getStatusCodeValue() != 200) {
      log.error("Error response {}", response.getStatusCode());
    }
    if (response.getBody() == null) {
      log.error("Empty body");
      return emptyResult;
    }
    if (!"1".equals(response.getBody().getStatus())) {
      log.error("Error status: {}", response.getBody().getMessage());
      return emptyResult;
    }
    ResponseSourceCode code = response.getBody();

    if (code.getResult() == null || code.getResult().isEmpty()) {
      log.error("Empty response for {}", address);
      return emptyResult;
    }
    SourceCodeResult result = code.getResult().get(0);
    if (result == null) {
      log.error("Empty code result for {}", address);
      return emptyResult;
    }

    if (result.getAbi() == null
        || "Contract source code not verified".equals(result.getAbi())) {
      log.warn("Not verified source code for {} {}", result.getContractName(), address);
      return emptyResult;
    }

    return result;
  }

  private ResponseEntity<ResponseSourceCode> sendRequest(Map<String, ?> vars, String network) {
    int count = 0;
    while (true) {
      try {
        checkRate();
        return restTemplate.getForEntity(
            getUrl(network) + PARAMS,
            ResponseSourceCode.class,
            vars
        );
      } catch (Exception e) {
        log.error("Error send request {} {}, retry {}", vars,network,  count, e);
        count++;
        if (count == RETRY_COUNT) {
          throw e;
        }
        try {
          //noinspection BusyWait
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  private void checkRate() {
    long diff = Duration.between(lastRequest, Instant.now()).toMillis();
    if(diff < RATE_TIMEOUT) {
      try {
        log.info("Wait rate limit " + (RATE_TIMEOUT - diff));
        Thread.sleep(RATE_TIMEOUT - diff);
      } catch (InterruptedException ignored) {
      }
    }
    lastRequest = Instant.now();
  }

  private String getUrl(String network) {
    if (ETH_NETWORK.equals(network)) {
      return ETHERSCAN_URL;
    } else if (BSC_NETWORK.equals(network)) {
      return BSC_URL;
    } else if (MATIC_NETWORK.equals(network)) {
      return MATIC_URL;
    } else {
      throw new IllegalStateException("Unknown network " + network);
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
