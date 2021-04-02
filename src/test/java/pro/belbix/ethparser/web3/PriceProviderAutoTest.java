package pro.belbix.ethparser.web3;

import static pro.belbix.ethparser.service.AbiProviderService.BSC_NETWORK;
import static pro.belbix.ethparser.service.AbiProviderService.ETH_NETWORK;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.controllers.PriceController;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceProviderAutoTest {

  // todo create a tolerance for each token
  private static final Double TOLERANCE_PCT = 0.1; // coingecko the has worst data for some tokens :(
  private static final String CG_URL = "https://api.coingecko.com/api/v3/";

  @Autowired
  private PriceController priceController;

  @Autowired
  private ContractLoader contractLoader;

  @Autowired
  private EthBlockService ethBlockService;

  @Autowired
  private Web3Functions web3Functions;

  @BeforeEach
  void setUp() throws Exception {
    contractLoader.load(ETH_NETWORK, BSC_NETWORK);
  }

  private final Set<String> exclude = Set.of(
      "ZERO",
      "YCRV",
      "CRV_CMPND",
      "CRV_BUSD",
      // unstable prices on coingecko
      "DSD",
      "ESD",
      "BSG"
  );

  @TestFactory
  public Stream<DynamicTest> tokenPricesEth() throws Exception {
    web3Functions.setCurrentNetwork(ETH_NETWORK);
    long block = web3Functions.fetchCurrentBlock().longValue();
    return runTests(fetchPrices(ETH_NETWORK), block, ETH_NETWORK);
  }

  @TestFactory
  public Stream<DynamicTest> tokenPricesBsc() throws Exception {
    web3Functions.setCurrentNetwork(BSC_NETWORK);
    long block = web3Functions.fetchCurrentBlock().longValue();
    return runTests(fetchPrices(BSC_NETWORK), block, BSC_NETWORK);

  }

  private Stream<DynamicTest> runTests(HashMap<String, Double> cgPrices, long block, String network) {
    return new ContractUtils(network).getAllTokens().stream()
        .filter(token -> !exclude.contains(token.getContract().getName()))
        .filter(t -> network.equals(t.getContract().getNetwork()))
        .map(token -> {
          String name = token.getContract().getName() + "(" + token.getSymbol() + ")";
          return DynamicTest.dynamicTest(name, () -> {
            String tokenAddress = token.getContract().getAddress();
            RestResponse response = priceController.token(tokenAddress, block, network);
            if (!response.getCode().equals("200")) {
              Assertions.fail("Failed to calculate price for token: " + token.getSymbol());
            }

            String priceStr = response.getData();
            double price = Double.parseDouble(priceStr.replace(",", "."));

            Double coingeckoPrice = cgPrices.get(tokenAddress);
            if (coingeckoPrice == null) {
              System.out.println("No external price found, Skip token test: " + name);
              return;
            }

            Assertions.assertEquals(coingeckoPrice, price, coingeckoPrice * TOLERANCE_PCT,
                "Token price deviation: " + name);
          });
        });
  }

  private HashMap<String, Double> fetchPrices(String network) throws Exception {
    HashMap<String, Double> result = new HashMap<>();
    ContractUtils contractUtils = new ContractUtils(network);
    String coins = contractUtils.getAllTokens().stream()
        .map(t -> t.getContract().getAddress())
        .collect(Collectors.joining(","));
    JSONObject json = new JSONObject(this.callCoinGeckoAPI(
        getCoinPriceAPIUri(network, coins)).get());

    contractUtils.getAllTokens().forEach(t -> {
      String adr = t.getContract().getAddress();
      try {
        double price = json.getJSONObject(adr).getDouble("usd");
        result.put(adr, price);
      } catch (JSONException ignored) {
      }
    });
    return result;
  }

  private URI getCoinPriceAPIUri(String network, String coins) {
    String net;
    if(ETH_NETWORK.equals(network)) {
      net = "ethereum";
    } else if(BSC_NETWORK.equals(network)) {
      net = "bsc"; // doesn't support, maybe cg will add it
    } else {
      throw new IllegalStateException();
    }
    String coinHistoryTpl =
        CG_URL + "simple/token_price/%s?vs_currencies=usd&contract_addresses=%s";
    String coinHistoryUri = String.format(coinHistoryTpl, net, coins);
    return URI.create(coinHistoryUri);
  }

  private CompletableFuture<String> callCoinGeckoAPI(URI uri) {
    HttpRequest request = HttpRequest.newBuilder(uri).header("Accept", "application/json").build();

    return HttpClient.newHttpClient()
        .sendAsync(request, BodyHandlers.ofString())
        .thenApply(HttpResponse::body);
  }

  private String getDateByBlockNumber(long block) {
    long ts = ethBlockService.getTimestampSecForBlock(block);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(new Date(ts * 1000));
  }


}
