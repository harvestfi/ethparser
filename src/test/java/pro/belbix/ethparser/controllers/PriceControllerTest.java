package pro.belbix.ethparser.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

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
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.entity.eth.TokenEntity;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.EthBlockService;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceControllerTest {
    private final Double tolerancePct = 0.05;
    private final long targetBlockNumber = 11790000L;

    @SpyBean
    private EthBlockService ethBlockService;

    @Autowired
    private PriceController priceController;

    @Autowired
    private ContractLoader contractLoader;

    private HashMap<String, Double> fetchPrices(String date) throws InterruptedException, ExecutionException, JSONException {
        HashMap<String, Double> coinPriceMap = new HashMap<String, Double>();

        URI coinListUri = getCoinListAPIUri();
        CompletableFuture<String> coinListFuture = this.callCoinGeckoAPI(coinListUri);
        String coinListJsonStr = coinListFuture.get();
        JSONArray coinListJsonArray = new JSONArray(coinListJsonStr);

        List<CompletableFuture<Void>> futuresList = new ArrayList<CompletableFuture<Void>>();
        
        for (Integer i = 0; i < coinListJsonArray.length(); i++) {
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
            .allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]))
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

    private String getDateByBlockNumber(long blockNumber) {
        long ts = ethBlockService.getTimestampSecForBlock(null, blockNumber);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateStr = dateFormat.format(new Date(ts * 1000));

        return dateStr;
    }

    @TestFactory
    public Stream<DynamicTest> tokenPrices() throws InterruptedException, ExecutionException, JSONException {
        contractLoader.load();

        doReturn(this.targetBlockNumber).when(ethBlockService).getLastBlock();
        String dateStr = this.getDateByBlockNumber(this.targetBlockNumber);
        HashMap<String, Double> addressPriceMap = this.fetchPrices(dateStr);

        return ContractUtils.getAllTokens().stream()
            .filter(token -> token.getSymbol() != "ZERO" && !token.getSymbol().isEmpty())
            .map(token -> DynamicTest.dynamicTest("token: " + token.getSymbol(), () -> {
                String tokenAddress = token.getContract().getAddress();
                RestResponse response = priceController.token(tokenAddress);
                if (response.getCode() != "200") {
                    fail("Failed to calculate price for token: " + token.getSymbol());
                }

                String priceStr = response.getData();
                Double price = Double.parseDouble(priceStr.replace(",", "."));

                Double coingeckoPrice = addressPriceMap.get(tokenAddress);
                if (coingeckoPrice == null) {
                    System.out.println("No external price found, Skip token test: " + token.getSymbol());
                    return;
                }

                Double priceToleranceDelta = coingeckoPrice * this.tolerancePct;
                assertEquals("Token price deviation: " + token.getSymbol(), coingeckoPrice, price, priceToleranceDelta);
            }));
    }

}
