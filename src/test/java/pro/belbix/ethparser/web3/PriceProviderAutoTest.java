package pro.belbix.ethparser.web3;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.controllers.PriceController;
import pro.belbix.ethparser.entity.contracts.TokenEntity;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.contracts.ContractLoader;
import pro.belbix.ethparser.web3.contracts.ContractUtils;

@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PriceProviderAutoTest {

  private static final Double TOLERANCE_PCT = 0.5;
  private static final long TARGET_BLOCK_DATE = 1614201489;

  @SpyBean
  private EthBlockService ethBlockService;

  @Autowired
  private PriceController priceController;

  @Autowired
  private ContractLoader contractLoader;

  @BeforeEach
  void setUp() {
    contractLoader.load();
  }

  private final Set<String> exclude = Set.of(
      "ZERO",
      "SBTC"
  );

  @TestFactory
  public Stream<DynamicTest> tokenPrices()
      throws InterruptedException, ExecutionException, JSONException {
//        doReturn(TARGET_BLOCK_NUMBER).when(ethBlockService).getLastBlock();
    String dateStr = this.getDateByBlockNumber();
        HashMap<String, Double> addressPriceMap = this.fetchPrices(dateStr);

        return ContractUtils.getAllTokens().stream()
            .filter(token -> !exclude.contains(token.getContract().getName()))
            .map(token -> DynamicTest.dynamicTest("token: " + token.getSymbol(), () -> {
                String tokenAddress = token.getContract().getAddress();
                RestResponse response = priceController.token(tokenAddress);
                if (!response.getCode().equals("200")) {
                    Assertions.fail("Failed to calculate price for token: " + token.getSymbol());
                }

                String priceStr = response.getData();
                double price = Double.parseDouble(priceStr.replace(",", "."));

                Double coingeckoPrice = addressPriceMap.get(tokenAddress);
                if (coingeckoPrice == null) {
                    System.out.println("No external price found, Skip token test: " + token.getSymbol());
                    return;
                }

                double priceToleranceDelta = coingeckoPrice * TOLERANCE_PCT;
                Assertions.assertEquals(coingeckoPrice, price, priceToleranceDelta,
                    "Token price deviation: " + token.getSymbol());
            }));
    }

    private HashMap<String, Double> fetchPrices(String date)
        throws InterruptedException, ExecutionException, JSONException {
        HashMap<String, Double> coinPriceMap = new HashMap<>();

        URI coinListUri = getCoinListAPIUri();
        CompletableFuture<String> coinListFuture = this.callCoinGeckoAPI(coinListUri);
        String coinListJsonStr = coinListFuture.get();
        JSONArray coinListJsonArray = new JSONArray(coinListJsonStr);

        List<CompletableFuture<Void>> futuresList = new ArrayList<>();

        for (int i = 0; i < coinListJsonArray.length(); i++) {
            JSONObject coin = coinListJsonArray.getJSONObject(i);
            JSONObject platforms = coin.getJSONObject("platforms");
            if (!platforms.has("ethereum")) {
                continue;
            }

            String ethAddr = platforms.getString("ethereum");
            Optional<TokenEntity> token = ContractUtils.getTokenByAddress(ethAddr);
            if (token.isEmpty()) {
                continue;
            }

            URI coinHistoryUri = this.getCoinHistoryAPIUri(coin.getString("id"), date);
            CompletableFuture<String> coinHistoryFuture = this.callCoinGeckoAPI(coinHistoryUri);
            CompletableFuture<Void> future = coinHistoryFuture.thenAccept(coinHistoryJsonStr -> {
                JSONObject coinHistoryJson;
                try {
                    coinHistoryJson = new JSONObject(coinHistoryJsonStr);
                    if (!coinHistoryJson.has("market_data")) {
                        return;
                    }

                    String historyPrice = coinHistoryJson
                        .getJSONObject("market_data")
                        .getJSONObject("current_price")
                        .getString("usd");
                    coinPriceMap.put(ethAddr, Double.parseDouble(historyPrice));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });

            futuresList.add(future);
        }

        CompletableFuture
            .allOf(futuresList.toArray(new CompletableFuture[0]))
            .get();

        return coinPriceMap;
    }

    private URI getCoinListAPIUri() {
        String coinListUri = "https://api.coingecko.com/api/v3/coins/list?include_platform=true";
        return URI.create(coinListUri);
    }

    private URI getCoinHistoryAPIUri(String coinId, String date) {
        String coinHistoryTpl = "https://api.coingecko.com/api/v3/coins/%s/history?date=%s";
        String coinHistoryUri = String.format(coinHistoryTpl, coinId, date);
        return URI.create(coinHistoryUri);
    }

    private CompletableFuture<String> callCoinGeckoAPI(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri).header("Accept", "application/json").build();

        return HttpClient.newHttpClient()
            .sendAsync(request, BodyHandlers.ofString())
            .thenApply(HttpResponse::body);
    }

    private String getDateByBlockNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      return dateFormat.format(new Date(TARGET_BLOCK_DATE * 1000));
    }


}
