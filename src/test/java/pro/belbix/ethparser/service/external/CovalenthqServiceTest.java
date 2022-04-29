package pro.belbix.ethparser.service.external;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureWireMock(port = 801)
@TestPropertySource(properties = {
    "external.covalenthq.url=http://localhost:801/",
    "external.covalenthq.key=1"
})
public class CovalenthqServiceTest {
  private static final String TRANSACTION_URL_PART = "/1/address/0x1234/transactions_v2/?quote-currency=USD&format=JSON&block-signed-at-asc=true&no-logs=false&key=1&page-number=0&page-size=3";
  private static final String TRANSACTION_SECOND_PAGE_URL_PART = "/1/address/0x1234/transactions_v2/?quote-currency=USD&format=JSON&block-signed-at-asc=true&no-logs=false&key=1&page-number=1&page-size=3";
  private static final String ADDRESS = "0x1234";
  private static final long BLOCK = 111111;

  @Autowired
  private CovalenthqService covalenthqService;


  @Test
  void testNotNull() {
    assertThat(covalenthqService).isNotNull();
  }

  @Test
  void getCreatedBlockByLastTransaction_successResult() {
    stubFor(
        get(urlEqualTo(TRANSACTION_URL_PART))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, "application/json")
                .withBodyFile("transaction-simple-response.json")
            )
    );
    var result = covalenthqService.getCreatedBlockByLastTransaction(ADDRESS, ETH_NETWORK);
    assertThat(result).isEqualTo(BLOCK);
  }

  @Test
  void getCreatedBlockByLastTransaction_emptyResult() {
    stubFor(
        get(urlEqualTo(TRANSACTION_URL_PART))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, "application/json")
                .withBodyFile("transaction-empty-response.json")
            )
    );
    var result = covalenthqService.getCreatedBlockByLastTransaction(ADDRESS, ETH_NETWORK);
    assertThat(result).isEqualTo(0);
  }

  @Test
  void getCreatedBlockByLastTransaction_withTwoCall() {
    stubFor(
        get(urlEqualTo(TRANSACTION_URL_PART))
            .inScenario("transfer")
            .whenScenarioStateIs(Scenario.STARTED)
            .willSetStateTo("second")
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, "application/json")
                .withBodyFile("transaction-with-transfer-response.json")
            )
    );
    stubFor(
        get(urlEqualTo(TRANSACTION_SECOND_PAGE_URL_PART))
            .inScenario("transfer")
            .whenScenarioStateIs("second")
            .willSetStateTo(Scenario.STARTED)
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, "application/json")
                .withBodyFile("transaction-simple-response.json")
            )
    );

    var result = covalenthqService.getCreatedBlockByLastTransaction(ADDRESS, ETH_NETWORK);
    assertThat(result).isEqualTo(BLOCK);
  }

}
