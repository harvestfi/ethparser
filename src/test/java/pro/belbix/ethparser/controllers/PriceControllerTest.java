package pro.belbix.ethparser.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.model.RestResponse;
import pro.belbix.ethparser.web3.contracts.ContractUtils;
import pro.belbix.ethparser.web3.contracts.ContractLoader;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class PriceControllerTest {

    @Autowired
    private PriceController priceController;
    @Autowired
    private ContractLoader contractLoader;

    private final Double tolerancePct = 0.05;

    private JSONObject fetchPrices() throws InterruptedException, ExecutionException {
        String[] tokenAddresses = ContractUtils.getAllTokens().stream()
            .map(token -> token.getContract().getAddress())
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
        contractLoader.load();
        JSONObject coingeckoPrices = this.fetchPrices();

        return ContractUtils.getAllTokens().stream()
                .filter(token -> token.getSymbol() != "ZERO")
                .map(token -> DynamicTest.dynamicTest("token: " + token.getSymbol(), () -> {
                    String tokenAddress = token.getContract().getAddress();
                    RestResponse response = priceController.token(tokenAddress);
                    if (response.getCode() != "200") {
                        fail("Failed to calculate price for token: " + token.getSymbol());
                    }

                    String priceStr = response.getData();
                    double price = Double.parseDouble(priceStr.replace(",", "."));

                    String coingeckoPriceStr = null;
                    try {
                        coingeckoPriceStr = coingeckoPrices.getJSONObject(tokenAddress).getString("usd");
                    }
                    catch (Exception e) {
                        System.out.println("No external price found, Skip token test: " + token.getSymbol());
                        return;
                    }

                    double coingeckoPrice = Double.parseDouble(coingeckoPriceStr);

                    double priceToleranceDelta = coingeckoPrice * this.tolerancePct;

                    assertEquals("Token price deviation: " + token.getSymbol(), coingeckoPrice, price, priceToleranceDelta);
                }));
    }

}
