package pro.belbix.ethparser.controllers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.contracts.TokenInfo;
import pro.belbix.ethparser.web3.contracts.Tokens;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceControllerTest {

    @Autowired
    private PriceController priceController;

    private final Double tolerancePct = 0.05;

    private JSONObject fetchPrices() throws InterruptedException, ExecutionException {
        String[] tokenAddresses = Tokens.tokenInfos.stream()
            .map(TokenInfo::getTokenAddress)
            .toArray(String[]::new);

        String uriTpl = "https://api.coingecko.com/api/v3/simple/token_price/ethereum?contract_addresses=%s&vs_currencies=usd&include_market_cap=true&include_24hr_vol=true&include_24hr_change=true&include_last_updated_at=true";
        String uriString = String.format(uriTpl, String.join(",", tokenAddresses));

        URI uri = URI.create(uriString);
        CompletableFuture<JSONObject> future = this.fetchPriceFromCoingecko(uri);
        JSONObject json = future.get();
        return json;
    }

    private CompletableFuture<JSONObject> fetchPriceFromCoingecko(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri).header("Accept", "application/json").build();

        return HttpClient.newHttpClient().sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
                .thenApply(t -> {
                    try {
                        return new JSONObject(t);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    @TestFactory
    public Stream<DynamicTest> tokenPrices() throws InterruptedException, ExecutionException {
        JSONObject coingeckoPrices = this.fetchPrices();

        return Tokens.tokenInfos.stream()
                .map(tokenInfo -> DynamicTest.dynamicTest("token: " + tokenInfo.getTokenName(), () -> {
                    String tokenAddress = tokenInfo.getTokenAddress();
                    RestResponse response = priceController.token(tokenAddress);
                    String priceStr = response.getData();
                    double price = Double.parseDouble(priceStr.replace(",", "."));

                    String coingeckoPriceStr = coingeckoPrices.getJSONObject(tokenAddress).getString("usd");
                    double coingeckoPrice = Double.parseDouble(coingeckoPriceStr);

                    double priceToleranceDelta = coingeckoPrice * this.tolerancePct;

                    Assertions.assertEquals(coingeckoPrice, price, priceToleranceDelta);

                }));
    }

}
